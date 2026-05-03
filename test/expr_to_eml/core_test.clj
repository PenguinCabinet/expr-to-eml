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
      (let [expr '(+ a b)
            tree (expr->eml-tree expr)]
        (is (near= (+ 5.0 2.0) (evaluate-eml tree env)))))
    
    (testing "subtraction"
      (let [expr '(- a b)
            tree (expr->eml-tree expr)]
        (is (near= (- 5.0 2.0) (evaluate-eml tree env)))))

    (testing "multiplication"
      (let [expr '(* a b)
            tree (expr->eml-tree expr)]
        (is (near= (* 5.0 2.0) (evaluate-eml tree env)))))

    (testing "division"
      (let [expr '(/ a b)
            tree (expr->eml-tree expr)]
        (is (near= (/ 5.0 2.0) (evaluate-eml tree env)))))

    (testing "complex expression"
      (let [expr '(/ (* a (+ a b)) (- a b))
            tree (expr->eml-tree expr)]
        (is (near= (/ (* 5.0 (+ 5.0 2.0)) (- 5.0 2.0)) (evaluate-eml tree env)))))

    (testing "unary minus"
      (let [expr '(- a)
            tree (expr->eml-tree expr)]
        (is (near= -5.0 (evaluate-eml tree env)))))

    (testing "constants"
      (let [expr '(+ a 1)
            tree (expr->eml-tree expr)]
        (is (near= 6.0 (evaluate-eml tree env)))))
    ))
