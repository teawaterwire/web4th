(ns app.core
  (:require 
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [akiroz.re-frame.storage :refer [reg-co-fx!]]
   [app.auth :as auth]
   [app.utils]
   [app.actions.registry]))

(reg-co-fx! :web4th {:fx :store :cofx :store}) 

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [auth/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::init])
  (mount-root))

(rf/reg-event-fx
 ::init
 (fn []
   {:dispatch [:app.auth/init]}))



