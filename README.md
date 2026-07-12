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



