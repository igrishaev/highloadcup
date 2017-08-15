(defproject highloadcup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [cprop "0.1.10"]
                 [org.xerial/sqlite-jdbc "3.20.0"]
                 [org.clojure/java.jdbc "0.7.0"]
                 [com.layerware/hugsql "0.4.7"]
                 [cheshire "5.7.1"]
                 [compojure "1.6.0"]
                 [mount "0.1.11"]
                 [clj-http "3.5.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]]

  :main ^:skip-aot highloadcup.core

  :target-path "target/%s"

  :uberjar-name "highloadcup.jar"

  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["env/dev/resources"]}
             :test {:resource-paths ["env/dev/resources"]}})
