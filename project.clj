(defproject com.atomist/clj-git-lib "0.2.0-SNAPSHOT"
  :description "Small wrapper around jgit with some handy editing utilities"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure               "1.8.0"]
                 [clj-jgit                          "0.8.9"]
                 [prismatic/schema "1.1.2"]
                 [org.clojure/tools.logging       "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.clojure/data.json        "0.2.6"]]
  :exclusions [commons-logging log4j org.slf4j/slf4j-log4j12]

  :repositories [
                 ["releases" {:url      "https://sforzando.artifactoryonline.com/sforzando/libs-release-local"
                              :username [:env/artifactory_user]
                              :sign-releases false
                              :password [ :env/artifactory_pwd]}]
                 ["plugins" {:url      "https://sforzando.artifactoryonline.com/sforzando/plugins-release"
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
