(defproject clj-git-lib "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure               "1.8.0"]
                 [clj-jgit                          "0.8.8"]
                 [prismatic/schema                  "0.3.7"]
                 [org.clojure/tools.logging       "0.3.1"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.clojure/data.json        "0.2.6"]
                 ]
  :exclusions [commons-logging log4j org.slf4j/slf4j-log4j12]

  :repositories [
                 ["snapshots" {:url      "https://sforzando.artifactoryonline.com/sforzando/libs-snapshot-local"
                               :username [:gpg :env/artifactory_user]
                               :password [:gpg :env/artifactory_pwd]
                               :snapshots true}]
                 ["releases" {:url      "https://sforzando.artifactoryonline.com/sforzando/libs-release-local"
                              :username [:gpg :env/artifactory_user]
                              :password [:gpg :env/artifactory_pwd]}]
                 ["plugins" {:url      "https://sforzando.artifactoryonline.com/sforzando/plugins-release"
                             :username [:gpg :env/artifactory_user]
                             :password [:gpg :env/artifactory_pwd]}]
                 ]
  )
