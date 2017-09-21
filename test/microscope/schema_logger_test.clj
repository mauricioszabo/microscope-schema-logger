(ns microscope.schema-logger-test
  (:require [schema.core :as s]
            [midje.sweet :refer :all]
            [microscope.io :as io]
            [microscope.logging :as log]
            [microscope.schema-logger :as s-log]))

(def OtherSchema (assoc s-log/LoggerMessage :some-id s/Int))
(facts "about logger"
  (fact "will create a JSON logger with default schema"
    (let [logger ((s-log/generator) {:cid "FOO"})]
      (io/deserialize-msg (with-out-str (log/info logger "Something")))
      => {:message "Something" :type "info" :cid "FOO"}

      (log/info logger "FooBar" :invalid "data") => (throws Exception)))

  (fact "allows to override logger"
    (let [logger ((s-log/generator {:logger ..logger-gen..}) {:cid "FOO"})]
      (log/info logger "Something"))
    => "LOGGED"
    (provided
     (..logger-gen.. {:cid "FOO"}) => ..logger..
     (log/log ..logger.. "Something" :info
              {:cid "FOO" :type :info :message "Something"}) => "LOGGED"))

  (fact "allows to override schema"
    (let [logger ((s-log/generator {:schema OtherSchema}) {:cid "FOO"})]
      (io/deserialize-msg (with-out-str (log/info logger "Something" :some-id 10)))
      => {:message "Something" :type "info" :cid "FOO" :some-id 10})))
