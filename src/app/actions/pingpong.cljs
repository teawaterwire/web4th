(ns app.actions.pingpong
  (:require 
   [app.actions.entrypoint :as actions]))

;; ping

(defn c-ping [_state]
  [:div.text-center
   "🏓 "
   [:button {:class "btn-blue"
              :on-click #(actions/send ::pong)} "Ping"]])

(defmethod actions/->edn ::ping
  [_action _db _args]
  {:action ::ping
   :component c-ping})

(actions/add-primary-action ::ping "Ping" {:default? true})

;; pong

(defn c-pong [_state]
  [:div.text-center
   [:button {:class "btn-blue"
              :on-click #(actions/send ::ping)} "Pong"]
   " 🏓"])

(defmethod actions/->edn ::pong
  [_action _db _args]
  {:action ::pong
   :component c-pong})

(actions/add-primary-action ::pong "Pong")
