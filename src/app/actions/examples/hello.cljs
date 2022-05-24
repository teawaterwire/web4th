(ns app.actions.examples.hello
  (:require
   [app.actions.entrypoint :as actions]))

(defn c-hello []
  [:div.text-center "gm 🌞"])

(defmethod actions/get-action ::hello
  []
  {:component c-hello})

(actions/add-primary-action ::hello "Hello" {:default? true})