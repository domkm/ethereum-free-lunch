(ns free-lunch.server.contract.free-lunch
  (:require
   [district.server.smart-contracts :refer [contract-call instance contract-address]]
   [free-lunch.shared.contract.free-lunch :refer [parse-load-freebie]]))

(defn set-freebie [freebie-key freebie-data & [opts]]
  (contract-call :free-lunch :set-freebie freebie-key freebie-data (merge {:gas 100000} opts)))

(defn load-freebie [freebie-key]
  (parse-load-freebie (contract-call :free-lunch :load-freebie freebie-key)))

(defn freebie-event [contract-key & args]
  (apply contract-call contract-key :Freebie args))

