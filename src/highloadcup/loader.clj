(ns highloadcup.loader
  (:require [clojure.string :as str]
            [highloadcup.db :as db]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn is-json? [file]
  (-> file .getName (.endsWith ".json")))

(defn get-json-files [path]
  (filter is-json? (file-seq (io/file path))))

(defn read-json [file]
  (json/parse-stream (io/reader file) true))

(defn load-data [entity data]
  (doseq [item (get data entity)]
    (db/create-entity entity item)))

(defn get-entity [file]
  (-> file .getName (str/split #"_") first keyword))

(defn load-db [path]
  (db/drop-db)
  (let [files (get-json-files path)]
    (doseq [file files]
      (let [entity (get-entity file)
            data (read-json file)]
        (load-data entity data))))
  (db/stats-db))
