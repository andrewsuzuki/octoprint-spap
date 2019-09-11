(ns api.broadcast
  (:require [cheshire.core :as cheshire]
            [org.httpkit.server :refer [send! websocket?]]
            [api.utils :refer [json-response]]))

;; channel store

(defprotocol ChannelStore
  (add [store ch attach])
  (end [store ch])
  (broadcast [store f]))

(deftype MemoryChannelStore [store-map]
  ChannelStore
  (add [_ ch attach]
    (swap! store-map (fn [old-store]
                       (assoc old-store ch attach))))
  (end [_ ch]
    (swap! store-map (fn [old-store]
                       (dissoc old-store ch))))
  (broadcast [store f]
    (doseq [[ch attach] @store-map]
      (let [body (cheshire/generate-string (f ch attach))]
        (if (websocket? ch)
          (send! ch body false)
          (send! ch (json-response body) true))))))

(defonce channel-store
  (MemoryChannelStore. (atom {})))

;; specific broadcasters

(defn broadcast-cam! [store printer-id cam]
  (let [m {:type :new-cam
           :printer-id printer-id
           :timestamp (-> cam :timestamp .toString)
           :data (:data cam)}]
    (broadcast store (constantly m))))

(defn broadcast-printer! [store printer]
  (let [m {:type :new-printer
           :printer-id (-> printer :id)
           :timestamp (-> printer :timestamp .toString)
           :data printer}]
    (broadcast store (constantly m))))
