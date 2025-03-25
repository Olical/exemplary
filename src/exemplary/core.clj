(ns exemplary.core
  (:require [clojure.string :as str]
            [clojure.test :as t]))

(defn process-var!
  "The vars examples will be added to the docstring and the var will become executable by clojure.test. You can use :exemplary.core/examples with a list/vector of examples or :exemplary.core/example with a single example.

  When clojure.test/*load-tests* is false the test metadata will be omitted, only the doc string will be updated.

  You must quote your code to ensure it doesn't execute immidiately. So you will have something like one of these in your function params which come after the doc string:

  ```clojure
  (defn foo \"Some info\"
    {:exemplary.core/example
     '(= 10 (+ 5 5))}
    []
    :result)

  (defn bar \"Some info\"
    {:exemplary.core/examples
     '((= 10 (+ 5 5))
       (pos? 3))}
    []
    :result)
  ```"

  [var]

  (assert (var? var) (str "var required, got " (type var)))

  (let [meta (meta var)
        original-ns (:ns meta)
        examples (or (seq (::examples meta))
                     (and (::example meta) [(::example meta)]))]
    (when examples
      (alter-meta!
       var
       (fn [meta]
         (-> (dissoc meta ::examples ::example)
             (update
              :doc
              (fn [original]
                (str
                 original
                 (when-not (empty? original)
                   "\n\n")
                 "```clojure\n"
                 (str/join "\n" (map pr-str examples))
                 "\n```")))
             (cond-> t/*load-tests*
               (assoc
                :test
                (fn []
                  (binding [*ns* original-ns]
                    (run!
                     (fn [example]
                       (t/is (eval example)))
                     examples)))))))))))

(defn process-ns!
  "Runs all vars in a ns through process-var! When given no arguments it will process the current ns."
  ([] (process-ns! *ns*))
  ([ns]
   (doseq [var (vals (ns-interns ns))]
     (when (var? var)
       (process-var! var)))))

(process-ns!)
