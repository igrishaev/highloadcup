(defproject highloadcup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 ;; [cprop "0.1.10"]
                 ;; [clj-time "0.14.0"]
                 [cheshire "5.7.1"]
                 [compojure "1.6.0"]
                 [mount "0.1.11"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]]

  :main ^:skip-aot highloadcup.core

  :target-path "target/%s"

  :uberjar-name "highloadcup.jar"

  :profiles {:uberjar {:aot :all}})
