(ns microscope.schema-logger
  (:require [schema.core :as s]
            [microscope.logging :as log]))

(def LoggerMessage {:message s/Str
                    :cid s/Str
                    :type (s/enum :info :warning :error :fatal)
                    (s/optional-key :payload) s/Str
                    (s/optional-key :meta) s/Str
                    (s/optional-key :additional-info) s/Str
                    (s/optional-key :exception) s/Str
                    (s/optional-key :backtrace) s/Str})

(defrecord Logger [cid original-logger validate-fn]
  log/Log
  (log [_ message type data]
    (let [normalized-data (-> data
                              (assoc :cid cid :message message :type type)
                              validate-fn)]
      (log/log original-logger (:message normalized-data) type normalized-data))))

(defn generator
  ([] (generator {}))
  ([{:keys [logger schema coercer]
     :or {logger log/default-logger-gen, schema LoggerMessage}}]
   (fn [{:keys [cid] :as params}]
     (->Logger cid (logger params) (or coercer #(s/validate schema %))))))
