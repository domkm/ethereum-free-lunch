(ns free-lunch.server.syncer
  (:require
   [bignumber.core :as bn]
   [camel-snake-kebab.core :as cs :include-macros true]
   [cljs-ipfs-api.files :as ifiles]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [district.server.config :refer [config]]
   [district.server.smart-contracts :as smart-contracts :refer [replay-past-events]]
   [district.server.web3 :refer [web3]]
   [district.web3-utils :as web3-utils]
   [free-lunch.server.contract.free-lunch :as free-lunch]
   [free-lunch.server.db :as db]
   [free-lunch.server.deployer]
   [free-lunch.server.generator]
   [mount.core :as mount :refer [defstate]]
   [print.foo :refer [look] :include-macros true]
   [taoensso.timbre :as log])
  (:require-macros
   [free-lunch.shared.macros :refer [try-catch]]))

(declare start)
(declare stop)
(defstate ^{:on-reload :noop} syncer
  :start (start (merge
                  (:syncer @config)
                  (:syncer (mount/args))))
  :stop (stop syncer))

(def info-text "smart-contract event")
(def error-text "smart-contract event error")

(defn get-ipfs-data [data-hash & [default]]
  (js/Promise.
    (fn [resolve reject]
      (log/info (str "Downloading: " "/ipfs/" data-hash) ::get-ipfs-data)
      (ifiles/fget (str "/ipfs/" data-hash)
        {:req-opts {:compress false}}
        (fn [err content]
          (try
            (if (and
                  (not err)
                  (not-empty content))
              ;; Get returns the entire content, this include CIDv0+more meta+data
              ;; TODO add better way of parsing get return
              (-> (re-find #".+(\{.+\})" content)
                second
                js/JSON.parse
                (js->clj :keywordize-keys true)
                resolve)
              (throw (js/Error. (str (or err "Error") " when downloading " "/ipfs/" data-hash ))))
            (catch :default e
              (log/error error-text {:error (ex-message e)} ::get-meme-data)
              (when goog.DEBUG
                (resolve default)))))))))

(defn on-freebie [_ event]
  (let [freebie (-> event
                  :args
                  vals
                  first
                  free-lunch/load-freebie)]
    (-> freebie
      :freebie/data-hash
      get-ipfs-data ;; TODO deal with removal
      (.then (fn [data err]
               (reduce-kv
                 (fn [m k v]
                   (assoc m (keyword :freebie k) v))
                 {:freebie/id (:freebie/id freebie)} ; TODO necessary?
                 data)))
      (.then db/insert-or-replace-freebie!))))

(defn start [{:keys [:initial-param-query] :as opts}]
  (when-not (web3/connected? @web3)
    (throw (js/Error. "Can't connect to Ethereum node")))
  [(-> (free-lunch/freebie-event :free-lunch {} {:from-block 0 :to-block "latest"})
     (replay-past-events on-freebie))
   (free-lunch/freebie-event :free-lunch {} "latest" on-freebie)])

(defn stop [syncer]
  (doseq [filter (remove nil? @syncer)]
    (web3-eth/stop-watching! filter (fn [err]))))
