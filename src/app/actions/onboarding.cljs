(ns app.actions.onboarding
  (:require 
   [app.actions.entrypoint :as actions]))

(actions/add-label "onboarding" ::onboarding false)

(defn c-onboarding [state]
  [:div.text-center
   [:span.font-bold.text-xl "Welcome to a  "
    [:a.underline {:href "https://mirror.xyz/penseur.eth/mvqOkcw0ABBgRjSD49Pb_V3lbpQMxxi68KCHabYj5RA" :target "_blank"} "web4ᵗʰ"]
    " app!"]
   [:br]
   [:div "You can start a game of 🏓 "
    [:button {:class "btn-blue mt-2 mb-2"
              :on-click #(actions/send :app.actions.pingpong/ping)} "Ping"]]
   [:br]
   [:div "Your username: " (:username state)]
   [:br]
   [:div.italic "Need help? Just start a chat with support 🧇"]])

(defmethod actions/->edn ::onboarding
  [_ _ args]
  {:component "c-onboarding"
   :state {:username (first args)}})

(defmethod actions/->component "c-onboarding" [] c-onboarding)