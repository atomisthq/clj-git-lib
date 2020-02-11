(ns com.atomist.git.core-test
  (:require [clojure.test :refer :all]
            [com.atomist.git.core :refer :all]
            [clj-jgit.porcelain :as jgit]
            [clojure.data.json :as json])
  (:import  [java.io File]))

(deftest fresh-repo-add-and-commit
  (testing "Test a complete GIT session"
    (let [f (File. "./test-repo")
          editor (fn [_]
                   (println "current data") {:new "data"})]
      (jgit/git-init f)
      (spit (File. f "whatever.json") (json/write-str {:key1 "val1" :key2 "val2"}))
      (perform f
               :edit {:file-pattern "whatever.json" :editor editor}
               :git-add {:file-pattern "whatever.json"}
               :git-commit {:message "" :name "test" :email "test@test.com"}))))

(deftest get-a-repo-hash
  (testing "can fetch the current commit of a local repo"
    (let [f (File. "./test-repo2")]
      (jgit/git-init f)
      (is (= "0000000000000000000000000000000000000000" (get-head-sha f))))))

