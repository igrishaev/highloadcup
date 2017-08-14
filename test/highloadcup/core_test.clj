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









  )
