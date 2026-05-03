(ns expr-to-eml.core)

(defn eml [x y]
  (let [x (double x)
        y (double y)]
    (- (Math/exp x) (Math/log y))))

;; Tree construction helpers
(def one 1)

(defn- eml-symbol [] 'eml)

(defn- exp-tree [x] (list (eml-symbol) x one))

(defn- ln-tree [x] 
  (list (eml-symbol) one (list (eml-symbol) (list (eml-symbol) one x) one)))

(defn- sub-tree [x y] 
  (list (eml-symbol) (ln-tree x) (exp-tree y)))

(defn- zero-tree [] (ln-tree one))

(defn- neg-tree [x] (sub-tree (zero-tree) x))

(defn- add-tree [x y] (sub-tree x (neg-tree y)))

(defn- div-tree [x y] 
  (exp-tree (sub-tree (ln-tree x) (ln-tree y))))

(defn- mul-tree [x y] 
  (exp-tree (add-tree (ln-tree x) (ln-tree y))))

(defn- int->tree [n]
  (cond
    (= n 1) one
    (> n 1) (add-tree one (int->tree (dec n)))
    (= n 0) (zero-tree)
    (< n 0) (neg-tree (int->tree (- n)))))

(defn expr->eml-tree [expr]
  (cond
    (integer? expr) (int->tree expr)
    (symbol? expr) expr
    (list? expr)
    (let [[op & args] expr]
      (case op
        + (add-tree (expr->eml-tree (first args)) (expr->eml-tree (second args)))
        - (if (= 1 (count args))
            (neg-tree (expr->eml-tree (first args)))
            (sub-tree (expr->eml-tree (first args)) (expr->eml-tree (second args))))
        * (mul-tree (expr->eml-tree (first args)) (expr->eml-tree (second args)))
        / (div-tree (expr->eml-tree (first args)) (expr->eml-tree (second args)))
        (throw (Exception. (str "Unsupported op: " op)))))
    :else expr))

(defn evaluate-eml [tree env]
  (let [bindings (vec (mapcat (fn [[k v]] [k (double v)]) env))]
    (eval `(let [~@bindings]
             (let [~'eml ~'expr-to-eml.core/eml]
               ~tree)))))

(defn -main [& args]
  (let [expr '(+ a (* b (- a 1)))
        tree (expr->eml-tree expr)
        env {'a 5 'b 2}]
    (println "Expression:" expr)
    (println "EML Tree:" tree)
    (println "Evaluating with a=5, b=2...")
    (println "Result:" (evaluate-eml tree env))
    (println "Expected (Clojure):" (+ 5 (* 2 (- 5 1))))))
