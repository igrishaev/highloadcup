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

(mount/defstate conn
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

(defn remap-query
  [{args :args :as m}]
  {:query (dissoc m :args)
   :args args})

(defn user-visits [user-id {:keys [fromDate
                                   toDate
                                   country
                                   toDistance]}]
  #_(d/pull (d/db conn) '[{:_user [*]} ] [:user/id user-id])
  #_(d/pull (d/db conn) '[:_visits] [:user/id user-id])

  #_(query '[:find (pull ?e [*])
           :in $ ?user-ref ?fromDate ?toDate ?country ?toDistance
           :where
           [?e :user ?user-ref]

           [?e :visited_at ?visited-at]
           [(> ?visited-at ?fromDate)]
           [(< ?visited-at ?toDate)]

           [?e :location ?location]
           [?location :country ?country]

           [?location :distance ?distance]
           [(< ?distance ?toDistance)]]

         [:user/id user-id]
         111111111
         999999999
         "test"
         9999)

  (cond-> '{:find [(pull ?e [* {:location [*]}])]
            :in [$ ?user-ref]
            :args []
            :where [[?e :user ?user-ref]]}

    (or fromDate toDate)
    (->
     (update :where conj
             '[?e :visited_at ?visited-at]))

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
    (->
     (update :where conj
             '[?e :location ?location]))

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
             '[?e :location ?location]
             '[?location :country ?country]))

    true
    remap-query
)


  #_(query '[:find (pull ?e [* {:location [*]}])
           :in $ ?user-ref ?country ;; ?fromDate ?toDate ?country ?toDistance
           :where
           [?e :user ?user-ref]

           ;; [?e :visited_at ?visited-at]
           ;; [(> ?visited-at ?fromDate)]
           ;; [(< ?visited-at ?toDate)]

           [?e :location ?location]
           [?location :country ?country]

           ;; [?location :distance ?distance]
           ;; [(< ?distance ?toDistance)]
                         ]

         [:user/id 1]
         "Австралия"
         ;; 111111111
         ;; 999999999
         ;; "test"
         ;; 9999
         )

  )


(defn age-to-ts [to-age]
  (c/to-epoch (t/minus (t/now) (t/years to-age))))

(defn location-visits
  [location-id {:keys [fromDate toDate fromAge toAge gender]}]

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
     d/query
)))

;;;;;;;;;;;;;;;;



(def db (atom nil))

(defn drop-db []
  (reset! db {})
  (cache/drop-cache))

(defn stats-db []
  {:users (-> @db :users count)
   :locations (-> @db :locations count)
   :visits (-> @db :visits count)})

#_(defn get-entity [entity id]
  (get-in @db [entity id]))

#_(def get-user
  (partial get-entity :users))

#_(def get-location
  (partial get-entity :locations))

#_(def get-visit
  (partial get-entity :visits))

(defn update-entity [entity id data]
  (swap! db update-in [entity id] merge data)
  (if (= entity :visits)
    (do ;; todo if field is not passed
      (cache/drop-visit-cache id data)
      (cache/create-visit-cache id data))))

(def update-user
  (partial update-entity :users))

(def update-location
  (partial update-entity :locations))

(def update-visit
  (partial update-entity :visits))

(defn create-entity [entity data]
  (let [id (:id data)]
    (swap! db assoc-in [entity id] data)
    (if (= entity :visits)
      (cache/create-visit-cache id data))))

(def create-user (partial create-entity :users))

(def create-location (partial create-entity :locations))

(def create-visit (partial create-entity :visits))

#_(defn user-visits
  [user-id opt]
  (let [{:keys [fromDate
                toDate
                country
                toDistance]} opt

        visit-ids (cache/get-user-visits user-id)
        visits (map get-visit visit-ids)

        opt-preds (remove
                   nil?

                   [(when fromDate
                      #(-> % :visited_at (> fromDate)))

                    (when toDate
                      #(-> % :visited_at (< toDate)))

                    (when country
                      #(some-> %
                               :location get-location
                               :country (= country)))

                    (when toDistance
                      #(some-> %
                               :location get-location
                               :distance (< toDistance)))])

        visits (if (empty? opt-preds)
                 visits
                 (let [pred (apply every-pred opt-preds)]
                   (filter pred visits)))]

    (for [visit visits]
      {:mark (:mark visit)
       :visited_at (:visited_at visit)
       :place (-> visit :location get-location :place)})))

#_(defn location-visits
  [location-id opt]
  (let [{:keys [fromDate
                toDate
                fromAge
                toAge
                gender]} opt

        location-pred #(-> % :location (= location-id))

        opt-preds [(when fromDate
                     #(-> % :visited_at (> fromDate)))

                   (when toDate
                     #(-> % :visited_at (< toDate)))

                   (when fromAge
                     #(some-> %
                              :user
                              get-user :birth_date
                              time/get-age
                              (> fromAge)))

                   (when toAge
                     #(some-> %
                              :user
                              get-user :birth_date
                              time/get-age
                              (< toAge)))

                   (when gender
                     #(some-> %
                              :user
                              get-user :gender
                              (= gender)))]

        main-pred (apply every-pred location-pred
                         (remove nil? opt-preds))]

    (filter main-pred (-> @db :visits vals))))
