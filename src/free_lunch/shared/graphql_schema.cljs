(ns free-lunch.shared.graphql-schema
  (:require-macros
   [free-lunch.shared.macros :refer [slurp-resource]]))

(def graphql-schema
  (slurp-resource "schema.graphql"))
