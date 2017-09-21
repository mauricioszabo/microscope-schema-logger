(ns microscope.schema-logger
  (:require [schema.core :as s]
            [microscope.logging :as log]))

(def LoggerMessage {:message s/Str
                    :cid s/Str
                    :type (s/enum :info :warning :error :fatal)
                    (s/optional-key :payload) s/Str
                    (s/optional-key :meta) s/Str
                    (s/optional-key :exception) s/Str
                    (s/optional-key :backtrace) s/Str})

(defrecord Logger [cid original-logger-gen validate-fn]
  log/Log
  (log [_ message type data]
    (let [normalized-data (-> data
                              (assoc :cid cid :message message :type type)
                              validate-fn)
          original-logger (original-logger-gen {:cid cid})]
      (log/log original-logger message type normalized-data))))

(defn generator
  ([] (generator {}))
  ([{:keys [logger schema]
     :or {logger log/default-logger-gen, schema LoggerMessage}}]
   (fn [{:keys [mocked cid]}]
     (if mocked
       (->Logger cid #(logger (assoc % :mocked true)) #(s/validate schema %))
       (->Logger cid logger #(s/validate schema %))))))
