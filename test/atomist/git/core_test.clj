(ns atomist.git.core-test
  (:require [clojure.test                    :refer :all]
            [atomist.git.core                :refer :all]
            [clj-jgit.porcelain              :as jgit])
  (:import  [java.io File]))

(deftest fresh-repo-add-and-commit
  (testing "Test a complete GIT session"
    (let [f (File. "./test-repo")
          editor (fn [_]
                   (println "current data") {:new "data"})]
      ;; (jgit/git-init f)
      (perform f
               {:edit {:file-pattern "whatever.json" :editor editor}}
               {:git-add {:file-pattern "whatever.json"}}
               {:git-commit {:message ""}}))))

(fresh-repo-add-and-commit)


