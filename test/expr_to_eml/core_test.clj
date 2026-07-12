(ns expr-to-eml.core-test
  (:require [clojure.test :refer :all]
            [expr-to-eml.core :refer :all]))

(defn near= [a b]
  (if (or (Double/isNaN a) (Double/isNaN b))
    false
    (< (Math/abs (- (double a) (double b))) 1e-9)))

(defn pure-eml-tree? [tree allowed-variables]
  (cond
    (= 1 tree) true
    (symbol? tree) (contains? allowed-variables tree)
    (list? tree) (and (= 3 (count tree))
                      (= 'eml (first tree))
                      (pure-eml-tree? (second tree) allowed-variables)
                      (pure-eml-tree? (nth tree 2) allowed-variables))
    :else false))

(deftest eml-arithmetic-test
  (let [env {'a 5.0 'b 2.0}]
    (testing "addition"
      (is (near= 7.0 (evaluate-eml (expr->eml-tree '(+ a b)) env)))
      (is (near= 8.0 (evaluate-eml (expr->eml-tree '(+ a 3)) env)))
      (is (near= 12.0 (evaluate-eml (expr->eml-tree '(+ 10 b)) env)))
      (is (near= 5.0 (evaluate-eml (expr->eml-tree '(+ 0 a)) env)))
      (is (near= 4.0 (evaluate-eml (expr->eml-tree '(+ b b)) env))))
    
    (testing "subtraction"
      (is (near= 3.0 (evaluate-eml (expr->eml-tree '(- a b)) env)))
      (is (near= -5.0 (evaluate-eml (expr->eml-tree '(- a 10)) env)))
      (is (near= -2.0 (evaluate-eml (expr->eml-tree '(- 0 b)) env)))
      (is (near= 95.0 (evaluate-eml (expr->eml-tree '(- 100 a)) env)))
      (is (near= -3.0 (evaluate-eml (expr->eml-tree '(- b a)) env))))

    (testing "multiplication"
      (is (near= 10.0 (evaluate-eml (expr->eml-tree '(* a b)) env)))
      (is (near= 0.0 (evaluate-eml (expr->eml-tree '(* a 0)) env)))
      (is (near= 2.0 (evaluate-eml (expr->eml-tree '(* b 1)) env)))
      (is (near= 12.0 (evaluate-eml (expr->eml-tree '(* 3 4)) env)))
      (is (near= 25.0 (evaluate-eml (expr->eml-tree '(* a a)) env))))

    (testing "division"
      (is (near= 2.5 (evaluate-eml (expr->eml-tree '(/ a b)) env)))
      (is (near= 0.4 (evaluate-eml (expr->eml-tree '(/ b a)) env)))
      (is (near= 5.0 (evaluate-eml (expr->eml-tree '(/ 10 2)) env)))
      (is (near= 5.0 (evaluate-eml (expr->eml-tree '(/ a 1)) env)))
      (is (near= 0.5 (evaluate-eml (expr->eml-tree '(/ 1 b)) env))))

    (testing "unary minus"
      (is (near= -5.0 (evaluate-eml (expr->eml-tree '(- a)) env)))
      (is (near= -2.0 (evaluate-eml (expr->eml-tree '(- b)) env)))
      (is (near= -1.0 (evaluate-eml (expr->eml-tree '(- 1)) env)))
      (is (near= 0.0 (evaluate-eml (expr->eml-tree '(- 0)) env)))
      (is (near= 5.0 (evaluate-eml (expr->eml-tree '(- -5)) env))))

    (testing "constants"
      (is (near= 1.0 (evaluate-eml (expr->eml-tree 1) env)))
      (is (near= 0.0 (evaluate-eml (expr->eml-tree 0) env)))
      (is (near= -1.0 (evaluate-eml (expr->eml-tree -1) env)))
      (is (near= 2.0 (evaluate-eml (expr->eml-tree 2) env)))
      (is (near= 5.0 (evaluate-eml (expr->eml-tree 5) env))))

    (testing "complex expression"
      (is (near= (/ (* 5.0 (+ 5.0 2.0)) (- 5.0 2.0)) (evaluate-eml (expr->eml-tree '(/ (* a (+ a b)) (- a b))) env)))
      (is (near= (+ 5.0 (* 2.0 (- 5.0 1.0))) (evaluate-eml (expr->eml-tree '(+ a (* b (- a 1)))) env)))
      (is (near= (* (+ 5.0 2.0) (- 5.0 2.0)) (evaluate-eml (expr->eml-tree '(* (+ a b) (- a b))) env)))
      (is (near= (/ 1.0 (+ 5.0 2.0)) (evaluate-eml (expr->eml-tree '(/ 1 (+ a b))) env)))
      (is (near= (- (+ 5.0 2.0) (* 5.0 2.0)) (evaluate-eml (expr->eml-tree '(- (+ a b) (* a b))) env))))))

(deftest signed-arithmetic-test
  (doseq [[expr expected] [['(+ -2 1) -1.0]
                           ['(- -2 1) -3.0]
                           ['(* -2 3) -6.0]
                           ['(* -2 -3) 6.0]
                           ['(/ -6 3) -2.0]
                           ['(/ 6 -3) -2.0]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr))))

(deftest variadic-arithmetic-test
  (doseq [[expr expected] [['(+) 0.0]
                           ['(*) 1.0]
                           ['(+ 1 2 3) 6.0]
                           ['(* 2 3 4) 24.0]
                           ['(- 10 2 3) 5.0]
                           ['(/ 24 2 3) 4.0]
                           ['(/ 4) 0.25]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr)))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"requires at least one operand"
                        (expr->eml-tree '(-))))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"requires at least one operand"
                        (expr->eml-tree '(/)))))

(deftest pure-eml-output-test
  (doseq [expr [1/2 -3/4 '(+ x 2/3) '(* -2 y)]]
    (is (pure-eml-tree? (expr->eml-tree expr) '#{x y})
        (str "non-EML terminal remained after compiling " expr)))
  (is (near= 0.5 (evaluate-eml (expr->eml-tree 1/2) {})))
  (is (near= -0.75 (evaluate-eml (expr->eml-tree -3/4) {})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Unsupported expression"
                        (expr->eml-tree 2.5)))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Unsupported expression"
                        (expr->eml-tree {:not "an expression"}))))

(deftest evaluator-validation-test
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Unbound variable"
                        (evaluate-eml (expr->eml-tree '(+ x 1)) {})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Invalid EML tree node"
                        (evaluate-eml '(eml 1 1 1) {}))))

(deftest eml-constants-test
  (is (near= Math/E (evaluate-eml (expr->eml-tree 'e) {})))
  (is (near= Math/PI (evaluate-eml (expr->eml-tree 'pi) {})))
  (let [value (evaluate-eml (expr->eml-tree 'i) {})]
    (is (near= 0.0 (:re value)))
    (is (near= 1.0 (:im value))))
  (doseq [constant ['e 'i 'pi -1 1 2]]
    (is (pure-eml-tree? (expr->eml-tree constant) #{})
        (str "constant did not compile to pure EML: " constant))))

(deftest elementary-unary-functions-test
  (doseq [[expr expected]
          [['(exp 1/2) (Math/exp 0.5)]
           ['(ln 2) (Math/log 2.0)]
           ['(inv 4) 0.25]
           ['(half 3) 1.5]
           ['(minus 2) -2.0]
           ['(sqrt 4) 2.0]
           ['(sqr 3) 9.0]
           ['(sigma 0) 0.5]
           ['(sigmoid 0) 0.5]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr))
    (is (pure-eml-tree? (expr->eml-tree expr) #{})
        (str "function did not compile to pure EML: " expr)))
  (let [value (evaluate-eml (expr->eml-tree '(ln -2)) {})]
    (is (near= (Math/log 2.0) (:re value)))
    (is (near= Math/PI (:im value)))))

(deftest trigonometric-functions-test
  (doseq [[expr expected]
          [['(sin 1/2) (Math/sin 0.5)]
           ['(cos 1/2) (Math/cos 0.5)]
           ['(tan 1/2) (Math/tan 0.5)]
           ['(arcsin 1/2) (Math/asin 0.5)]
           ['(arccos 1/2) (Math/acos 0.5)]
           ['(arctan 1/2) (Math/atan 0.5)]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr))
    (is (pure-eml-tree? (expr->eml-tree expr) #{})
        (str "function did not compile to pure EML: " expr))))

(deftest hyperbolic-functions-test
  (doseq [[expr expected]
          [['(sinh 1/2) (Math/sinh 0.5)]
           ['(cosh 1/2) (Math/cosh 0.5)]
           ['(tanh 1/2) (Math/tanh 0.5)]
           ['(arsinh 1/2) (Math/log (+ 0.5 (Math/sqrt 1.25)))]
           ['(arcosh 2) (Math/log (+ 2.0 (Math/sqrt 3.0)))]
           ['(artanh 1/2) (* 0.5 (Math/log 3.0))]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr))
    (is (pure-eml-tree? (expr->eml-tree expr) #{})
        (str "function did not compile to pure EML: " expr))))

(deftest additional-binary-operators-test
  (doseq [[expr expected]
          [['(pow 2 3) 8.0]
           ['(log 2 8) 3.0]
           ['(avg 2 4) 3.0]
           ['(hypot 3 4) 5.0]]]
    (is (near= expected (evaluate-eml (expr->eml-tree expr) {}))
        (str "failed to evaluate " expr))
    (is (pure-eml-tree? (expr->eml-tree expr) #{})
        (str "operator did not compile to pure EML: " expr))))

(deftest composed-elementary-expression-test
  (let [expr '(+ (sin (/ pi 2))
                 (log 2 (pow 2 3))
                 (hypot 3 4))]
    (is (near= 9.0 (evaluate-eml (expr->eml-tree expr) {})))
    (is (pure-eml-tree? (expr->eml-tree expr) #{})))
  (let [expr '(avg (sqr x) (cosh x))
        tree (expr->eml-tree expr)]
    (is (near= (/ (+ 0.25 (Math/cosh 0.5)) 2.0)
               (evaluate-eml tree {'x 0.5})))
    (is (pure-eml-tree? tree '#{x}))))

(deftest scientific-operator-arity-test
  (doseq [expr ['(sin) '(sqrt 1 2) '(pow 2) '(hypot 3 4 5)]]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"requires exactly"
                          (expr->eml-tree expr))
        (str "invalid arity was accepted: " expr))))
