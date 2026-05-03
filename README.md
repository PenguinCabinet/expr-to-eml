# expr-to-eml.clj
Clojureで実装した、四則演算のS木をeml関数と1と代数に変換するライブラリです。

This is a Clojure library that converts S-trees of arithmetic operations into eml functions, 1, and algebraic numbers.

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



