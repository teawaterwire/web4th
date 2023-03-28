(ns app.actions.onboarding
  (:require 
   [app.actions.entrypoint :as actions]))

(defn c-onboarding []
  [:div.text-center
   [:span.font-bold.text-xl "Welcome to a  "
    [:a.underline {:href "https://mirror.xyz/penseur.eth/mvqOkcw0ABBgRjSD49Pb_V3lbpQMxxi68KCHabYj5RA" :target "_blank"} "web4ᵗʰ"]
    " app!"]
   [:br]
   [:div.text-left.mt-2 
    "You can say "
    [:button {:class "text-blue-500 hover:underline font-bold"
              :on-click #(actions/send :app.actions.examples.hello/hello)} "Hello"]
    ", "  "start a game of "
    [:button {:class "text-blue-500 hover:underline font-bold"
              :on-click #(actions/send :app.actions.examples.pingpong/pong)} "Ping"]
    " or create a "
    [:button {:class "text-blue-500 hover:underline font-bold"
              :on-click #(actions/send :app.actions.examples.todolist/todos)} "Todo list"]]
   [:br]
   [:div.italic.text-left "Want to keep this session? Save the session id found below." [:br] 
    "Need help? Just start a chat with support!"]])

(defmethod actions/get-action ::onboarding
  [_ _ args]
  {:component c-onboarding
   :state {:username (first args)}})