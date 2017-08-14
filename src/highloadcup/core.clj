(ns highloadcup.core
  (:gen-class)
  (:require [highloadcup.conf :as conf]
            [highloadcup.db :as db]
            [highloadcup.server :as server]
            [highloadcup.loader :as loader]))

(defn start []
  (conf/start)
  (db/start)
  (loader/auto-load)
  (server/start))

(defn stop []
  (conf/stop)
  (db/stop)
  (server/stop))

(defn -main
  [& args]
  (start))
