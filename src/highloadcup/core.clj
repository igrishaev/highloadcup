(ns highloadcup.core
  (:gen-class)
  (:require [highloadcup.conf :as conf]
            [highloadcup.server :as server]))

(defn -main
  [& args]
  (conf/start)
  (server/start))
