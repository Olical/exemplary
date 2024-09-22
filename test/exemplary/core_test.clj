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

(t/deftest var->test-name-symbol
  (t/testing "adds the correct suffix to a vars name and returns it as a symbol"
    (t/is (= 'square-exemplary-test (exemplary/var->test-name-symbol #'square)))))

(defn test-var-fixture
  "Instrument the test fns for use in meta testing."
  [f]
  (alter-meta! (intern 'exemplary.core-test 'example-var 42) merge {:foo true})
  (exemplary/process-var! #'square)
  (exemplary/process-var! #'half)
  (f)
  (create-ns 'exemplary.made-up-namespace)
  (ns-unmap 'exemplary.made-up-namespace 'example-var))

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

(t/deftest test-ns?
  (t/testing "returns true for test namespaces"
    (t/is (not (exemplary/test-ns? 'exemplary.core)))
    (t/is (exemplary/test-ns? 'exemplary.core-test))))

(t/deftest ns->test-ns
  (t/testing "converts a namespace to a test namespace"
    (t/is (= 'exemplary.core-test
             (ns-name
              (exemplary/ns->test-ns
               (the-ns 'exemplary.core)))))))

(t/deftest move-var
  (t/testing "moves a var from one namespace to another and returns the new one, carries over metadata"
    (t/is (nil? (resolve 'exemplary.made-up-namespace/example-var)))
    (exemplary/move-var
     (the-ns 'exemplary.core-test)
     (create-ns 'exemplary.made-up-namespace)
     'example-var)
    (let [var (resolve 'exemplary.made-up-namespace/example-var)]
      (t/is (= 42 (var-get var)))
      (t/is (= {:foo true
                :name 'example-var
                :ns (the-ns 'exemplary.made-up-namespace)}
               (meta var))))))

(t/deftest process-ns!
  (t/testing "calls process-var! on every var in an ns"
    (with-redefs [exemplary/process-var! (spy/spy)]
      (exemplary/process-ns! 'exemplary.core-test)
      (t/is (spy/called-with? exemplary/process-var! #'exemplary.core-test/square))
      (t/is (spy/called-with? exemplary/process-var! #'exemplary.core-test/half)))))
