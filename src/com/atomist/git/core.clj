(ns com.atomist.git.core
  (:require [clj-jgit.porcelain              :as jgit]
            [schema.core                     :as s]
            [clojure.pprint                  :refer :all]
            [com.atomist.git.schemas             :as t]
            [clojure.java.io                 :as io]
            [clojure.data.json               :as json])
  (:import [java.io File]))

(defn contains-repo [f]
  (let [dot-git (File. f ".git")]
    (and (.exists dot-git) (.isDirectory dot-git))))

(defmulti perform-instruction
  "Do something to a git repo"
  (fn [type _]
    type))

(defmulti edit (fn [_ file-pattern _]
                 (cond
                   (or (.endsWith file-pattern ".yaml") (.endsWith file-pattern ".yml"))
                   :yaml
                   (.endsWith file-pattern ".json")
                   :json

                   :else
                   :slurp)))

(defmethod perform-instruction :edit
  [_ instr]
  (let [{:keys [file-pattern editor]} instr]
    (edit (:repo instr) file-pattern editor)))

(defmethod perform-instruction :write
  [_ instr]
  (let [{destination :to, contents :contents} instr]
    (spit destination contents)))

(defmethod perform-instruction :mkdir
  [_ instr]
  (let [{file-that-needs-a-home :for} instr]
    (io/make-parents (File. (:repo instr) file-that-needs-a-home))))

(defmethod perform-instruction :copy
  [_ instr]
  (let [{from-file :from to-file :to} instr]
    (io/copy (File. (:repo instr) from-file) (File. (:repo instr) to-file))))

(defmethod perform-instruction :git-checkout
  [_ instr]
  (let [{:keys [branch]} instr]
    (jgit/with-repo (:repo instr)
      (jgit/git-checkout branch))))

(defmethod perform-instruction :git-commit
  [_ instr]
  (let [{commit-message :message} instr]
    (jgit/with-repo (:repo instr) (jgit/git-commit repo commit-message))))

(defmethod perform-instruction :git-tag
  [_ instr]
  (let [{tag-message :message} instr]
    (jgit/with-repo (:repo instr) (jgit/git-tag repo tag-message))))

(defmethod perform-instruction :git-push
  [_ instr]
  (let [{:keys [remote branch]} instr]
    (jgit/with-repo (:repo instr) (jgit/git-push))))

(defmethod perform-instruction :git-clone
  [_ instr]
  (jgit/git-clone (str "https://" (:oauth-token instr) "@github.com/" (:org instr) "/" (:repo instr) ".git") (File. (:to instr))))

(defmethod perform-instruction :git-add
  [_ instr]
  (let [{file-that-needs-adding :file-pattern} instr]
    (jgit/with-repo (:repo instr)
      (jgit/git-add repo file-that-needs-adding))))

(defn act-on-filesystem
  [^java.io.File repo instructions]
  (let [errors (remove nil? (map :error instructions))]
    (cond
      (seq errors)
      (do (println "Errors:" errors)
          (throw (ex-info "Errors occurred" {:errors errors :instructions instructions})))
      :else
      (loop [instructions instructions]
        (when (not-empty instructions)
          (println ".... perform " (take 2 instructions))
          (perform-instruction (first instructions) (assoc (second instructions) :repo repo))
          (recur (nthrest instructions 2)))))))

(defn perform
  [^java.io.File repo & instructions]
  (if (or (= :git-clone (first instructions))
        (and (.exists repo) (contains-repo repo)))
    (act-on-filesystem repo instructions)
    (throw (ex-info "Not a valid git repository" {:error (str repo "is not a valid git repository")}))))

(defmethod edit :default
  [_ _ _]
  (throw (UnsupportedOperationException. "no support yet")))

(defmethod edit :json
  [repo file-pattern editor]
  (->> (slurp (File. repo file-pattern))
       (json/read-json)
       (editor)
       (json/json-str)
       (spit (File. repo file-pattern))))

(defmethod edit :slurp
  [repo file-pattern editor]
  (->> (slurp (File. repo file-pattern))
      (editor)
      (spit (File. repo file-pattern))))


