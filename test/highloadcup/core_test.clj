(ns highloadcup.core-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [highloadcup.conf :refer [conf]]
            [highloadcup.core :refer [start stop]]))

(defn start-fixture [f]
  (try
    (start)
    (f)
    (finally
      (stop))))

(use-fixtures
  :once
  start-fixture)

(defn get-url [path]
  (format "http://127.0.0.1:%d%s"
          (:server-port conf)
          path))

(def base-params
  {:as :json
   :throw-exceptions false})

(def new-user
  {:id 999999
   :email "test@test.com"
   :first_name "Ivan"
   :last_name "Grishaev"
   :gender "m"
   :birth_date 123123123})

(deftest test-api

  (testing "not found"
    (let [url (get-url "/fofofo")
          res (client/get url base-params)]
      (is (= (:status res) 404))
      (is (= (:body res) ""))))

  (testing "getting a user"
    (let [url (get-url "/users/1")
          res (client/get url base-params)]
      (is (= (:status res) 200))
      (is (= (:body res)
             {:email "wibylcudestiwuk@icloud.com",
              :first_name "Пётр",
              :last_name "Фетатосян",
              :gender "m",
              :birth_date -1720915200,
              :id 1}))))

  (testing "user wrong id"
    (let [url (get-url "/users/aaah")
          res (client/get url base-params)]
      (is (= (:status res) 404))
      (is (= (:body res) ""))))

  (testing "creating a user: ok"
    (let [url (get-url "/users/new")
          user new-user
          params (assoc base-params
                        :query-params {:queryId 999}
                        :form-params user
                        :content-type :json)
          res (client/post url params)]
      (is (= (:status res) 200))
      (is (= (:body res) {}))))

  (testing "creating a user: wrong fields"
    (let [url (get-url "/users/new")
          user (assoc new-user
                      :id 123123123
                      :email 42)
          params (assoc base-params
                        :form-params user
                        :content-type :json)
          res (client/post url params)]
      (is (= (:status res) 400))
      (is (= (:body res) "{}"))))

  (testing "updating a user"
    (let [url (get-url "/users/1")
          user {:email "new@test.com"}
          params (assoc base-params
                        :form-params user
                        :content-type :json)
          res (client/post url params)]
      (is (= (:status res) 200))
      (is (= (:body res) {}))))

  (testing "getting an updated user"
    (Thread/sleep 500) ;; because of send-off
    (let [url (get-url "/users/1")
          res (client/get url base-params)]
      (is (= (:status res) 200))
      (is (= (-> res :body :email)
             "new@test.com"))))

  (testing "getting a visit"
    (let [url (get-url "/visits/1")
          res (client/get url base-params)]
      (is (= (:status res) 200))
      (is (= (:body res)
             {:location 32
              :user 44
              :visited_at 1103485742
              :mark 4
              :id 1}))))

  (testing "getting a visit"
    (let [url (get-url "/visits/1")
          res (client/get url base-params)]
      (is (= (:status res) 200))
      (is (= (:body res)
             {:location 32
              :user 44
              :visited_at 1103485742
              :mark 4
              :id 1}))))

  ;; create/update visits

  (testing "no such visit"
    (let [url (get-url "/locations/92535236453")
          res (client/get url base-params)]
      (is (= (:status res) 404))
      (is (= (:body res) "{}"))))

  ;; create/update locations

  (testing "user visits"
    (let [url (get-url "/users/1/visits")
          res (client/get url base-params)
          visits (-> res :body :visits)
          dates (map :visited_at visits)]

      (is (= (:status res) 200))
      (is (= (count visits) 40))
      (is (= (first visits)
             {:mark 3
              :visited_at 965970299
              :place "Фонарь"}))

      (is (= (last visits)
             {:mark 4
              :visited_at 1418372744
              :place "Забор"}))

      (is (= (-> dates sort vec)
             (-> dates vec)))))

  (testing "user visits: fromDate"
    (let [url (get-url "/users/1/visits")
          params (assoc base-params
                        :query-params {:fromDate 965970299})
          res (client/get url params)
          visits (-> res :body :visits)
          dates (map :visited_at visits)]

      (is (= (:status res) 200))
      (is (= (count visits) 39))
      (is (= (first visits)
             {:mark 4
              :visited_at 969951712
              :place "Гараж"}))))

  (testing "user visits: toDate"
    (let [url (get-url "/users/1/visits")
          params (assoc base-params
                        :query-params {:toDate 1418372744})
          res (client/get url params)
          visits (-> res :body :visits)
          dates (map :visited_at visits)]

      (is (= (:status res) 200)) ;; todo first/last
      (is (= (count visits) 39))))

  (testing "user visits: country"
    (let [url (get-url "/users/1/visits")
          params (assoc base-params
                        :query-params {:country "Австралия"})
          res (client/get url params)
          visits (-> res :body :visits)]

      (is (= (:status res) 200))
      (is (= (count visits) 2))))

  (testing "user visits: toDistance"
    (let [url (get-url "/users/1/visits")
          params (assoc base-params
                        :query-params {:toDistance 10})
          res (client/get url params)
          visits (-> res :body :visits)]

      (is (= (:status res) 200))
      (is (= (count visits) 5))))

  (testing "user visits: wrong params"
    (let [url (get-url "/users/1/visits")
          params (assoc base-params
                        :query-params {:toDistance "arr"})
          res (client/get url params)]

      (is (= (:status res) 400))
      (is (= (:body res) "{}"))))

  (testing "user visits: no such user"
    (let [url (get-url "/users/999919999/visits")
          res (client/get url base-params)]

      (is (= (:status res) 404))
      (is (= (:body res) "{}"))))

  (testing "avg: ok"
    (let [url (get-url "/locations/1/avg")
          res (client/get url base-params)]

      (is (= (:status res) 200))
      (is (= (:body res) {:avg 2.7561}))))

  (testing "avg: no such user"
    (let [url (get-url "/locations/991991991/avg")
          res (client/get url base-params)]

      (is (= (:status res) 404))
      (is (= (:body res) "{}"))))

  (testing "avg: fromDate"
    (let [url (get-url "/locations/1/avg")
          params (assoc base-params
                        :query-params {:fromDate "2147483647"})
          res (client/get url params)]

      (is (= (:status res) 200))
      (is (= (:body res) {:avg 0}))))

  (testing "avg: toDate"
    (let [url (get-url "/locations/1/avg")
          params (assoc base-params
                        :query-params {:toDate "1"})
          res (client/get url params)]

      (is (= (:status res) 200))
      (is (= (:body res) {:avg 0}))))

  (testing "avg: fromAge"
    (let [url (get-url "/locations/1/avg")
          params (assoc base-params
                        :query-params {:fromDate "2147483647"})
          res (client/get url params)]

      (is (= (:status res) 200))
      (is (= (:body res) {:avg 0}))))

  (testing "avg: gender"
    (let [url (get-url "/locations/1/avg")
          params (assoc base-params
                        :query-params {:gender "m"})
          res (client/get url params)]

      (is (= (:status res) 200))
      (is (= (:body res) {:avg 3.04762}))))



  )
