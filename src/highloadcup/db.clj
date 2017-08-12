(ns highloadcup.db)

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

(defn get-entity-by-id [entity id]
  (get-in @db [entity id]))

(def get-user-by-id
  (partial get-entity-by-id :users))

(def get-location-by-id
  (partial get-entity-by-id :locations))

(def get-visit-by-id
  (partial get-entity-by-id :visits))

#_(defn get-location-by-id [loc-id]
  (json-response {:location 42}))

#_(defn get-visit-by-id [loc-id]
  (json-response {:visit 42}))

#_(defn create-new-user [request]
  (let [fields (:body request)
        id (:id fields)
        user (assoc fields :id id)]
    (swap! db assoc-in [:users id] user)
    (json-response user)))

#_(defn update-user
  [{body :body} user-id]
  (swap! db update-in [:users user-id] merge body)
  (json-response {}))

#_(defn get-visits [user-id]
  (let [pred (every-pred
              (fn [visit] (-> visit :user (= user-id))))
        visits (filter pred (-> @db :visits vals))]
    (json-response {:visits visits})))

#_(defn location-avg [loc-id]
  (let [pred (every-pred
              (fn [visit] (-> visit :location (= loc-id))))
        visits (filter pred (-> @db :visits vals))
        sum (apply + (map :mark visits))
        cnt (count visits) ;; zero
        result (/ sum cnt) ;; rount to 5

        ]
    (json-response {:avg result}))

  )
