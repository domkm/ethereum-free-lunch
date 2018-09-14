(ns free-lunch.server.graphql-resolvers
  (:require
   [bignumber.core :as bn]
   [cljs-time.core :as t]
   [cljs-web3.core :as web3-core]
   [cljs-web3.eth :as web3-eth]
   [cljs.nodejs :as nodejs]
   [clojure.string :as str]
   [clojure.string :as str]
   [clojure.string :as string]
   [district.graphql-utils :as graphql-utils]
   [district.server.config :refer [config]]
   [district.server.db :as db]
   [district.server.web3 :as web3]
   [honeysql.core :as sql]
   [honeysql.helpers :as sqlh]
   [print.foo :refer [look] :include-macros true]
   [taoensso.timbre :as log])
  (:require-macros
   [free-lunch.shared.macros :refer [try-catch-throw]]))

(def resolvers-map
  {:Query {:freebies (fn [& _]
                       (db/all
                         {:select [:*]
                          :from [:freebies]}))}})
