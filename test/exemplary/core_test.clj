(ns exemplary.core-test
  (:require [clojure.test :as t]
            [exemplary.core :as exemplary]))

(defn square
  "It squares numbers. The examples here are okay."
  {::exemplary/examples
   '((= 100 (square 10))
     (= 25 (square 5)))}
  [n]
  (* n n))

(defn half
  "It halves numbers, this one is broken deliberately."
  {::exemplary/example
   '(= 5 (half 10))}
  [n]
  (* n 0.51))

(t/deftest var->test-name-symbol
  (t/testing "adds the correct suffix to a vars name and returns it as a symbol"
    (t/is (= 'square-exemplary-test (exemplary/var->test-name-symbol #'square)))))

(defn test-var-fixture
  "Instrument the test fns for use in meta testing.

  Warning! These tests do not work when re-run inside the same namespace multiple times. So kaocha --watch and running tests in the REPL repeatedly will throw errors.

  This is because we need to undefine the test vars created by the library (which intentionally fail!) before the next run which I can't get working in this fixture for some reason.

  Maybe we should just have all tests pass and just check that they run with the counts, then we can leave them defined... that might be better!"
  [f]
  (exemplary/process-var! #'square)
  (exemplary/process-var! #'half)
  (f))

(t/use-fixtures :once test-var-fixture)

(t/deftest process-var!
  (t/testing "embeds their examples into their doc strings"
    (t/is (= "It squares numbers. The examples here are okay.\n\nExamples:\n (= 100 (square 10))\n (= 25 (square 5))"
             (:doc (meta #'square))))

    (t/is (= "It halves numbers, this one is broken deliberately.\n\nExamples:\n (= 5 (half 10))"
             (:doc (meta #'half)))))

  (t/testing "creates test vars under known names"
    (t/is (var? (resolve 'exemplary.core-test/square-exemplary-test)))
    (t/is (var? (resolve 'exemplary.core-test/half-exemplary-test))))

  (t/testing "we can execute these tests and check the function"
    (t/is (t/successful? (t/run-test-var (resolve 'exemplary.core-test/square-exemplary-test))))
    (t/is (not (t/successful? (t/run-test-var (resolve 'exemplary.core-test/half-exemplary-test)))))))
