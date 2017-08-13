(ns highloadcup.core
  (:gen-class)
  (:require [highloadcup.conf :as conf]
            [highloadcup.server :as server]
            [highloadcup.loader :as loader]))

(defn start []
  (conf/start)
  (loader/auto-load)
  (server/start))

(defn stop []
  (conf/stop)
  (server/stop))

(defn -main
  [& args]
  (start))
