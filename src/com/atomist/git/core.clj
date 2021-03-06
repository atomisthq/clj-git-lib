(ns com.atomist.git.core
  (:require [clj-jgit.porcelain :as jgit]
            [clj-jgit.querying :as query]
            [clojure.pprint :refer :all]
            [cheshire.core :as cheshire]
            [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:import [java.io File]
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.transport UsernamePasswordCredentialsProvider RefSpec)
           (org.eclipse.jgit.internal.storage.file FileRepository)
           (org.eclipse.jgit.lib ObjectId)
           (org.eclipse.jgit.revwalk RevCommit)))

(defn contains-repo [f]
  (let [dot-git (File. f ".git")]
    (and (.exists dot-git) (.isDirectory dot-git))))

(defmulti perform-instruction
  "Do something to a git repo"
  (fn [instr]
    (:command instr)))

(defmulti edit (fn [_ file-pattern _]
                 (cond
                   (or (.endsWith file-pattern ".yaml") (.endsWith file-pattern ".yml"))
                   :yaml
                   (.endsWith file-pattern ".json")
                   :json

                   :else
                   :slurp)))

(defmethod perform-instruction :edit
  [{params :params :as instr}]
  (let [{:keys [file-pattern editor]} params]
    (edit (:repo instr) file-pattern editor)))

(defmethod perform-instruction :editor
  [{params :params :as instr}]
  (let [{:keys [editor]} params]
    (editor (:repo instr))))

(defmethod perform-instruction :write
  [{params :params :as instr}]
  (let [{destination :to, contents :contents} params]
    (spit destination contents)))

(defmethod perform-instruction :mkdir
  [{params :params :as instr}]
  (let [{file-that-needs-a-home :for} params]
    (io/make-parents (File. (:repo instr) file-that-needs-a-home))))

(defmethod perform-instruction :copy
  [{params :params :as instr}]
  (let [{from-file :from to-file :to} params]
    (io/copy (File. (:repo instr) from-file) (File. (:repo instr) to-file))))

(defmethod perform-instruction :git-checkout
  [{params :params :as instr}]
  (let [{:keys [branch]} params]
    (jgit/with-repo (:repo instr)
      (jgit/git-checkout repo branch))))

(defmethod perform-instruction :git-commit
  [{params :params :as instr}]
  (let [{commit-message :message name :name email :email} params]
    (jgit/with-repo (:repo instr)
      (if (and name email)
        (jgit/git-commit repo commit-message {:name name :email email})
        (jgit/git-commit repo commit-message)))))

(defmethod perform-instruction :git-branch-create
  [{params :params :as instr}]
  (let [{:keys [branch]} params]
    (jgit/with-repo (:repo instr)
      (if branch
        (jgit/git-branch-create repo branch)))))

(defmethod perform-instruction :git-tag
  [{params :params :as instr}]
  (let [{tag-message :message tag-name :name} params]
    (if (:oauth-token params)
      (->
       (Git. (FileRepository. (File. (:repo instr) "/.git")))
       (.tag)
       (.setName tag-name)
       (.setCredentialsProvider (UsernamePasswordCredentialsProvider. "token" (str (:oauth-token params))))
       (.call))
      (->
       (Git. (FileRepository. (File. (:repo instr) "/.git")))
       (.tag)
       (.setName tag-name)
       (.setMessage tag-message)
       (.call)))))

(defmethod perform-instruction :git-push
  [{params :params :as instr}]
  (if (:oauth-token params)
    (->
     (Git. (FileRepository. (File. (:repo instr) "/.git")))
     (.push)
     (.setRemote (:remote params))
     (.setCredentialsProvider (UsernamePasswordCredentialsProvider. "token" (str (:oauth-token params))))
     (.call))
    (->
     (Git. (FileRepository. (File. (:repo instr) "/.git")))
     (.push)
     (.setPushTags)
     (.setRemote (:remote params))
     (.setRefSpecs [(RefSpec. (:spec params))])
     (.call))))

(defn delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (clojure.java.io/file fname))))

(defmethod perform-instruction :git-clone
  [{params :params :as instr}]
  (let [clone #(doto
                (Git/cloneRepository)
                 (.setDirectory (:repo instr))
                 (.setBranch (:branch params))
                 (.setCloneAllBranches false)
                 (.setURI (str "https://github.com/" (:org params) "/" (:repo-name params) ".git"))
                 (.setCredentialsProvider (UsernamePasswordCredentialsProvider. "token" (str (:oauth-token params))))
                 (.call))]
    (if (not (.exists (:repo instr)))
      (clone)
      (if (:try-fetch? params)
        (try
          (->
           (Git. (FileRepository. (File. (:repo instr) "/.git")))
           (.pull)
           (.setCredentialsProvider (UsernamePasswordCredentialsProvider. "token" (str (:oauth-token params))))
           (.setRebase (if (some? (:rebase? params))
                         (:rebase? params)
                         true))
           (.call))
          (catch Exception e
            (if (:force? params)
              (do
                (delete-recursively (:repo instr))
                (clone))
              (throw e))))
        (throw (RuntimeException. "Target directory already exists, and try-fetch? is not set"))))))

(defmethod perform-instruction :git-add
  [{params :params :as instr}]
  (let [{file-that-needs-adding :file-pattern} params]
    (jgit/with-repo (:repo instr)
      (jgit/git-add repo file-that-needs-adding))))

(defmethod perform-instruction :git-rm
  [{params :params :as instr}]
  (let [{file-that-needs-deleting :file-pattern} params]
    (jgit/with-repo (:repo instr)
      (jgit/git-rm repo file-that-needs-deleting))))

(defn act-on-filesystem
  [^File repo instructions]
  (let [errors (remove nil? (map :error instructions))]
    (cond
      (seq errors) (throw (ex-info "Errors occurred" {:errors errors :instructions instructions}))
      :else
      (loop [instructions instructions]
        (when (not-empty instructions)
          (perform-instruction {:command (first instructions) :params (second instructions) :repo repo})
          (recur (nthrest instructions 2)))))))

(defn perform
  [^File repo & instructions]
  (if (or (= :git-clone (first instructions))
          (and (.exists repo) (contains-repo repo)))
    (act-on-filesystem repo instructions)
    (throw (ex-info "Not a valid git repository" {:error (str repo "is not a valid git repository")}))))

(defmethod edit :default
  [_ _ _]
  (throw (UnsupportedOperationException. "no support yet")))

(defmethod edit :json
  [repo file-pattern editor]
  (let [thefile (File. repo file-pattern)]
    (as->
     (slurp thefile) spec
      (json/read-str spec :key-fn keyword)
      (editor spec)
      (cheshire/generate-string spec {:pretty true})
      (spit thefile spec))))

(defmethod edit :slurp
  [repo file-pattern editor]
  (->> (slurp (File. repo file-pattern))
       (editor)
       (spit (File. repo file-pattern))))

(defn get-head-sha
  [^File repo]
  (ObjectId/toString (.getObjectId (.getRef (FileRepository. (File. repo "/.git")) "HEAD"))))

(defn get-user-info
  ([repo-dir]
   (get-user-info repo-dir nil))
  ([^File repo-dir sha-or-branch]
   (let [ident (.getAuthorIdent (jgit/with-repo repo-dir
                                  ^RevCommit (query/find-rev-commit repo rev-walk (or sha-or-branch "HEAD"))))]
     {:name (.getName ident)
      :email (.getEmailAddress ident)})))