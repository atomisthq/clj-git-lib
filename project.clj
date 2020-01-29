(defproject com.atomist/clj-git-lib "0.3.4-SNAPSHOT"
  :description "Small wrapper around jgit with some handy editing utilities"
  :url "https://github.com/atomisthq/clj-git-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure               "1.10.1"]
                 [clj-jgit                          "0.8.10"]
                 [prismatic/schema "1.1.12"]
                 [org.clojure/tools.logging       "0.5.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.9.0"]                         ;for pretty print output to file
                 [org.slf4j/jcl-over-slf4j "1.7.30"]
                 [org.slf4j/jul-to-slf4j "1.7.30"]
                 [org.slf4j/log4j-over-slf4j "1.7.30"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.clojure/data.json        "0.2.7"]]
  :exclusions [commons-logging log4j org.slf4j/slf4j-log4j12]

  :vcs :git

  :release-tasks [#_["vcs" "assert-committed"]
                  ["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :repositories [["releases" {:url "https://sforzando.jfrog.io/sforzando/libs-release-local"
                              :sign-releases false
                              :username [:gpg :env/artifactory_user]
                              :password [:gpg :env/artifactory_pwd]}]]
  :profiles {:dev
             {:source-paths ["dev"]
              :plugins      []}})
