(ns free-lunch.server.core
  (:require
   [cljs-time.core :as t]
   [cljs.nodejs :as nodejs]
   [district.graphql-utils :as graphql-utils]
   [district.server.config :refer [config]]
   [district.server.graphql :as graphql]
   [district.server.graphql.utils :as utils]
   [district.server.logging]
   [district.server.middleware.logging :refer [logging-middlewares]]
   [district.server.web3-watcher]
   [free-lunch.server.db]
   [free-lunch.server.deployer]
   [free-lunch.server.generator]
   [free-lunch.server.graphql-resolvers :refer [resolvers-map]]
   [free-lunch.server.syncer]
   [free-lunch.shared.graphql-schema :refer [graphql-schema]]
   [free-lunch.shared.smart-contracts]
   [mount.core :as mount]
   [taoensso.timbre :refer-macros [info warn error]]))

(nodejs/enable-util-print!)

(defn -main [& _]
  (-> (mount/with-args
        {:config {:default {:web3 {:port 8545}}}
         :smart-contracts {:contracts-var #'free-lunch.shared.smart-contracts/smart-contracts}
         :logging {:level "info"
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
         :web3-watcher {:on-online (fn []
                                     (warn "Ethereum node went online again")
                                     (mount/stop #'free-lunch.server.db/db)
                                     (mount/start #'free-lunch.server.db/db
                                                  #'free-lunch.server.syncer/syncer))
                        :on-offline (fn []
                                      (warn "Ethereum node went offline")
                                      (mount/stop #'free-lunch.server.syncer/syncer))}
         :syncer {:ipfs-config {:host "http://127.0.0.1:5001" :endpoint "/api/v0"}}})
    (mount/except [#'free-lunch.server.deployer/deployer
                   #'free-lunch.server.generator/generator])
    (mount/start))
  (warn "System started" {:config @config}))

(set! *main-cli-fn* -main)

(comment
  (-> (mount/only [#'free-lunch.server.generator/generator])
    mount/stop
    cljs.pprint/pprint)
  (-> (mount/with-args {:generator {:memes/use-accounts 1
                                    :memes/items-per-account 3
                                    :memes/scenarios [:scenario/buy]}})
    (mount/only [#'free-lunch.server.generator/generator])
    mount/start
    cljs.pprint/pprint))
