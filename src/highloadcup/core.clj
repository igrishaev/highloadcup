(ns highloadcup.core
  (:gen-class)
  (:require [highloadcup.conf :refer [conf]]
            [highloadcup.db :refer [conn] :as db]
            [highloadcup.server :refer [server]]
            [highloadcup.loader :as loader]
            [highloadcup.warmup :as warmup]
            [mount.core :as mount]))

(defn start []
  (mount/start #'conf #'conn #'server)
  (db/load-schema)
  (loader/auto-load)
  (warmup/run (:warmup-ratio conf)))

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
;; make common functions
;; gc config
