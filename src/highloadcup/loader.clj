(ns highloadcup.loader
  (:require [clojure.string :as str]
            [highloadcup.db :as db]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn read-zip [path]
  (let [zip (java.util.zip.ZipFile. path)
        entries (-> zip .entries enumeration-seq)]
    (for [e entries]
      (.getInputStream zip e))))

(defn read-stream [stream]
  (json/parse-stream (io/reader stream) true))

(defn load-data [entity data]
  (doseq [item (get data entity)]
    (db/create-entity entity item)))

(defn get-entity [data]
  (-> data keys first))

(defn load-db [path]
  (db/drop-db)
  (doseq [stream (read-zip path)]
    (let [data (read-stream stream)
          entity (get-entity data)
          items (get data entity)]
      (doseq [item items]
        (db/create-entity entity item))))
  (db/stats-db))
