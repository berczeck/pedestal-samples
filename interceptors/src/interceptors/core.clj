(ns interceptors.core
  (:require [interceptors.routes :as service]
            [io.pedestal.http :as http])
  (:gen-class))

(def port 8890)

(def service {::http/port port
              ::http/type :jetty
              ::http/routes service/routes})

(defn create-server []
  (http/create-server service))

(defn start-server []
  (println (str "Server has started in port " port))
  (http/start (create-server)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (start-server))
