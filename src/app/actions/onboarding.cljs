(ns app.actions.onboarding
  (:require 
   [app.actions.entrypoint :as actions]))

(defn c-onboarding [state]
  [:div.text-center
   [:span.font-bold.text-xl "Welcome to a  "
    [:a.underline {:href "https://mirror.xyz/0x7A0F0B39FeA907Deb70A483387Fc6d42fa99adFD/mvqOkcw0ABBgRjSD49Pb_V3lbpQMxxi68KCHabYj5RA" :target "_blank"} "web4ᵗʰ"]
    " app!"]
   [:br]
   [:div "Your username: " (:username state)]
   [:br]
   [:div.italic "Need help? Just start a chat with support 🧇"]])

(defmethod actions/->edn ::onboarding
  [_ _ args]
  {:component "c-onboarding"
   :state {:username (first args)}})

(defmethod actions/->component "c-onboarding" [] c-onboarding)