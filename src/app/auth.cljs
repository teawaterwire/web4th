(ns app.auth
  (:require
   [promesa.core :as p]
   [clojure.string :as str]
   [re-frame.core :as rf]
   [app.matrix :as matrix]))

(defn create-secret []
  (let [secret (js/crypto.randomUUID)
        username (.slice secret -12)
        password (.slice secret 0 8)]
    (str username password)))

(rf/reg-event-fx
 ::init
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store}]
   {:db (assoc db ::init? true)
    ::init store}))

(defn matrix-login [secret]
  (p/let [username (.slice secret 0 12)
          password (.slice secret -8)
          access-token (matrix/matrix-login username password)]
    (rf/dispatch [::set-user secret access-token])))

(rf/reg-event-fx
 ::set-user
 (fn [{db :db} [_ secret access-token]]
   {:db (assoc db ::user {:secret secret :access-token access-token})
    :store {:secret secret}}))

(rf/reg-fx
 ::init
 (fn [store]
   (p/do!
    (if-let [secret (:secret store)]
      (matrix-login secret))
    (rf/dispatch [:set ::init? false]))))

(rf/reg-event-fx
 ::load-session
 (fn [{db :db}]
   {:dispatch [::create-session (::session-id db)]}))

(rf/reg-event-fx
 ::create-session
 (fn [{db :db} [_ secret]]
   {:db (assoc db ::authenticating? true ::login-error nil)
    ::create-session secret}))

(rf/reg-fx
 ::create-session
 (fn [secret]
   (-> (matrix-login (or secret (create-secret)))
       (p/finally #(rf/dispatch [::create-session-end])))))

(rf/reg-event-fx
 ::create-session-end
 (fn [{db :db}]
   {:db (dissoc db ::init? ::authenticating?)}))

(rf/reg-event-fx
 ::logout
 (fn []
   {:db nil
    :store {}
    ::logout nil}))

(rf/reg-fx
 ::logout
 (fn []
   (js/location.reload)))

(defn login-panel []
  [:<>
   [:div {:class "flex"}
    (if @(rf/subscribe [:get ::authenticating?])
      [:button {:class "animate-pulse btn-gray" :disabled true}
       "Creating session..."]
      [:button {:class "btn-blue disabled:opacity-70"
                :on-click #(rf/dispatch [::create-session])}
       "Create new session"])]
   [:div {:class "mt-2 mb-2"} "OR"]
   [:div {:class "flex"}
    [:input {:class "rounded-l flex-grow"
             :type "text" :placeholder "Enter session id"
             :disabled @(rf/subscribe [:get ::authenticating?])
             :onKeyDown #(if (= (.-key %) "Enter") (rf/dispatch [::load-session]))
             :on-change #(rf/dispatch [:set ::session-id (.. % -target -value)])
             :value @(rf/subscribe [:get ::session-id])}]
    (if @(rf/subscribe [:get ::authenticating?])
      [:button {:class "animate-pulse btn-gray rounded-l-none" :disabled true}
       "Loading session..."]
      [:button {:class "btn-blue rounded-l-none disabled:opacity-70" 
                :disabled (str/blank? @(rf/subscribe [:get ::session-id]))
                :on-click #(rf/dispatch [::load-session])}
       "Load session"])]
   [:div {:class "text-center text-xs mt-2 text-red-600/80"}
       @(rf/subscribe [:get ::login-error])]])

(defn loading-panel []
  [:div {:class "flex items-center"}
   [:div {:class "animate-pulse text-center flex-grow text-9xl"}
    "🧇"]])

(defn main-panel []
  [:div {:class "container mx-auto max-w-xl h-screen py-2 flex flex-col justify-center"}
   (cond
     (true? @(rf/subscribe [:get ::init?])) [loading-panel]
     (nil? @(rf/subscribe [:get ::user])) [login-panel]
     :else
     [:<>
      [matrix/chat]
      [:div {:class "text-center text-xs mt-2"}
       (let [revealed? @(rf/subscribe [:get ::secret-revealed?])]
         [:span 
          [:a {:class "hover:underline text-blue-600 cursor-pointer"
               :on-click #(rf/dispatch [:set ::secret-revealed? (not revealed?)])}
           "Session id"] ": "
          (if revealed? @(rf/subscribe [:get ::user :secret]) "...")])
       @(rf/subscribe [:get ::user :username]) " • "
       [:a {:class "hover:underline text-blue-600 cursor-pointer"
            :on-click #(rf/dispatch [::logout])}
        "Sign out"]]])])