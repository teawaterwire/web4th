(ns app.views 
  (:require 
   [re-frame.core :as rf]
   [app.matrix :as matrix]
   [app.actions.index :as actions]))

(defn messages []
  [:div {:class "h-4/5 overflow-y-auto pb-4 rounded-t-xl bg-gradient-to-r from-gray-50 to-gray-100 px-4"}
   (for [{:keys [:event_id :content :type :sender :origin_server_ts]} @(rf/subscribe [:get :app.matrix/timeline])
         :let [{:keys [body component state]} content]
         :when (= type "m.room.message")]
     ^{:key event_id}
     [:div {:class (str "group relative grid my-2"
                        (if (some? component)
                          " place-content-center bg-gradient-to-r from-yellow-50 to-yellow-100 rounded p-3 font-sans"))
            :ref #(when % (.scrollIntoView %))}
      [:div {:class "group-hover:visible invisible z-10 absolute text-xs text-white -bottom-2 text-center w-full"}
       [:span {:class "bg-pink-400 px-1"}	(.toLocaleString (new js/Date origin_server_ts))]]
      (if (nil? component)
        [:div
         {:class (str "bg-gradient-to-r p-2 rounded "
                      (if (not= sender matrix/matrix-support-id)
                        "text-right place-self-end from-green-100 to-blue-200 ml-16"
                        "place-self-start from-red-100 to-purple-200 mr-16"))}
         body]
        [(actions/get-component component) state])])])

(defn login-panel []
  [:div {:class "flex mt-20 h-3/4"}
   [:input {:class "rounded-l flex-grow"
            :type "text" :placeholder "Enter username"
            :on-change #(rf/dispatch [:set ::username (.. % -target -value)])
            :value @(rf/subscribe [:get ::username])}]
   [:button {:class "btn-blue rounded-l-none" :on-click #(rf/dispatch [:app.magic/login-webauthn])} "Log in"]])

(defn loading-panel []
  [:div {:class "flex mt-20 h-3/4 items-center"}
   [:div {:class "animate-pulse text-center flex-grow text-9xl"}
    "🧇"]])

(defn main-panel []
  [:div {:class "container mx-auto max-w-xl h-screen py-2 flex flex-col"}
   (cond
     (true? @(rf/subscribe [:get :app.magic/init?])) [loading-panel]
     (nil? @(rf/subscribe [:get :app.magic/user])) [login-panel]
     :else
     [:<>
      [messages]
      [:div {:class "rounded-b-xl bg-gradient-to-r from-gray-200 to-gray-300 p-4"}
       [:input {:type "text" :class "w-full rounded-xl"
                :placeholder "Enter text or select action below"
                :on-change #(rf/dispatch [:set ::burp (.. % -target -value)])
                :value @(rf/subscribe [:get ::burp])
                :onKeyDown #(if (= (.-key %) "Enter") (rf/dispatch [:app.matrix/send-burp]))}]
       [:div {:class "py-2 place-content-between flex"}
        [:div
         [:button {:class "btn-blue mr-2"
                   :on-click #(rf/dispatch [::send-action ::receive])} "Receive"]
         [:button {:class "btn-blue mr-2 disabled:opacity-50"
                   :on-click #(rf/dispatch [::send-action ::balance])} "Balance"]
         [:button {:class "btn-blue mr-2 disabled:opacity-50"
                   :on-click #(rf/dispatch [::send-action ::send])} "Send"]]
        [:button {:class "btn-gray"
                  :on-click #(rf/dispatch [:app.matrix/toggle-chat-with-support])}
         (if @(rf/subscribe [:get ::support?])
           "Stop chat with support"
           "Start chat with support")]]]
      [:div {:class "text-center text-xs mt-2"}
       @(rf/subscribe [:get :user :username]) " • "
       [:a {:class "hover:underline text-blue-600 cursor-pointer"
            :on-click #(rf/dispatch [::logout])}
        "Log out"]]])])