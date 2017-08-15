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
             ["where id = ?" id]))

(def update-user (partial update-entity :users))
(def update-location (partial update-entity :locations))
(def update-visit (partial update-entity :visits))


#_(defn user-visits
    [user-id {:keys [fromDate
                     toDate
                     country
                     toDistance]}]

    (cond-> '{:find [?mark ?visited-at ?place]
              :in [$ ?user]
              :args []
              :where [[?v :user ?user]
                      [?v :mark ?mark]
                      [?v :visited_at ?visited-at]
                      [?v :location ?location-id]
                      [?location :location/id ?location-id]
                      [?location :place ?place]]}

      true
      (update :args conj
              (d/db conn)
              user-id)

      fromDate
      (->
       (update :in conj '?fromDate)
       (update :args conj fromDate)
       (update :where conj
               '[(> ?visited-at ?fromDate)]))

      toDate
      (->
       (update :in conj '?toDate)
       (update :args conj toDate)
       (update :where conj
               '[(< ?visited-at ?toDate)]))

      toDistance
      (->
       (update :in conj '?toDistance)
       (update :args conj toDistance)
       (update :where conj
               '[?location :distance ?distance]
               '[(< ?distance ?toDistance)]))

      country
      (->
       (update :in conj '?country)
       (update :args conj country)
       (update :where conj
               '[?location :country ?country]))

      true
      (->
       remap-query
       d/query)))

#_(defn age-to-ts [to-age]
    (c/to-epoch (t/minus (t/now) (t/years to-age))))

#_(defn location-avg
    [location-id {:keys [fromDate
                         toDate
                         fromAge
                         toAge
                         gender]}]

    (cond-> '{:find [(avg ?mark) .]
              :with [?v]
              :in [$ ?location-id]
              :args []
              :where [[?v :location ?location-id]
                      [?v :mark ?mark]]}

      true
      (update :args conj
              (d/db conn)
              location-id)

      (or fromDate toDate)
      (update :where conj
              '[?v :visited_at ?visited-at])

      fromDate
      (->
       (update :in conj '?fromDate)
       (update :args conj fromDate)
       (update :where conj
               '[(> ?visited-at ?fromDate)]))

      toDate
      (->
       (update :in conj '?toDate)
       (update :args conj toDate)
       (update :where conj
               '[(< ?visited-at ?toDate)]))

      (or fromAge toAge gender)
      (update :where conj
              '[?v :user ?user-id]
              '[?user :user/id ?user-id])

      (or fromAge toAge)
      (update :where conj
              '[?user :birth_date ?birth-date])

      fromAge
      (->
       (update :in conj '?fromAge)
       (update :args conj (age-to-ts fromAge))
       (update :where conj
               '[(< ?birth-date ?fromAge)]))

      toAge
      (->
       (update :in conj '?toAge)
       (update :args conj (age-to-ts toAge))
       (update :where conj
               '[(> ?birth-date ?toAge)]))

      gender
      (->
       (update :in conj '?gender)
       (update :args conj gender)
       (update :where conj
               '[?user :gender ?gender]))

      true
      (->
       remap-query
       d/query)))
