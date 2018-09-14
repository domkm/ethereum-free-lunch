(ns free-lunch.ui.contract.free-lunch
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs.spec.alpha :as s]
   [district.ui.logging.events :as logging]
   [district.ui.notification.events :as notification-events]
   [district.ui.smart-contracts.queries :as contract-queries]
   [district.ui.web3-accounts.queries :as account-queries]
   [district.ui.web3-tx.events :as tx-events]
   [district0x.re-frame.spec-interceptors :as spec-interceptors]
   [goog.string :as gstring]
   [print.foo :refer [look] :include-macros true]
   [re-frame.core :as re-frame :refer [reg-event-fx]]))

(def interceptors [re-frame/trim-v])

(re-frame/reg-event-fx
  ::set-freebie
  (fn [{:keys [db]} [_ {:keys [freebie/id]} {:keys [Hash]}]]
    {:dispatch [::tx-events/send-tx
                {:instance (contract-queries/instance db :free-lunch)
                 :fn :set-freebie
                 :args [id Hash]
                 :tx-opts {:from (account-queries/active-account db)
                           :gas 6000000}
                 :tx-id {::set-freebie id}
                 :on-tx-success [::set-freebie-success]
                 :on-tx-hash-error [::logging/error [::set-freebie]]
                 :on-tx-error [::logging/error [::set-freebie]]}]}))

(re-frame/reg-event-fx
  ::set-freebie-success
  (constantly nil))
