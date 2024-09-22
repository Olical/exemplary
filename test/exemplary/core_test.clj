(ns exemplary.core-test
  (:require [clojure.test :as t]
            [exemplary.core :as exemplary]))

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

(t/deftest var->test-name-symbol
  (t/testing "adds the correct suffix to a vars name and returns it as a symbol"
    (t/is (= 'square-exemplary-test (exemplary/var->test-name-symbol #'square)))))

(defn test-var-fixture
  "Instrument the test fns for use in meta testing."
  [f]
  (exemplary/process-var! #'square)
  (exemplary/process-var! #'half)
  (f))

(t/use-fixtures :once test-var-fixture)

(t/deftest process-var!
  (t/testing "embeds their examples into their doc strings"
    (t/is (= "It squares numbers.\n\nExamples:\n (= 100 (square 10))\n (= 25 (square 5))"
             (:doc (meta #'square))))

    (t/is (= "It halves numbers.\n\nExamples:\n (= 5 (half 10))"
             (:doc (meta #'half)))))

  (t/testing "creates test vars under known names"
    (t/is (var? (resolve 'exemplary.core-test/square-exemplary-test)))
    (t/is (var? (resolve 'exemplary.core-test/half-exemplary-test))))

  (t/testing "we can execute these tests and check the function"
    (t/is (= {:test 1, :pass 2, :fail 0, :error 0, :type :summary}
             (t/run-test-var (resolve 'exemplary.core-test/square-exemplary-test))))
    (t/is (= {:test 1, :pass 1, :fail 0, :error 0, :type :summary}
             (t/run-test-var (resolve 'exemplary.core-test/half-exemplary-test))))))
