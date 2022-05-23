(ns app.magic
  (:require 
   [re-frame.core :as rf]
   [promesa.core :as p]
   [clojure.string :as str]
   [app.utils :refer [j->c]]
   [app.config :refer [env]]
   [app.matrix :as matrix]
   ["ethers" :refer [ethers]]
   ["magic-sdk" :refer [Magic]]
   ["@magic-ext/webauthn" :refer [WebAuthnExtension]]))

(defonce magic (new Magic (:magic-key env)
                (clj->js {:extensions [(new WebAuthnExtension)]})))

(defonce magic-provider (new (.. ethers -providers -Web3Provider) (.-rpcProvider magic)))


(rf/reg-event-fx
 ::init
 (fn [{db :db}]
   {:db (assoc db ::init? true)
    ::init nil}))

(rf/reg-event-fx
 ::login-webauthn
 (fn [{db :db}]
   {::login-webauthn (::username db)}))

(defn matrix-login [username]
  (p/let [metadata-js (.. magic -user (getMetadata))
          {:keys [:publicAddress] :as metadata} (j->c metadata-js)
          signer (.getSigner magic-provider)
          password (.signMessage signer "💩")
          access-token (matrix/matrix-login (str/lower-case publicAddress) password {:magic-username username})]
    (rf/dispatch [:set ::user (assoc metadata :username username :access-token access-token)])))

(defn magic-loging-webauthn [username]
  (->
   (.. magic -webauthn (registerNewUser #js {:username username}))
   (p/catch #(.. magic -webauthn (login #js {:username username})))
   (p/then #(matrix-login username))
   (p/finally
     #(rf/dispatch [:set ::init? false]))))

(rf/reg-fx
 ::login-webauthn
 magic-loging-webauthn)

(rf/reg-fx
 ::init
 (fn []
   (p/let [logged? (.. magic -user (isLoggedIn))]
          (p/do!
           (if logged?
             (p/let [metadata (.. magic -webauthn (getMetadata))]
               (matrix-login (.-username metadata))))
           (rf/dispatch [:set ::init? false])))))

(rf/reg-event-fx
 ::logout
 (fn []
   {::logout nil}))

(rf/reg-fx
 ::logout
 (fn []
   (p/do!
    (.. magic -user (logout))
    (js/location.reload))))

(defn login-panel []
  [:div {:class "flex mt-20 h-3/4"}
   [:input {:class "rounded-l flex-grow"
            :type "text" :placeholder "Enter username"
            :on-change #(rf/dispatch [:set ::username (.. % -target -value)])
            :value @(rf/subscribe [:get ::username])}]
   [:button {:class "btn-blue rounded-l-none" :on-click #(rf/dispatch [::login-webauthn])} "Log in"]])

(defn loading-panel []
  [:div {:class "flex mt-20 h-3/4 items-center"}
   [:div {:class "animate-pulse text-center flex-grow text-9xl"}
    "🧇"]])

(defn main-panel []
  [:div {:class "container mx-auto max-w-xl h-screen py-2 flex flex-col"}
   (cond
     (true? @(rf/subscribe [:get ::init?])) [loading-panel]
     (nil? @(rf/subscribe [:get ::user])) [login-panel]
     :else
     [:<>
      [matrix/chat]
      [:div {:class "text-center text-xs mt-2"}
       @(rf/subscribe [:get ::user :username]) " • "
       [:a {:class "hover:underline text-blue-600 cursor-pointer"
            :on-click #(rf/dispatch [::logout])}
        "Log out"]]])])

