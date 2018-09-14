(ns free-lunch.ui.events
  (:require
   [cljsjs.buffer]
   [free-lunch.ui.contract.free-lunch :as free-lunch]
   [print.foo :refer [look] :include-macros true]
   [re-frame.core :as re-frame]))

(defn- build-challenge-meta-string [{:keys [comment] :as data}]
  (-> {:comment comment}
    clj->js
    js/JSON.stringify))

;; Adds the challenge to ipfs and if successfull dispatches ::create-challenge
(re-frame/reg-event-fx
  ::add-freebie
  (fn [{:keys [db]} [_ {:keys [] :as data}]]
    (prn "Uploading freebie " data)
    {:ipfs/call {:func "add"
                 :args [(-> data clj->js js/JSON.stringify js/buffer.Buffer.from)]
                 :on-success [::free-lunch/set-freebie data]
                 :on-error [::error]}}))
