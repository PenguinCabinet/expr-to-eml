(ns expr-to-eml.core)

(defrecord Complex [re im])

(defn- ->complex [x]
  (if (instance? Complex x)
    x
    (->Complex (double x) 0.0)))

(defn- complex-sub [x y]
  (let [x (->complex x)
        y (->complex y)]
    (->Complex (- (:re x) (:re y)) (- (:im x) (:im y)))))

(defn- complex-exp [z]
  (let [{:keys [re im]} (->complex z)
        magnitude (Math/exp re)]
    ;; Avoid Inf * 0 => NaN on the real axis.
    (if (zero? im)
      (->Complex magnitude 0.0)
      (->Complex (* magnitude (Math/cos im))
                 (* magnitude (Math/sin im))))))

(defn- complex-log [z]
  (let [{:keys [re im]} (->complex z)]
    ;; atan2 selects the principal branch, with imaginary part in [-pi, pi].
    (->Complex (Math/log (Math/hypot re im))
               (Math/atan2 im re))))

(defn- eml-complex [x y]
  (complex-sub (complex-exp x) (complex-log y)))

(defn eml [x y]
  "Evaluate exp(x) - log(y) using the principal complex logarithm."
  (let [{:keys [re im] :as result} (eml-complex x y)]
    (if (zero? im) re result)))

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

(declare expr->eml-tree)

(defn- compile-args [args]
  (map expr->eml-tree args))

(defn- compile-add [args]
  (reduce add-tree (zero-tree) (compile-args args)))

(defn- compile-sub [args]
  (case (count args)
    0 (throw (ex-info "'-' requires at least one operand" {:operator '-}))
    1 (neg-tree (expr->eml-tree (first args)))
    (reduce sub-tree (compile-args args))))

(defn- compile-mul [args]
  (reduce mul-tree one (compile-args args)))

(defn- compile-div [args]
  (case (count args)
    0 (throw (ex-info "'/' requires at least one operand" {:operator '/}))
    1 (div-tree one (expr->eml-tree (first args)))
    (reduce div-tree (compile-args args))))

(defn expr->eml-tree [expr]
  (cond
    (integer? expr) (int->tree expr)
    (ratio? expr) (div-tree (int->tree (numerator expr))
                            (int->tree (denominator expr)))
    (symbol? expr) expr
    (list? expr)
    (let [[op & args] expr]
      (case op
        + (compile-add args)
        - (compile-sub args)
        * (compile-mul args)
        / (compile-div args)
        (throw (ex-info (str "Unsupported operator: " op) {:operator op}))))
    :else (throw (ex-info (str "Unsupported expression: " (pr-str expr))
                          {:expression expr}))))

(defn evaluate-eml [tree env]
  (letfn [(evaluate [node]
            (cond
              (= node one) (->complex one)
              (symbol? node) (if (contains? env node)
                               (->complex (get env node))
                               (throw (ex-info (str "Unbound variable: " node)
                                               {:variable node})))
              (and (list? node) (= 'eml (first node)) (= 3 (count node)))
              (eml-complex (evaluate (second node)) (evaluate (nth node 2)))
              :else (throw (ex-info (str "Invalid EML tree node: " (pr-str node))
                                    {:node node}))))]
    (let [{:keys [re im] :as result} (evaluate tree)]
      (if (< (Math/abs im) 1.0e-10)
        re
        result))))

(defn -main [& args]
  (let [expr '(+ a (* b (- a 1)))
        tree (expr->eml-tree expr)
        env {'a 5 'b 2}]
    (println "Expression:" expr)
    (println "EML Tree:" tree)
    (println "Evaluating with a=5, b=2...")
    (println "Result:" (evaluate-eml tree env))
    (println "Expected (Clojure):" (+ 5 (* 2 (- 5 1))))))
