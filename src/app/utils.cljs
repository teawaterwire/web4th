(ns app.utils
  (:require 
   [re-frame.core :as rf]))

(def j->c #(js->clj % :keywordize-keys true))

(rf/reg-event-fx
 :set 
 (fn [{db :db } [_ & v]]
   {:db (assoc-in db (butlast v) (last v))}))

(rf/reg-sub
 :get 
 (fn [db [_ & v]]
   (get-in db v)))