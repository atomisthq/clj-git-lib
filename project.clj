(defproject com.atomist/clj-git-lib "0.3.0"
  :description "Small wrapper around jgit with some handy editing utilities"
  :url "https://github.com/atomisthq/clj-git-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure               "1.8.0"]
                 [clj-jgit                          "0.8.9"]
                 [prismatic/schema "1.1.11"]
                 [org.clojure/tools.logging       "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [cheshire "5.6.3"]                         ;for pretty print output to file
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.clojure/data.json        "0.2.6"]]
  :exclusions [commons-logging log4j org.slf4j/slf4j-log4j12]

  :vcs :git

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :repositories [["releases" {:url      "https://atomist.jfrog.io/atomist/libs-release-local"
                              :sign-releases false
                              :username [:env/artifactory_user]
                              :password [:env/artifactory_pwd]}]]
  :profiles {:dev
             {:source-paths ["dev"]
              :plugins      [[jonase/eastwood "0.2.1"]
                             [lein-cloverage "1.0.6"]
                             [lein-set-version "0.4.1"]
                             [lein-ancient "0.6.8" :exclusions [org.clojure/clojure]]]
              :eastwood     {:namespaces      [:source-paths]
                             :exclude-linters []}}})
