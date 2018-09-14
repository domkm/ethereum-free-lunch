(ns free-lunch.shared.contract.free-lunch
  (:require
   [bignumber.core :as bn]
   [cljs-web3.core :as web3]
   [district.web3-utils :refer [web3-time->local-date-time empty-address? wei->eth-number]]))

(def load-freebie-keys [:freebie/id
                        :freebie/data-hash])

(defn parse-load-freebie [freebie & [{:keys [:parse-dates?]}]]
  (when freebie
    (let [freebie (zipmap load-freebie-keys freebie)]
      (update freebie :freebie/data-hash web3/to-ascii))))
