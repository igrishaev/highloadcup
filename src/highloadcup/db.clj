(ns highloadcup.db
  (:require [highloadcup.time :as time]))

(def db (atom {}))

(swap! db assoc-in [:users 1]
       {:id 1
        :email "johndoe@gmail.com"
        :first_name "John"
        :last_name "Doe"
        :gender "m"
        :birth_date -1613433600})

(swap! db assoc-in [:visits 1]
       {:id 1
        :location 44
        :user 4
        :visited_at 123123123123
        :mark 2})

(swap! db assoc-in [:visits 6]
       {:id 2
        :location 44
        :user 1
        :visited_at 532523423
        :mark 5})

(defn get-entity [entity id]
  (get-in @db [entity id]))

(def get-user
  (partial get-entity :users))

(def get-location
  (partial get-entity :locations))

(def get-visit
  (partial get-entity :visits))

(defn update-entity [entity id data]
  (swap! db update-in [entity id] merge data))

(def update-user
  (partial update-entity :users))

(def update-location
  (partial update-entity :locations))

(def update-visit
  (partial update-entity :visits))

(defn create-entity [entity data]
  (swap! db assoc-in [entity (:id data)] data))

(def create-user (partial create-entity :users))

(def create-location (partial create-entity :locations))

(def create-visit (partial create-entity :visits))

(defn user-visits
  [user-id opt]
  (let [{:keys [fromDate
                toDate
                country
                toDistance]} opt

        user-pred #(-> % :user (= user-id))

        opt-preds [(when fromDate
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
                              :distance (< toDistance)))]

        main-pred (apply every-pred user-pred
                         (remove nil? opt-preds))]

    (filter main-pred (-> @db :visits vals))))

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
