(ns app.actions.entrypoint)

(defmulti ->edn (fn [action] action))

(defmulti ->component identity)

