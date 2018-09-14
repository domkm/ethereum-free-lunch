(ns free-lunch.ui.home.page
  (:require
   [district.ui.component.page :refer [page]]
   [district.ui.graphql.subs :as gql]
   [free-lunch.shared.utils :as shared-utils]
   [free-lunch.ui.contract.free-lunch :as free-lunch]
   [free-lunch.ui.events :as ev]
   [free-lunch.ui.utils :as utils]
   [print.foo :refer [look] :include-macros true]
   [re-frame.core :refer [subscribe dispatch]]
   [react-infinite]
   [reagent.core :as r]))

(def freebies-query
  [:freebies
   [:freebie/id
    :freebie/name]])

(defmethod page :route/home []
  (let [freebies (subscribe [::gql/query
                             {:queries [freebies-query]}
                             {:refetch-on #{::free-lunch/set-freebie-success}}])
        form (r/atom {})
        on-change (fn [k event]
                    (->> event
                      .-target
                      .-value
                      prn
                      )
                    (->> event
                      .-target
                      .-value
                      (swap! form assoc k ))
                    (prn @form))]
    (fn []
      [:div
       [:form {:on-submit (fn [event]
                            (.preventDefault event)
                            (dispatch [::ev/add-freebie @form])
                            (reset! form {}))}
        [:label "ID"
         [:input {:type "text"
                  :value (:freebie/id @form)
                  :on-change #(on-change :freebie/id %)}]]
        [:br]
        [:label "Name"
         [:input {:type "text"
                  :value (:freebie/name @form)
                  :on-change #(on-change :freebie/name %)}]]
        [:br]
        [:label "Street Address"
         [:input {:type "text"
                  :value (:freebie/street-address @form)
                  :on-change #(on-change :freebie/street-address %)}]]
        [:br]
        [:label "Description"
         [:input {:type "text"
                  :value (:freebie/description @form)
                  :on-change #(on-change :freebie/description %)}]]
        [:br]
        [:input {:type "submit" :value "Submit"}]]
       [:ul
        (some->> @freebies
          :freebies
          (map (fn [{:keys [freebie/id freebie/name]}]
                 [:li {:key id}
                  name]))
          doall)]])))
