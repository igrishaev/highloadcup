(ns highloadcup.db
  (:require [highloadcup.time :as time]
            [highloadcup.conf :refer [conf]]
            [mount.core :as mount]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.java.io :as io]
            [datomic.api :as d]
            [highloadcup.cache :as cache])) ;; cache

(declare conn)

(defn read-edn
  [filename]
  (-> filename
      io/resource
      slurp
      read-string))

(defn transact
  [data]
  @(d/transact conn data))

(defn load-schema []
  (-> "schema.edn" read-edn transact))

(defn on-start []
  (let [db-url (:db-url conf)]
    (d/create-database db-url)
    (d/connect db-url)))

(defn on-stop []
  (let [db-url (:db-url conf)]
    (d/delete-database db-url)
    ;; close conn
    nil))

(mount/defstate
  ^{:on-reload :noop}
  conn
  :start (on-start)
  :stop (on-stop))

(defn start []
  (mount/start #'conn))

(defn stop []
  (mount/stop #'conn))

(defn query [q & args]
  (apply d/q q (d/db conn) args))

(defn get-entity [entity id]
  (let [pk (keyword entity "id")
        ref [pk id]
        e (d/pull (d/db conn) '[*] ref)]
    (when-let [id (pk e)]
      (-> e
          (dissoc pk :db/id)
          (assoc :id id)))))

(def get-user (partial get-entity "user"))

(def get-location (partial get-entity "location"))

(def get-visit (partial get-entity "visit"))

(defn get-entity-ref [entity id]
  [(keyword (name entity) "id") id])

(def get-user-ref (partial get-entity-ref :user))

(def get-location-ref (partial get-entity-ref :location))

(defn ->datum
  [entity {id :id :as fields}]
  (case entity

    :user
    (-> fields
        (dissoc :id)
        (assoc :user/id id))

    :location
    (-> fields
        (dissoc :id)
        (assoc :location/id id))

    :visit
    (-> fields
        (dissoc :id)
        (assoc :visit/id id)
        (cond->
          :user
          (update :user get-user-ref)
          :location
          (update :location get-location-ref)))))

(defn upsert-entity
  [entity fields]
  (let [datum (->datum entity fields)]
    (transact [datum])))

(defn remap-query
  [{args :args :as m}]
  {:query (dissoc m :args)
   :args args})

(defn user-visits
  [user-id {:keys [fromDate
                   toDate
                   country
                   toDistance]}]

  (cond-> '{:find [(pull ?v [* {:location [*]}])]
            :in [$ ?user]
            :args []
            :where [[?v :user ?user]]}

    true
    (update :args conj
            (d/db conn)
            [:user/id user-id]) ;; check resolve

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

    (or toDistance country)
    (update :where conj
            '[?v :location ?location])

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
             '[?v :location ?location]
             '[?location :country ?country]))

    true
    (->
     remap-query
     d/query)))

(defn age-to-ts [to-age]
  (c/to-epoch (t/minus (t/now) (t/years to-age))))

(defn location-avg
  [location-id {:keys [fromDate
                       toDate
                       fromAge
                       toAge
                       gender]}]

  (cond-> '{:find [(avg ?mark) .]
            :in [$ ?location]
            :args []
            :where [[?v :location ?location]
                    [?v :mark ?mark]]}

    true
    (update :args conj
            (d/db conn)
            [:location/id location-id]) ;; check resolve

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
     (update :in conj '?toDistance)
     (update :args conj toDate)
     (update :where conj
             '[(< ?visited-at ?fromDate)]))

    (or toAge gender)
    (update :where conj
            '[?v :user ?user])

    gender
    (->
     (update :in conj '?gender)
     (update :args conj gender)
     (update :where conj
             '[?user :gender ?gender]))

    toAge
    (->
     (update :in conj '?toTimestamp)
     (update :args conj (age-to-ts toAge))
     (update :where conj
             '[?user :birth_date ?birth-date]
             '[(< ?birth-date ?toTimestamp)]))

    true
    (->
     remap-query
     d/query)))
