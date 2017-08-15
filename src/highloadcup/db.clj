(ns highloadcup.db
  (:require [highloadcup.conf :refer [conf]]
            [mount.core :as mount]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [hugsql.core :as hugsql]
            [clojure.java.jdbc :as j]))

(declare conn)

(hugsql/def-db-fns "queries.sql")

(defn load-schema []
  (create-users-table conn)
  (create-locations-table conn)
  (create-visits-table conn))

(defn on-start []
  (let [db-uri (:db-url conf)
        spec {:connection-uri db-uri}
        conn (j/get-connection spec)]
    (assoc spec :connection conn)))

(defn on-stop []
  (-> conn :connection .close)
  nil)

(mount/defstate
  ^{:on-reload :noop}
  conn
  :start (on-start)
  :stop (on-stop))

(defn start []
  (mount/start #'conn))

(defn stop []
  (mount/stop #'conn))

(defn get-entity [table id]
  (j/get-by-id conn table id))

(def get-user (partial get-entity :users))
(def get-location (partial get-entity :locations))
(def get-visit (partial get-entity :visits))

(defn create-entity [table fields]
  (j/insert! conn table fields))

(def create-user (partial create-entity :users))
(def create-location (partial create-entity :locations))
(def create-visit (partial create-entity :visits))

(defn update-entity
  [table id fields]
  (j/update! conn table fields
             ["id = ?" id]))

(def update-user (partial update-entity :users))
(def update-location (partial update-entity :locations))
(def update-visit (partial update-entity :visits))

(defn age-to-ts [to-age]
  (c/to-epoch (t/minus (t/now) (t/years to-age))))
