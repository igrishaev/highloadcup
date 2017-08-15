(ns highloadcup.core
  (:gen-class)
  (:require [highloadcup.conf :refer [conf]]
            [highloadcup.db :refer [conn] :as db]
            [highloadcup.server :refer [server]]
            [highloadcup.loader :as loader]
            [mount.core :as mount]))

(defn start []
  (mount/start #'conf #'conn #'server)
  (db/load-schema)
  (loader/auto-load))

(defn stop []
  (mount/stop #'conf #'conn #'server))

(defn -main
  [& args]
  (start))

;; todo:
;; java start args
;; make command to load data.zip
;; build in docker
;; logging
;; catch top exc
;; add tests for update visits
;; try openjdk
;; gc config
;; warmup
