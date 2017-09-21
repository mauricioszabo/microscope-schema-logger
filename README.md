# Schema Logger
[![Build Status](https://travis-ci.org/acessocard/microscope-schema-logger.svg?branch=master)](https://travis-ci.org/acessocard/microscope-schema-logger)
[![Clojars Project](https://img.shields.io/clojars/v/microscope/schema-logger.svg)](https://clojars.org/microscope/schema-logger)
[![Dependencies Status](https://jarkeeper.com/acessocard/microscope-schema-logger/status.svg)](https://jarkeeper.com/acessocard/microscope-schema-logger)

A logger for microscope that defines a specific schema.

## Why?

When using Kibana or Graylog, logs are persisted in ElasticSearch. Elastic is not really
schemaless - every ES index will try to match the first document it receives, and will try
to coerce future messages to match this schema. This means that if we persist a document
like `{"id": 10}` and, sometime in the future, try to persist `{"id": "foo"}` in the same
index, ES will not allow it. Graylog will warn that an error has ocurred. Kibana (more
precisely, ELK stack) will simply drop the message. In both cases, we'll lose the log.

This project exists to restrict every log to a specific schema. This means that we'll not
be able to log a message that doesn't match our schema - it will throw an exception, and
we'll be forced to fix our logging.

## Usage

The schema logger is, to be precise, a _meta logger:_ it will match our message and our
additional data to a specific schema, and then delegate to a real logger. This means that
we can use schema logger with any other logger that we want - it'll just validate the
schema for us. It accepts some parameters: `:logger`, `:schema` and  `:coercer`.

* `:logger` is the "real" logger generator that will be used. It defaults to default
logger on microscope (that converts our logging info to JSON and then outputs it to
stdout);
* `:schema` is the schema that will be matched. By default, it is
`microscope.schema-logger/LoggerMessage`. To override it, please be sure to extend
`LoggerMessage`, otherwise libs like `microscope-rabbit` will not be able to log their
information. The schema library we use is prismatic-schema.
* `:coercer` is a function that will coerce our data. By default, it just
validates our message, and returns it unmodified if matches. If you want, you can
override this to any coercer: so, for instance, we can make a coercer that will
transform every value to string, and then whatever we log will be persisted as string
and ElasticSearch will be able to coerce anything

```clojure
(ns some-test
  (:require [microscope.core :as components]
            [microscope.logging :as log]
            [schema.core :as s]
            [microscope.schema-logger :as s-logger]))

; Simple usage:
(let [subs (components/subscribe-with :logger (s-logger/generator)
                                      :queue (some/queue :foo))]
  (subs :queue handle-msg))

; With another schema. Let's say we want to log a specific ID in our system...
; Please, notice that :id needs to be optional - our queue, when receives the
; message, has NO IDEA on where to pick up this information to log it.
(def OurSchema (assoc s-logger/LoggerMessage
                      (s/optional-key :id) s/Int))

(let [subs (components/subscribe-with :logger (s-logger/generator {:schema OurSchema})
                                      :queue (some/queue :foo))]
  (subs :queue handle-msg))

; With another logger - we simply pass the logger to our system
(require '[microscope.gelf-logger :as gelf])
(let [subs (components/subscribe-with :logger (s-logger/generator {:logger gelf/logger})
                                      :queue (some/queue :foo))]
  (subs :queue handle-msg))

; And finally, with a coercer
(require '[schema.coerce :as coerce])
(let [coercer (coerce/coercer! OtherSchema {String str})
      subs (components/subscribe-with :logger (s-logger/generator {:coercer coercer})
                                      :queue (some/queue :foo))]
  (subs :queue (fn [msg {:keys [logger]}]
                 ; we can log a integer, and it'll be converted to string.
                 (future/map #(log/info 10) msg))))
```

## MIT License

Copyright 2017 AcessoCard

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
