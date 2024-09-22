(ns exemplary.core
  (:require [clojure.string :as str]))

(defn var->test-name-symbol
  "Takes a var and returns a simple-symbol to be used in a deftest name."
  {::example '(= 'var->test-name-symbol-exemplary-test
                 (var->test-name-symbol #'var->test-name-symbol))}
  [var]
  (symbol (str (:name (meta var)) "-exemplary-test")))

(defn test-ns?
  "Given a namespace, returns true if it ends in -test."
  [ns]
  (str/ends-with? (str ns) "-test"))

(defn ns->test-ns
  "Given a namespace, returns the -test suffixed version of the same namespace."
  [ns]
  (create-ns (symbol (str ns "-test"))))

(defn move-var
  "Moves a var from one ns to another. Also copies all metadata over to the new var but without the :ns key which will be different now."
  [from-ns to-ns var-name]
  (let [var (ns-resolve from-ns var-name)]
    (intern to-ns var-name (var-get var))
    (ns-unmap from-ns var-name)
    (let [moved-var (ns-resolve to-ns var-name)]
      (alter-meta! moved-var merge (dissoc (meta var) :ns))
      moved-var)))

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
      (let [original-ns (:ns meta)
            test-var-name (var->test-name-symbol var)
            test-var
            (binding [*ns* original-ns]
              (eval
               `(clojure.test/deftest
                  ^:exemplary.core/test
                  ~test-var-name
                  ~@(map
                     (fn [example]
                       `(clojure.test/is ~example))
                     examples))))]
        (if (test-ns? original-ns)
          test-var
          (move-var original-ns (ns->test-ns original-ns) test-var-name))))))

(defn process-ns!
  "Runs all vars in a ns through process-var!"
  [ns]
  (doseq [var (vals (ns-interns ns))]
    (when (var? var)
      (process-var! var))))

(defn process-every-ns!
  "Runs all vars in all namespaces through process-var!"
  []
  (doseq [ns (all-ns)]
    (process-ns! ns)))

(time
 (process-every-ns!))
