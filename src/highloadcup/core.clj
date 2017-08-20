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
  (loader/auto-load))

(defn stop []
  (mount/stop #'conf #'conn #'server))

(defn -main
  [& args]
  (start)
  #_(warmup/run (:warmup-ratio conf)))

;; todo:
;; do not deref transaction
;; java start args
;; make command to load data.zip
;; build in docker
;; logging
;; catch top exc
;; add tests for update visits
;; gc config
;; try sqlite
;; try agents
;; check existance when update
