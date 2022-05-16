(ns app.actions.entrypoint
  (:require [re-frame.core :as rf]))

(defmulti ->edn (fn [action] action))

(defmulti ->component identity)

(defn add-label [label action primary?]
  (rf/dispatch [:set ::label-> label {:action action :primary? primary?}]))

(rf/reg-sub
 ::label->
 :<- [:get ::label->]
 (fn [label->]
   label->))

(defn send [action & [args]]
  (rf/dispatch [::send-action action args]))

(rf/reg-event-fx
 ::send-action
 (fn [{db :db} [_ action args]]
   {:app.matrix/send {:room-id (:app.matrix/room-id db)
                      :burp (js/JSON.stringify (clj->js (->edn action db args)))}}))