(ns app.core
  (:require 
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [app.magic :as magic]
   [app.utils]
   [app.actions.registry]))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [magic/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::init])
  (mount-root))

(rf/reg-event-fx
 ::init
 (fn []
   {:dispatch [:app.magic/init]}))

