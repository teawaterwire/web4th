(ns app.matrix
  (:require 
   [cljs.reader]
   [promesa.core :as p]
   [re-frame.core :as rf]
   [goog.object]
   [app.utils :refer [j->c]]
   [app.config :refer [env]]
   [app.actions.entrypoint :as actions]
   ["matrix-js-sdk" :as matrix]))

(defonce matrix-client (matrix/createClient (str "https://" (:matrix-domain env))))

(def matrix-bot-id (str "@" (:app-id env) ":" (:matrix-domain env)))

(def matrix-support-id (str "@" (:support-id env) ":" (:matrix-domain env)))

(defn matrix-register [username password]
  (p/let [session-id (-> (.. matrix-client (registerRequest #js {:username username :password password}))
                         (p/catch (fn [resp-js] (.. resp-js -data -session))))]
    (.. matrix-client (register username password session-id #js {:type "m.login.dummy"}))))

(defn matrix-create-room [username]
  (let [alias (str (:app-id env) (subs username 0 10))]
    (p/do!
     (-> (.. matrix-client (createRoom #js {:room_alias_name alias
                                            :visibility "private"
                                            :invite #js [matrix-bot-id]}))
         (p/catch #()))
     (p/let [room-id-js (.. matrix-client (getRoomIdForAlias (str "#" alias ":" (:matrix-domain env))))
             room-id (.-room_id room-id-js)
             joined-js (.. matrix-client (getJoinedRoomMembers room-id))
             support? (some? (goog.object/getValueByKeys joined-js "joined" matrix-support-id))]
       (rf/dispatch [:set ::support? support?])
       room-id))))

(defn matrix-sync [room-id]
  (.. matrix-client (on "Room.timeline"
                        (fn [event room]
                          (if (= room-id (.-roomId room))
                            (rf/dispatch [::add-to-timeline (j->c (.-event event))]))))))

(defn matrix-login [username password {:keys [:magic-username]}]
  (p/let [available? (.. matrix-client (isUsernameAvailable username))
          _ (if available? (matrix-register username password))
          login-resp-js (.. matrix-client (login "m.login.password" #js {:user username :password password}))
          room-id (matrix-create-room username)]
    (matrix-sync room-id)
    (.. matrix-client (startClient #js {:initialSyncLimit 20}))
    (rf/dispatch [:set ::room-id room-id])
    (if available? (actions/send :app.actions.onboarding/onboarding [magic-username]))
    (.-access_token login-resp-js)))

(defn fill-state [state-to-fill db]
  (->> state-to-fill
       (map (fn [[k path-strs]]
              [k (get-in db (map keyword path-strs))]))
       (into {})))

(rf/reg-event-fx
 ::add-to-timeline
 (fn [{db :db} [_ event]]
   (let [{:keys [state-to-fill action state]} (try (cljs.reader/read-string (-> event :content :body)) (catch js/Object _ nil)) 
         state' (merge state (fill-state state-to-fill db))
         event' (merge-with merge event {:content {:action action :state state'}})]
     {:db (update-in db [::timeline] (fn [timeline] (conj (or timeline []) event')))})))

(rf/reg-event-fx
 ::toggle-chat-with-support
 (fn [{db :db}]
   (if (::support? db)
     {::kick {:room-id (::room-id db)
                    :user-id matrix-support-id
                    :on-success [:set ::support? false]}}
     {::invite {:room-id (::room-id db)
                      :user-id matrix-support-id
                      :on-success [:set ::support? true]}})))

(rf/reg-fx
 ::invite
 (fn [{:keys [:room-id :user-id :on-success]}]
   (let [prom (.. matrix-client (invite room-id user-id))]
     (cond-> prom
       (some? on-success) (p/then #(rf/dispatch on-success))))))

(rf/reg-fx
 ::kick
 (fn [{:keys [:room-id :user-id :on-success]}]
   (let [prom (.. matrix-client (kick room-id user-id))]
     (cond-> prom
       (some? on-success) (p/then #(rf/dispatch on-success))))))

(rf/reg-event-fx
 ::send-burp
 (fn [{db :db}]
   {::send {:room-id (::room-id db)
            :burp (::burp db)
            :on-success [:set ::burp nil]}}))

(rf/reg-fx
 ::send
 (fn [{:keys [:room-id :burp :on-success]}]
   (let [content #js {:body burp :msgtype "m.text"}
         prom (.. matrix-client (sendEvent room-id "m.room.message" content))]
     (cond-> prom
       (some? on-success) (p/then #(rf/dispatch on-success))))))

(defn messages []
  [:div {:class "h-4/5 overflow-y-auto pb-4 rounded-t-xl bg-gradient-to-r from-gray-50 to-gray-100 px-4"}
   (for [{:keys [:event_id :content :type :sender :origin_server_ts]} @(rf/subscribe [:get ::timeline])
         :let [{:keys [body state action]} content]
         :when (= type "m.room.message")]
     ^{:key event_id}
     [:div {:class (str "group relative grid my-2"
                        (if (some? action)
                          " place-content-center bg-gradient-to-r from-yellow-50 to-yellow-100 rounded p-3 font-sans"))
            :ref #(when % (.scrollIntoView %))}
      [:div {:class "group-hover:visible invisible z-10 absolute text-xs text-white -bottom-2 text-center w-full"}
       [:span {:class "bg-pink-400 px-1"} (.toLocaleString (new js/Date origin_server_ts))]]
      (if (nil? action)
        [:div
         {:class (str "bg-gradient-to-r p-2 rounded "
                      (if (not= sender matrix-support-id)
                        "text-right place-self-end from-green-100 to-blue-200 ml-16"
                        "place-self-start from-red-100 to-purple-200 mr-16"))}
         body]
        [(:component (actions/get-action action)) state])])])

(defn chat []
  [:<> 
   [messages]
   [:div {:class "rounded-b-xl bg-gradient-to-r from-gray-200 to-gray-300 p-4"}
       [:input {:type "text" :class "w-full rounded-xl"
                :placeholder "Enter text or select action below"
                :on-change #(rf/dispatch [:set ::burp (.. % -target -value)])
                :value @(rf/subscribe [:get ::burp])
                :onKeyDown #(if (= (.-key %) "Enter") (rf/dispatch [::send-burp]))}]
       [:div {:class "py-2 place-content-between flex"}
        [:div
         (for [[label {:keys [action default?]}] @(rf/subscribe [:get :app.actions.entrypoint/label->primary-action])
               :when default?]
           ^{:key label}
           [:button {:class "btn-blue mr-2"
                     :on-click #(actions/send action)} label])]
        [:button {:class "btn-gray"
                  :on-click #(rf/dispatch [::toggle-chat-with-support])}
         (if @(rf/subscribe [:get ::support?])
           "Stop chat with support"
           "Start chat with support")]]]])