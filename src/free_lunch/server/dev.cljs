(ns free-lunch.server.dev
  (:require
   [bignumber.core :as bn]
   [camel-snake-kebab.core :as cs :include-macros true]
   [cljs-time.core :as t]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.evm :as web3-evm]
   [cljs.nodejs :as nodejs]
   [cljs.pprint :as pprint]
   [clojure.pprint :refer [print-table]]
   [clojure.string :as str]
   [district.graphql-utils :as graphql-utils]
   [district.server.config :refer [config]]
   [district.server.db :as db]
   [district.server.graphql :as graphql]
   [district.server.graphql.utils :as utils]
   [district.server.logging :refer [logging]]
   [district.server.middleware.logging :refer [logging-middlewares]]
   [district.server.smart-contracts]
   [district.server.web3 :refer [web3]]
   [district.server.web3-watcher]
   [free-lunch.server.db]
   [free-lunch.server.deployer]
   [free-lunch.server.generator]
   [free-lunch.server.graphql-resolvers :refer [resolvers-map]]
   [free-lunch.server.ipfs]
   [free-lunch.server.syncer]
   [free-lunch.shared.graphql-schema :refer [graphql-schema]]
   [free-lunch.shared.smart-contracts]
   [goog.date.Date]
   [graphql-query.core :refer [graphql-query]]
   [mount.core :as mount]
   [print.foo :include-macros true]))

(nodejs/enable-util-print!)

(def graphql-module (nodejs/require "graphql"))
(def parse-graphql (aget graphql-module "parse"))
(def visit (aget graphql-module "visit"))

(defn on-jsload []
  (graphql/restart {:schema (utils/build-schema graphql-schema
                              resolvers-map
                              {:kw->gql-name graphql-utils/kw->gql-name
                               :gql-name->kw graphql-utils/gql-name->kw})
                    :field-resolver (utils/build-default-field-resolver graphql-utils/gql-name->kw)}))

(defn deploy-to-mainnet []
  (mount/stop #'district.server.web3/web3
              #'district.server.smart-contracts/smart-contracts)
  (mount/start-with-args (merge
                           (mount/args)
                           {:web3 {:port 8545}
                            :deployer {:write? true
                                       :gas-price (web3/to-wei 4 :gwei)}})
                         #'district.server.web3/web3
                         #'district.server.smart-contracts/smart-contracts))

(defn redeploy []
  (mount/stop)
  (-> (mount/with-args
        (merge
          (mount/args)
          {:deployer {:write? true}}))
    (mount/start)
    pprint/pprint))

(defn resync []
  (mount/stop #'free-lunch.server.db/db
              #'free-lunch.server.syncer/syncer)
  (-> (mount/start #'free-lunch.server.db/db
                   #'free-lunch.server.syncer/syncer)
      pprint/pprint))

(defn -main [& _]
  (-> (mount/with-args
        {:config {:default {:logging {:level "info"
                                      :console? true}
                            :graphql {:port 6300
                                      :middlewares [logging-middlewares]
                                      :schema (utils/build-schema graphql-schema
                                                resolvers-map
                                                {:kw->gql-name graphql-utils/kw->gql-name
                                                 :gql-name->kw graphql-utils/gql-name->kw})
                                      :field-resolver (utils/build-default-field-resolver graphql-utils/gql-name->kw)
                                      :path "/graphql"
                                      :graphiql true}
                            :web3 {:port 8549}
                            :deployer {}
                            :ipfs {:host "http://127.0.0.1:5001" :endpoint "/api/v0" :gateway "http://127.0.0.1:8080/ipfs"}
                            :smart-contracts {:contracts-var #'free-lunch.shared.smart-contracts/smart-contracts
                                              :print-gas-usage? true
                                              :auto-mining? true}
                            :syncer {:initial-param-query {:meme-registry-db [:max-total-supply
                                                                              :max-auction-duration
                                                                              :deposit]}}}}})
    (mount/except [#'free-lunch.server.deployer/deployer
                   #'free-lunch.server.generator/generator])
    (mount/start)
    pprint/pprint))

(set! *main-cli-fn* -main)

(comment
  (resync))

(comment
  (redeploy))
