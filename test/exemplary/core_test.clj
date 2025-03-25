(ns exemplary.core-test
  (:require [clojure.test :as t]
            [exemplary.core :as exemplary]
            [spy.core :as spy]))

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

(defn test-var-fixture
  "Instrument the test fns for use in meta testing."
  [f]
  (exemplary/process-var! #'square)
  (exemplary/process-var! #'half)
  (f))

(t/use-fixtures :once test-var-fixture)

(t/deftest process-var!
  (t/testing "embeds their examples into their doc strings"
    (t/is (= "It squares numbers.\n\n```clojure\n(= 100 (square 10))\n(= 25 (square 5))\n```"
             (:doc (meta #'square))))

    (t/is (= "It halves numbers.\n\n```clojure\n(= 5 (half 10))\n```"
             (:doc (meta #'half)))))

  (t/testing "we can execute the vars as tests"
    (t/is (= {:test 1, :pass 2, :fail 0, :error 0, :type :summary}
             (t/run-test-var (resolve 'exemplary.core-test/square))))
    (t/is (= {:test 1, :pass 1, :fail 0, :error 0, :type :summary}
             (t/run-test-var (resolve 'exemplary.core-test/half))))))

(t/deftest process-ns!
  (t/testing "calls process-var! on every var in an ns"
    (with-redefs [exemplary/process-var! (spy/spy)]
      (exemplary/process-ns! 'exemplary.core-test)
      (t/is (spy/called-with? exemplary/process-var! #'exemplary.core-test/square))
      (t/is (spy/called-with? exemplary/process-var! #'exemplary.core-test/half)))))
