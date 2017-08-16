(ns highloadcup.db
  (:require [highloadcup.conf :refer [conf]]
            [mount.core :as mount]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(mount/defstate
  ^{:on-reload :noop}
  conn
  :start (agent {})
  :stop nil)

(defn start []
  (mount/start #'conn))

(defn stop []
  (mount/stop #'conn))

(defn get-entity [entity id]
  (get-in @conn [entity id]))

(defn create-entity [table {id :id :as fields}]
  (send conn assoc-in [table id] fields))

(defn update-entity
  [table id fields]
  (send conn update-in [table id] merge fields))

(def get-user (partial get-entity :users))
(def get-location (partial get-entity :locations))
(def get-visit (partial get-entity :visits))

(def create-user (partial create-entity :users))
(def create-location (partial create-entity :locations))
(def create-visit (partial create-entity :visits))

(def update-user (partial update-entity :users))
(def update-location (partial update-entity :locations))
(def update-visit (partial update-entity :visits))

(defn age-to-ts [to-age]
  (c/to-epoch (t/minus (t/now) (t/years to-age))))

(defn get-user-visits
  [user-id {:keys [fromDate toDate toDistance country]}]

  (let [preds (cond-> []

                  true
                  (conj #(-> % :user (= user-id)))

                  fromDate
                  (conj #(-> % :visited_at (> fromDate)))

                  toDate
                  (conj #(-> % :visited_at (< toDate)))

                  toDistance
                  (conj #(-> % :location get-location :distance (< toDistance)))

                  country
                  (conj #(-> % :location get-location :country (= country))))

        super-pred (apply every-pred preds)

        visits (filter super-pred (-> @conn :visits vals))

        ]

    (sort-by
     :visited_at
     (for [visit visits]
       {:visited_at (:visited_at visit)
        :mark (:mark visit)
        :place (-> visit :location get-location :place)})))
  )


(defn get-location-avg
  [location-id {:keys [fromDate toDate fromAge toAge gender]}]

  (let [preds (cond-> []

                  true
                  (conj #(-> % :location (= location-id)))

                  fromDate
                  (conj #(-> % :visited_at (> fromDate)))

                  toDate
                  (conj #(-> % :visited_at (< toDate)))

                  fromAge
                  (conj #(-> % :user get-user :birth_date (< (age-to-ts fromAge))))

                  toAge
                  (conj #(-> % :user get-user :birth_date (> (age-to-ts toAge))))

                  gender
                  (conj #(-> % :user get-user :gender (= gender))))

        super-pred (apply every-pred preds)

        visits (filter super-pred (-> @conn :visits vals))

        avg (if (empty? visits)
              nil
              (double
               (/ (reduce + (map :mark visits))
                  (count visits))))
        ]

    avg

    )


  )
