(ns highloadcup.loader
  (:require [highloadcup.db :as db]
            [highloadcup.conf :refer [conf]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn read-zip [path]
  (let [zip (java.util.zip.ZipFile. path)
        entries (-> zip .entries enumeration-seq)]
    (for [e entries]
      (.getInputStream zip e))))

(defn read-stream [stream]
  (json/parse-stream (io/reader stream) true))

(defn get-entity [data]
  (-> data keys first))

(defn load-data [entity items]

  (case entity

    :users
    (db/transact
     (for [{id :id :as item} items]
       (-> item
           (dissoc :id)
           (assoc :user/id id))))

    :locations
    (db/transact
     (for [{id :id :as item} items]
       (-> item
           (dissoc :id)
           (assoc :location/id id))))

    :visits
    (db/transact
     (for [{id :id :as item} items]
       (-> item
           (update :user db/get-user-ref)
           (update :location db/get-location-ref)
           (dissoc :id)
           (assoc :visit/id id))))))

(defn load-db [path]
  (doseq [stream (read-zip path)]
    (let [data (read-stream stream)
          entity (get-entity data)
          items (get data entity)]
      (load-data entity items))))

(defn auto-load []
  (load-db (:zip-path conf)))
