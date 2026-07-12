(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'io.github.penguincabinet/expr-to-eml)
(def version (or (System/getenv "VERSION") "0.1.0-SNAPSHOT"))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]
                :pom-data
                [[:description "Compile scientific-calculator expressions to pure EML trees"]
                 [:url "https://github.com/PenguinCabinet/expr-to-eml"]
                 [:scm
                  [:url "https://github.com/PenguinCabinet/expr-to-eml"]
                  [:connection "scm:git:https://github.com/PenguinCabinet/expr-to-eml.git"]
                  [:developerConnection "scm:git:ssh://git@github.com/PenguinCabinet/expr-to-eml.git"]]]})
  (b/copy-dir {:src-dirs ["src"] :target-dir class-dir})
  (b/jar {:class-dir class-dir :jar-file jar-file})
  {:jar-file jar-file :lib lib :version version})

(defn install [_]
  (jar nil)
  (b/install {:basis @basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir})
  {:lib lib :version version})
