(ns expr-to-eml.core-test
  (:require [clojure.test :refer :all]
            [expr-to-eml.core :refer :all]))

(defn near= [a b]
  (if (or (Double/isNaN a) (Double/isNaN b))
    false
    (< (Math/abs (- (double a) (double b))) 1e-9)))

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
