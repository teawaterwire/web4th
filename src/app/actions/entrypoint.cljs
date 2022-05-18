(ns app.actions.entrypoint
  (:require [re-frame.core :as rf]))

(defmulti ->edn (fn [action] action))

(defn add-primary-action [action label & [{:keys [default?]}]]
  (rf/dispatch [:set ::label->primary-action label {:action action :default? default?}]))

(defn send [action & [args]]
  (rf/dispatch [::send-action action args]))

(rf/reg-event-fx
 ::send-action
 (fn [{db :db} [_ action args]]
   (let [burp (select-keys (->edn action db args) [:action :state])]
     {:app.matrix/send {:room-id (:app.matrix/room-id db)
                        :burp (pr-str burp)}})))