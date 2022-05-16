(ns app.actions.pingpong
  (:require 
   [app.actions.entrypoint :as actions]))

;; ping

(actions/add-label "Ping" ::ping true)

(defn c-ping [_state]
  [:div.text-center
   "🏓 "
   [:button {:class "btn-blue"
              :on-click #(actions/send ::pong)} "Ping"]])

(defmethod actions/->edn ::ping
  [_action _db _args]
  {:component "c-ping"})

(defmethod actions/->component "c-ping" [] c-ping)

;; pong

(actions/add-label "Pong" ::pong false)

(defn c-pong [_state]
  [:div.text-center
   [:button {:class "btn-blue"
              :on-click #(actions/send ::ping)} "Pong"]
   " 🏓"])

(defmethod actions/->edn ::pong
  [_action _db _args]
  {:component "c-pong"})

(defmethod actions/->component "c-pong" [] c-pong)