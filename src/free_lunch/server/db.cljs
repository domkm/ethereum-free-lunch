(ns free-lunch.server.db
  (:require
   [district.server.config :refer [config]]
   [district.server.db :as db]
   [district.server.db.column-types :refer [address not-nil default-nil default-zero default-false sha3-hash primary-key]]
   [district.server.db.honeysql-extensions]
   [honeysql.core :as sql]
   [honeysql.helpers :refer [merge-where merge-order-by merge-left-join defhelper]]
   [medley.core :as medley]
   [mount.core :as mount :refer [defstate]]
   [print.foo :refer [look] :include-macros true]
   [taoensso.timbre :as logging :refer-macros [info warn error]]))

(declare start)
(declare stop)
(defstate ^{:on-reload :noop} db
  :start (start (merge
                  (:db @config)
                  (:db (mount/args))))
  :stop (stop))

(def ipfs-hash (sql/call :char (sql/inline 46)))

(def freebies-columns
  [[:freebie/id :varchar primary-key not-nil]
   [:freebie/name :varchar not-nil]
   [:freebie/street-address :varchar]
   [:freebie/description :varchar not-nil]])

(def freebies-column-names (map first freebies-columns))

(defn start [opts]
  (db/run! {:create-table [:freebies]
            :with-columns [freebies-columns]}))

(defn stop []
  (db/run! {:drop-table [:freebies]}))

(defn create-insert-fn [table-name column-names & [{:keys [:insert-or-replace?]}]]
  (fn [item]
    (let [item (select-keys item column-names)]
      (db/run! {(if insert-or-replace? :insert-or-replace-into :insert-into) table-name
                :columns (keys item)
                :values [(vals item)]}))))

(defn create-update-fn [table-name column-names id-keys]
  (fn [item]
    (let [item (select-keys item column-names)
          id-keys (if (sequential? id-keys) id-keys [id-keys])]
      (db/run! {:update table-name
                :set item
                :where (concat
                         [:and]
                         (for [id-key id-keys]
                           [:= id-key (get item id-key)]))}))))

(defn create-get-fn [table-name id-keys]
  (let [id-keys (if (sequential? id-keys) id-keys [id-keys])]
    (fn [item fields]
      (cond-> (db/get {:select (if (sequential? fields) fields [fields])
                       :from [table-name]
                       :where (concat
                                [:and]
                                (for [id-key id-keys]
                                  [:= id-key (get item id-key)]))})
        (keyword? fields) fields))))

(def insert-freebie! (create-insert-fn :freebies freebies-column-names))
(def insert-or-replace-freebie! (create-insert-fn :freebies freebies-column-names {:insert-or-replace? true}))
(def update-freebie! (create-update-fn :freebies freebies-column-names [:freebie/place-id]))
(def get-freebie (create-get-fn :freebies :freebie/place-id))
