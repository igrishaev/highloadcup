(ns highloadcup.conf
  (:require [highloadcup.spec :as spec]
            [mount.core :as mount]
            [cprop.source :refer [from-resource
                                  from-env]])
  (:import java.util.MissingResourceException))

(defn read-config []
  (merge
   (try
     (from-resource)
     (catch MissingResourceException e))
   (from-env)))

(defn- on-start []
  (let [config (read-config)]
    (if-let [config (spec/validate :cfg/params config)]
      config
      (System/exit 1))))

(mount/defstate
  ^{:on-reload :noop}
  conf
  :start (on-start)
  :stop nil)

(defn start []
  (mount/start #'conf))

(defn stop []
  (mount/stop #'conf))
