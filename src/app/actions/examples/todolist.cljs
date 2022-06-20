(ns app.actions.examples.todolist
  (:require 
   [app.actions.entrypoint :as actions]
   [reagent.core :as r]
   [clojure.string :as str]))

(defn c-todos [state]
  (let [items (r/atom (:items state))
        update-item (fn [{:keys [time text]}]
                      (if (str/blank? text)
                        (swap! items dissoc time)
                        (swap! items assoc-in [time :editing?] false)))
        new-todo (r/atom nil)
        add-item (fn [now] 
                   (when-not (str/blank? @new-todo)
                     (swap! items assoc now {:text @new-todo :time now})
                     (reset! new-todo nil)))]
    (fn []
      [:div
       (for [{:keys [text done? time editing?] :as item} (->> (vals @items) (sort-by :time))]
         ^{:key time}
         [:div {:class "flex gap-2 items-center mb-2"}
          [:input {:type "checkbox"
                   :checked done?
                   :on-change #(swap! items assoc-in [time :done?] (not done?))}]
          (if editing?
            [:input {:type "text"
                     :value text
                     :on-change #(swap! items assoc-in [time :text] (.. % -target -value))
                     :onKeyDown #(if (= (.-key %) "Enter") (update-item item))}]
            [:span {:class (if done? "line-through")
                    :on-click #(swap! items assoc-in [time :editing?] true)}
             text])])
       [:input {:type "text"
                :class "w-full rounded-xl bg-transparent"
                :placeholder "Add a todo..."
                :on-change #(reset! new-todo (.. % -target -value))
                :value @new-todo
                :onKeyDown #(if (= (.-key %) "Enter") (add-item (js/Date.now)))}]
       (if (not= (:items state) @items)
         [:<>
          [:br]
          [:button {:class "btn-blue mt-2"
                    :on-click #(actions/send ::todos [@items])}
           "Save current state"]])])))

(defmethod actions/get-action ::todos
  [_ _ args]
  {:component c-todos
   :state {:items (first args)}})

(actions/add-primary-action ::todos "New Todo" {:default? true})