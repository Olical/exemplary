# Exemplary [![Clojars Project](https://img.shields.io/clojars/v/uk.me.oli/exemplary.svg)](https://clojars.org/uk.me.oli/exemplary) [![cljdoc badge](https://cljdoc.org/badge/uk.me.oli/exemplary)](https://cljdoc.org/d/uk.me.oli/exemplary)

## Turns your examples into documentation and runnable tests.

> Very good and suitable to be copied by other people.
>
> &mdash; <cite>[Cambridge English Dictionary][dict-def]</cite>

Exemplary translates examples into function documentation and runnable tests. Test runners like [kaocha][] can automatically re-run these example based tests as you make changes while [cljdoc][] will display your examples with proper Clojure syntax highlighting.

## Usage

```clojure
(ns thing.doer
  (:require [exemplary.core :as ex]))

(defn square
  "It squares numbers."
  {::ex/examples
   '((= 100 (square 10))
     (= 25 (square 5)))}
  [n]
  (* n n))

(defn half
  "It halves numbers."
  {::ex/example
   '(= 5 (half 10))}
  [n]
  (/ n 2))

(ex/process-ns!)

;; Alternatively...
; (ex/process-all-ns! #"^thing\.")
```

The docstrings for these functions will now look like this:

````
-------------------------
thing.doer/square
([n])
  It squares numbers.

```clojure
(= 100 (square 10))
(= 25 (square 5))
```

-------------------------
thing.doer/half
([n])
  It halves numbers.

```clojure
(= 5 (half 10))
```
````

## Instrumentation

You can either add `(ex/process-ns!)` to the bottom of the namespaces you wish to instrument or add a call to `(ex/process-all-ns! #"^my-project\.")` somewhere at the root of your system. This way you can have one instrumentation call that will find all of the examples for you automatically.

You will need to make sure this is re-executed when you make changes if you want the examples to be updated in your running REPL. Executing it once should be enough for your test suite or [cljdoc][] output.

## Kaocha

You may need to tweak your [kaocha][] configuration so it will run tests in namespaces that don't end with `-test`, make sure it's set to `".*"`.

```clojure
{:kaocha/tests
 [{:kaocha/ns-patterns [".*"]}]}
```

## Feedback

I'd love to hear your thoughts, please feel free to reach out to me on [mastodon][] or open a discussion on this repository.

[dict-def]: https://dictionary.cambridge.org/dictionary/english/exemplary
[kaocha]: https://github.com/lambdaisland/kaocha
[malli-dev-inst]: https://github.com/metosin/malli/blob/master/docs/function-schemas.md#development-instrumentation
[malli]: https://github.com/metosin/malli
[mastodon]: https://mastodon.social/@Olical
[cljdoc]: https://cljdoc.org/
