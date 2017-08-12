(ns highloadcup.cache
  (:require [clojure.set :as s]))

(def cache (atom nil))

(defn drop-cache []
  (reset! cache nil))

(defn create-visit-cache [id data]
  (let [{:keys [user location]} data]
    (swap! cache update-in
           [:user-visits user]
           s/union #{id})

    (swap! cache update-in
           [:location-visits location]
           s/union #{id})))

(defn drop-visit-cache [id data]
  (let [{:keys [user location]} data]
    (swap! cache update-in
           [:user-visits user]
           disj id)

    (swap! cache update-in
           [:location-visits location]
           disj id)))

(defn get-user-visits [user-id]
  (get-in @cache [:user-visits user-id]))

(defn get-location-visits [location-id]
  (get-in @cache [:location-visits location-id]))
