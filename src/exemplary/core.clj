(ns exemplary.core
  (:require [clojure.string :as str]))

(defn var->test-name-symbol
  "Takes a var and returns a simple-symbol to be used in a deftest name."
  {::example '(= 'var->test-name-symbol-exemplary-test
                 (var->test-name-symbol #'var->test-name-symbol))}
  [var]
  (symbol (str (:name (meta var)) "-exemplary-test")))

(defn process-var!
  "The vars examples will be added to the docstring and clojure.test/deftest calls will be created for you within the current namespace. You can use :exemplary.core/examples with a list/vector of examples or :exemplary.core/example with a single example.

  You must quote your code to ensure it doesn't execute immidiately. So you will have something like one of these in your function params which come after the doc string:

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
    :result)"

  [var]

  (assert (var? var) (str "var required, got " (type var)))

  (let [meta (meta var)
        examples (or (seq (::examples meta))
                     (and (::example meta) [(::example meta)]))]
    (when examples
      (alter-meta!
       var
       (fn [meta]
         (let [meta (dissoc meta ::examples ::example)]
           (update
            meta :doc
            (fn [original]
              (str
               original
               (when-not (empty? original)
                 "\n\n")
               "Examples:\n"
               (->> examples
                    (map (comp #(str " " %) pr-str))
                    (str/join "\n"))))))))

      (require 'clojure.test)
      (binding [*ns* (:ns meta)]
        (eval
         `(clojure.test/deftest
            ^:exemplary.core/test
            ~(var->test-name-symbol var)
            ~@(map
               (fn [example]
                 `(clojure.test/is ~example))
               examples)))))))

(process-var! #'var->test-name-symbol)
