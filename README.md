# Exemplary

> Very good and suitable to be copied by other people.
>
> &mdash; <cite>[Cambridge English Dictionary][dict-def]</cite>

Write real example code in function metadata, share those examples as text in your docstrings and exported [cljdoc][]. As a side effect, tests are created in the equivalent `-test` suffixed namespace to check that your examples continue to work.

Test runners like [kaocha][] can automatically re-run these example based tests as you make changes.

## Usage

```clojure
(ns thing.doer
  (:require [exemplary.core :as exm]))

(defn square
  "It squares numbers."
  {::exemplary/examples
   '((= 100 (square 10))
     (= 25 (square 5)))}
  [n]
  (* n n))

(defn half
  "It halves numbers."
  {::exemplary/example
   '(= 5 (half 10))}
  [n]
  (/ n 2))

(exm/process-ns!)
```

The docstrings for these functions will now look like this:

```clojure
; -------------------------
; exemplary.core-test/square
; ([n])
;   It squares numbers.
;
; Examples:
;  (= 100 (square 10))
;  (= 25 (square 5))

; -------------------------
; exemplary.core-test/half
; ([n])
;   It halves numbers.
;
; Examples:
;  (= 5 (half 10))
```

And we have some new tests defined in `thing.doer-test` that will be picked up and executed by our test runner.

My current suggested workflow is to include a call to `process-ns!` at the bottom of your files that use this library. This is reloaded when you load the file in your REPL or when [kaocha][] detects changes and reloads the file.

This library is still very new and I'd love to hear your thoughts on how we could improve this UX, maybe something like how [malli][]'s [dev instrumentation][malli-dev-inst] works? Please feel free to reach out to me on [mastodon][] or open a discussion here about the topic.

[dict-def]: https://dictionary.cambridge.org/dictionary/english/exemplary
[kaocha]: https://github.com/lambdaisland/kaocha
[cljdoc]: https://cljdoc.org/
[malli-dev-inst]: https://github.com/metosin/malli/blob/master/docs/function-schemas.md#development-instrumentation
[malli]: https://github.com/metosin/malli
[mastodon]: https://mastodon.social/@Olical
