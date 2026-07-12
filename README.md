# expr-to-eml.clj
Clojureで実装した、四則演算のS木をeml関数と1と代数に変換するライブラリです。

This is a Clojure library that converts S-trees of arithmetic operations into eml functions, 1, and algebraic numbers.

整数・有理数・変数からなる四則演算式に対応し、負数を含む式は主値複素対数を使って内部評価します。
変換結果の終端は `1` と入力変数だけです。浮動小数点リテラルは正確な純粋EML表現ではないため受け付けません。

[https://arxiv.org/pdf/2603.21852](https://arxiv.org/pdf/2603.21852)

## 実行例
```
clojure -M -m expr-to-eml.core
```

```
Expression: (+ a (* b (- a 1)))
EML Tree: (eml (eml 1 (eml (eml 1 a) 1)) (eml (eml (eml 1 (eml (eml 1 (eml 1 (eml (eml 1 1) 1))) 1)) (eml (eml (eml (eml 1 (eml (eml 1 (eml 1 (eml (eml 1 b) 1))) 1)) (eml (eml (eml 1 (eml (eml 1 (eml 1 (eml (eml 1 1) 1))) 1)) (eml (eml 1 (eml (eml 1 (eml (eml 1 (eml (eml 1 a) 1)) (eml 1 1))) 1)) 1)) 1)) 1) 1)) 1))
Evaluating with a=5, b=2...
Result: 13.0
Expected (Clojure): 13
````

## 対応する式

論文 Table 1 の科学計算機プリミティブをS式から純粋EMLツリーへ変換します。

```clojure
;; 定数
e i pi -1 1 2

;; 四則演算（可変長）
(+ x y) (- x y) (* x y) (/ x y)

;; 基本単項関数
(exp x) (ln x) (inv x) (half x) (minus x)
(sqrt x) (sqr x) (sigma x)

;; 三角関数・逆三角関数
(sin x) (cos x) (tan x)
(arcsin x) (arccos x) (arctan x)

;; 双曲線関数・逆双曲線関数
(sinh x) (cosh x) (tanh x)
(arsinh x) (arcosh x) (artanh x)

;; 二項演算
(pow x y)   ; x^y
(log x y)   ; log_x(y): xを底とするyの対数
(avg x y)   ; (x+y)/2
(hypot x y) ; sqrt(x^2+y^2)
```

`sigmoid` は `sigma` の別名としても使用できます。`x`、`y` など、予約済み定数以外のシンボルは入力変数として扱われます。



