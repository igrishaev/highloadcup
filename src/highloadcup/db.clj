(ns highloadcup.db
  (:require [highloadcup.time :as time]
            [clojure.java.io :as io]
            [datomic.api :as d]
            [highloadcup.cache :as cache]))

(def db-uri "datomic:mem://highloadcup")

(d/delete-database db-uri)

(d/create-database db-uri)

(def conn (d/connect db-uri))

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

(defn query [q & args]
  (apply d/q q (d/db conn) args))



;;;;;;;;;;;;;;;;





(def db (atom nil))

(defn drop-db []
  (reset! db {})
  (cache/drop-cache))

(defn stats-db []
  {:users (-> @db :users count)
   :locations (-> @db :locations count)
   :visits (-> @db :visits count)})

(defn get-entity [entity id]
  (get-in @db [entity id]))

(def get-user
  (partial get-entity :users))

(def get-location
  (partial get-entity :locations))

(def get-visit
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

(defn user-visits
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

(defn location-visits
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
