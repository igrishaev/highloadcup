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
    (let [url (get-url "/users/new")
          user (assoc new-user
                      :email "new@test.com")
          params (assoc base-params
                        :form-params user
                        :content-type :json)
          res (client/post url params)]
      (is (= (:status res) 200))
      (is (= (:body res) {}))))

  (testing "getting a new user"
    (let [url (get-url (format "/users/%s" (:id new-user)))
          res (client/get url base-params)]
      (is (= (:status res) 200))
      (is (= (:body res)
             (assoc new-user :email "new@test.com")))))

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

      (is (= (-> dates sort vec)
             (-> dates vec)))))

  ;; test vists filters









  )
