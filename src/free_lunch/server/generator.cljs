(ns free-lunch.server.generator
  (:require
   [bignumber.core :as bn]
   [cljs-ipfs-api.files :as ipfs-files]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.evm :as web3-evm]
   [cljs-web3.utils :refer [js->cljkk camel-case]]
   [district.cljs-utils :refer [rand-str]]
   [district.format :as format]
   [district.server.config :refer [config]]
   [district.server.smart-contracts :refer [contract-address contract-call instance]]
   [district.server.web3 :refer [web3]]
   [free-lunch.server.contract.free-lunch :as free-lunch]
   [free-lunch.server.deployer]
   [mount.core :as mount :refer [defstate]]
   [print.foo :refer [look] :include-macros true]
   [taoensso.timbre :as log])
  (:require-macros
   [free-lunch.shared.macros :refer [try-catch]]))

(def fs (js/require "fs"))

(declare start)
(defstate ^{:on-reload :noop} generator :start (start (merge (:generator @config)
                                                        (:generator (mount/args)))))

(defn upload-freebie-data [data]
  (let [json (format/clj->json data)]
    (log/info "Uploading freebie data" {:freebie-data json} ::upload-freebie-data)
    (js/Promise.
      (fn [resolve reject]
        (ipfs-files/add
          (js/Buffer.from json)
          (fn [err {data-hash :Hash}]
            (if err
              (log/error "IPFS error" {:error err} ::upload-freebie-data)
              (do
                (log/info "Uploaded freebie data received " {:data-hash data-hash} ::upload-freebie-data)
                (resolve data-hash)))))))))

(def freebies
  {"ChIJyWEHuEmuEmsRm9hTkapTCrk" #:freebie{:name "Rhythmboat Cruises"
                                           :description "Cruises and shit"}
   "ChIJqwS6fjiuEmsRJAMiOY9MSms" #:freebie{:name "Private Charter Sydney Habour Cruise"
                                           :description "private"}})

(defn generate-freebies [{:keys [accounts]}]
  (log/info "Going to generate" freebies ::generate-freebies)
  (doseq [[k m] freebies]
    (-> m
      (upload-freebie-data)
      (.then (fn [data-hash]
               (try-catch
                 (free-lunch/set-freebie k data-hash {:from (first accounts)})))))))

(defn start [opts]
  (let [opts (assoc opts :accounts (web3-eth/accounts @web3))]
    (generate-freebies opts)))
