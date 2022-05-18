(ns app.actions.entrypoint
  (:require [re-frame.core :as rf]))

(defmulti get-action (fn [action] action))

(defn add-primary-action [action label & [{:keys [default?]}]]
  (rf/dispatch [:set ::label->primary-action label {:action action :default? default?}]))

(defn send [action & [args]]
  (rf/dispatch [::send-action action args]))

(rf/reg-event-fx
 ::send-action
 (fn [{db :db} [_ action args]]
   (let [burp (merge {:action action} (select-keys (get-action action db args) [:state]))]
     {:app.matrix/send {:room-id (:app.matrix/room-id db)
                        :burp (pr-str burp)}})))