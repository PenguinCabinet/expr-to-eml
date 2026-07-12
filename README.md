# expr-to-eml

A Clojure library that compiles mathematical S-expressions into EML trees containing only `eml(x, y) = exp(x) - log(y)`, the constant `1`, and input variables.

Based on [All elementary functions from a single binary operator](https://arxiv.org/abs/2603.21852).

## Installation

### Git dependency

Add the library to the consumer project's `deps.edn`. Replace `COMMIT_SHA` with the SHA of a pushed commit containing this version. Uncommitted or unpushed changes cannot be used as a Git dependency.

```clojure
{:deps
 {io.github.penguincabinet/expr-to-eml
  {:git/url "https://github.com/PenguinCabinet/expr-to-eml.git"
   :git/sha "COMMIT_SHA"}}}
```

### Local checkout

```clojure
{:deps
 {io.github.penguincabinet/expr-to-eml
  {:local/root "../expr-to-eml"}}}
```

## Usage

```clojure
(ns example.core
  (:require [expr-to-eml.core :as eml]))

(def tree
  (eml/expr->eml-tree
   '(+ (sin (/ pi 2)) (hypot 3 4))))

(eml/evaluate-eml tree {})
;; => approximately 6.0
```

Variables are supplied as symbol-to-value bindings:

```clojure
(def variable-tree
  (eml/expr->eml-tree '(avg (sqr x) (cosh x))))

(eml/evaluate-eml variable-tree {'x 1/2})
```

## Public API

- `expr->eml-tree` — compiles an S-expression into a pure EML tree
- `evaluate-eml` — evaluates an EML tree with optional variable bindings

Input expressions accept integers, Clojure ratios such as `1/2`, and variables. Symbols other than reserved constants are treated as input variables. Expressions involving negative numbers are evaluated internally with the principal complex logarithm.

## Supported expressions

```clojure
;; Constants
e i pi

;; Variadic arithmetic
(+ x y) (- x y) (* x y) (/ x y)

;; Basic functions
(exp x) (ln x) (inv x) (half x) (minus x)
(sqrt x) (sqr x) (sigma x)

;; Trigonometric and inverse trigonometric functions
(sin x) (cos x) (tan x)
(arcsin x) (arccos x) (arctan x)

;; Hyperbolic and inverse hyperbolic functions
(sinh x) (cosh x) (tanh x)
(arsinh x) (arcosh x) (artanh x)

;; Other binary operations
(pow x y)   ; x raised to y
(log x y)   ; logarithm of y in base x
(avg x y)   ; (x + y) / 2
(hypot x y) ; sqrt(x^2 + y^2)
```

`sigmoid` is accepted as an alias for `sigma`.

## Development

Run the test suite:

```powershell
clojure -M:test -e "(require 'expr-to-eml.core-test)(clojure.test/run-tests 'expr-to-eml.core-test)"
```

Build a JAR:

```powershell
clojure -T:build jar
```

Install the package into the local Maven repository:

```powershell
clojure -T:build install
```

The locally installed dependency can then be referenced as:

```clojure
{:deps
 {io.github.penguincabinet/expr-to-eml
  {:mvn/version "0.1.0-SNAPSHOT"}}}
```
