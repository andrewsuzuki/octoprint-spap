(ns api.config
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]))

(def default-config
  {:port 8080
   :cam-polling-interval 5000
   :octoprint-reconnect-interval 5000
   :printers []})

(defonce conf (atom default-config))

(defn read-config! [file]
  (let [p (-> (slurp file)
              (cheshire/parse-string true)
              (update :printers (fn [printers]
                                  (map
                                   (fn [printer]
                                     ; light validation
                                     (assert (every? #(and (contains? printer %) (not (string/blank? (% printer))))
                                                     #{:display-name :octoprint-address})
                                             "a printer config is missing display-name and/or octoprint-address")
                                     (assert (or (nil? (:cam-address printer))
                                                 (not (string/blank? (:cam-address printer))))
                                             "a printer config has a blank cam-address, either omit or fill it in")
                                     ; assoc unique id
                                     (assoc printer :id (.toString (java.util.UUID/randomUUID))))
                                   printers))))
        c (merge default-config p)]
    ; light validation
    (let [{:keys [port cam-polling-interval octoprint-reconnect-interval printers]} c]
      (assert (and (int? port) (pos? port))
              "port must be positive integer")
      (assert (and (int? cam-polling-interval) (<= 2000 cam-polling-interval))
              "cam-polling-interval must be integer greater or equal to 2000")
      (assert (and (int? octoprint-reconnect-interval) (<= 1000 octoprint-reconnect-interval))
              "octoprint-reconnect-interval must be integer greater or equal to 1000")
      (assert (seq printers)
              "printers array must not be empty"))
    ; swap into conf
    (swap! conf (constantly c))))

(defn get-config [k]
  (get @conf k))
