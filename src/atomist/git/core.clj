(ns atomist.git.core
  (:require [clj-jgit.porcelain              :as jgit]
            [schema.core                     :as s]
            [clojure.pprint                  :refer :all]
            [atomist.git.schemas             :as t]
            [clojure.java.io                 :as io]
            [clojure.data.json               :as json])
  (:import [java.io File]))

(declare perform-write perform-mkdir perform-copy perform-git-add perform-git-commit perform-git-tag perform-edit perform-git-checkout)

(def ^:dynamic repo nil)

(defn matches? [schema thing]
  (nil? (s/check schema thing)))

(defn contains-repo [f]
  (let [dot-git (File. f ".git")]
    (and (.exists dot-git) (.isDirectory dot-git))))

(defmacro perform [^java.io.File f & instructions]
  `(if (and (.exists ~f) (contains-repo ~f))
     (with-bindings {#'repo ~f}
       (act-on-filesystem [~@instructions]))
     (throw (ex-info "Not a valid git repository" {:error (str ~f "is not a valid git repository")}))))

(s/defn act-on-filesystem [instructions :- [t/Instruction]]
  (let [errors (remove nil? (map :error instructions))]
    (cond
      (seq errors)
      (do (println "Errors:" errors)
          (throw (ex-info "Errors occurred" {:errors errors :instructions instructions})))
      :else
      (doseq [one-instruction instructions]
        (println ".... perform " one-instruction)
        (condp matches? one-instruction

          t/EditInstruction
          (perform-edit one-instruction)

          t/WriteInstruction
          (perform-write one-instruction)

          t/MkdirInstruction
          (perform-mkdir one-instruction)

          t/CopyInstruction
          (perform-copy one-instruction)

          t/GitCheckout
          (perform-git-checkout one-instruction)

          t/GitAdd
          (perform-git-add one-instruction)

          t/GitCommit
          (perform-git-commit one-instruction)

          t/GitTag
          (perform-git-tag one-instruction)

          t/NoOp
          nil)))))

(defmulti edit (fn [file-pattern _]
                 (cond
                   (or (.endsWith file-pattern ".yaml") (.endsWith file-pattern ".yml"))
                   :yaml

                   (.endsWith file-pattern ".json")
                   :json

                   :else
                   :slurp)))

(s/defn ^:always-validate perform-edit [instr :- t/EditInstruction]
  (let [{:keys [file-pattern editor]} (:edit instr)]
    (edit file-pattern editor)))

(s/defn ^:always-validate perform-write [instr :- t/WriteInstruction]
  (let [{destination :to, contents :contents} (:write instr)]
    (spit destination contents)))

(s/defn ^:always-validate perform-mkdir [instr :- t/MkdirInstruction]
  (let [{file-that-needs-a-home :for} (:mkdir instr)]
    (io/make-parents (File. repo file-that-needs-a-home))))

(s/defn ^:always-validate perform-copy [instr :- t/CopyInstruction]
  (let [{from-file :from to-file :to} (:copy instr)]
    (io/copy (File. repo from-file) (File. repo to-file))))

(s/defn ^:always-validate perform-git-add [instr :- t/GitAdd]
  (let [{file-that-needs-adding :file-pattern} (:git-add instr)]
    (jgit/with-repo repo (jgit/git-add repo file-that-needs-adding))))

(s/defn ^:always-validate perform-git-commit [instr :- t/GitCommit]
  (let [{commit-message :message} (:git-commit instr)]
    (jgit/with-repo repo (jgit/git-commit repo commit-message))))

(s/defn ^:always-validate perform-git-tag [instr :- t/GitTag]
  (let [{tag-message :message} (:git-tag instr)]
    (jgit/with-repo repo (jgit/git-tag repo tag-message))))

(s/defn ^:always-validate perform-git-push [instr :- t/GitPush]
  (let [{:keys [remote branch]} (:git-push instr)]
    (jgit/with-repo repo (jgit/git-push))))

(s/defn ^:always-validate perform-git-checkout [instr :- t/GitCheckout]
  (let [{:keys [branch]} (:git-checkout instr)]
    (jgit/with-repo repo (jgit/git-checkout branch))))

(defmethod edit :yaml
  [_ _]
  (throw (UnsupportedOperationException. "no support for yaml yet")))

(defmethod edit :json
  [file-pattern editor]
  (->> (slurp (File. repo file-pattern))
       (json/read-json)
       (editor)
       (json/json-str)
       (spit (File. repo file-pattern))))

(defmethod edit :slurp
  [file-pattern editor]
  (->> (slurp (File. repo file-pattern))
      (editor)
      (spit (File. repo file-pattern))))


