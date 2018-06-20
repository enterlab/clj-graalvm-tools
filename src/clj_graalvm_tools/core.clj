(ns clj-graalvm-tools.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]) 
  (:gen-class))

(defn deps-tree-from-folder [project-folder]
  (shell/with-sh-dir project-folder
    (let [{:keys [out]} (shell/sh  "lein" "deps" ":tree-data")] ;; :tree-data new in lein 2.8.1! :)
      (loop [lines (string/split-lines out)]
        (let [line (first lines)]          
          (when line
            (if (= \{ (first line))
              (clojure.edn/read-string (apply str (map string/trim lines)))
              (recur (rest lines)))))))))

(defn unique-deps-from-tree [deps-tree]
  (reduce
   #(cond
      (nil? %2) %1
      (and (vector? %2)
           (-> %2 first symbol?)) (let [[id version] %2]
                                    (conj %1 [(str id) version]))
      (or (vector? %2) (map? %2)) (into %1 (unique-deps-from-tree %2)) 
      :else (do (println "##: " %2) %1)) 
   #{}
   deps-tree))

(defn org-and-artifact [id slash-idx]
  [(subs id 0 slash-idx) (subs id (inc slash-idx) (count id))])

(defn jar-path-from-dep-vec [[id version]]
  (let [dot (string/index-of id ".")
        slash (string/index-of id "/")]
    (cond
      (and dot slash) (let [[org artifact] (org-and-artifact id slash)
                            org-path (string/replace org \. \/)]
                        (format "%s/%s/%s/%s-%s.jar" org-path artifact version artifact version))
      slash (let [[org artifact] (org-and-artifact id slash)]
              (format "%s/%s/%s/%s-%s.jar" org artifact version artifact version))
      :else (format "%s/%s/%s/%s-%s.jar" id id version id version))))

(defn generate-deps-path-lines [unique-deps]
  (apply str
         (interpose " \\\n"
                    (map #(format "    %s" %)
                         (sort
                          (reduce
                           #(conj %1 (jar-path-from-dep-vec %2))
                           []
                           unique-deps))))))

(defn save-deps [deps-tree filename]
  (spit filename
        (let [header "#!/usr/bin/env bash
for archive in \\
    org/clojure/clojure/1.9.0/clojure-1.9.0.jar \\
    org/clojure/core.specs.alpha/0.1.24/core.specs.alpha-0.1.24.jar \\
    org/clojure/spec.alpha/0.1.143/spec.alpha-0.1.143.jar \\
"
              path-lines (-> deps-tree
                             unique-deps-from-tree
                             generate-deps-path-lines)
              footer "; do
    (
      cd target/classes && \\
      jar xf ~/.m2/repository/$archive
    )
done"]
          (format "%s%s%s" header path-lines footer)
          )))

(defn generate-deps-copy-script
  "NB: Doesn't work with -SNAPSHOT dependencies!"
  [project-folder target-script-filename]
  (save-deps
   (deps-tree-from-folder project-folder)
   (format "%s/%s" project-folder target-script-filename)))

;; Example below
#_(generate-deps-copy-script
   "/home/luposlip/meew.ee"
   "cp-deps.sh")

