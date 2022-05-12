(ns app.config
  (:require
   [cljs.reader]
   [shadow.resource :as rc]))

(def env (-> (rc/inline "./.env.edn") cljs.reader/read-string))
