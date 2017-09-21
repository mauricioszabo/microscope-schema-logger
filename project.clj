(defproject microscope/schema-logger "0.0.1"
  :description "A logger with schema, to avoid name-clash with Kibana/Graylog/ElasticSearch"
  :url "github.com/acessocard/microscope-schema-logger"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [microscope "0.1.5"]
                 [prismatic/schema "1.1.6"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[midje "1.8.3"]]
                   :plugins [[lein-midje "3.2.1"]]}})
