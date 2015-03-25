(ns clojure-word2vec.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.core.matrix :as mat]
            [clojure.core.matrix.operators :as matop]
            [incanter.stats :as i-stat]
            )
  (:import [com.medallia.word2vec Word2VecTrainerBuilder Word2VecModel]
           [com.medallia.word2vec.thrift Word2VecModelThrift]
           [com.medallia.word2vec.neuralnetwork NeuralNetworkType]
           [com.medallia.word2vec.util Common]))

(defn word2vec
   "Return a trained instance of Word2Vec"
  ([sentences &
    {:keys [ min-vocab-frequency window-size type layer-size
             use-negative-samples downsampling-rate num-iterations num-threads]
           ; layerSize windowSize numThreads type negativeSamples useHierarchicalSoftmax vocab minFrequency
           ;  initialLearningRate downSampleRate iterations listener]
     :or { min-vocab-frequency 5
           window-size 8
           type NeuralNetworkType/CBOW
           layer-size 5
           use-negative-samples 25
           downsampling-rate 1e-5
           num-iterations 100
          num-threads (.availableProcessors (Runtime/getRuntime))}
     }]
     (let [bldr (doto (Word2VecModel/trainer )
       (.setMinVocabFrequency min-vocab-frequency)
       (.useNumThreads num-threads))]
       (.train bldr sentences)
       )))

(defn create-input-format
  [inpfile]
  (let [f (io/file (io/resource inpfile))]
    (if (not (.exists f))
      (throw (IllegalStateException. (str "Please download " inpfile " and place it in the resources folder ")))
      (let [data  (Common/readToList f)]
        (->> data
             ;split the string and return a seq instead of Array of strings
             (map #(seq (.split #" " % )))
             ;remove empty strings
             (map #(remove empty? %))
             ;remove collections that are empty
             (remove empty?)
             )))))

(defn get-matches
  ([model word] (get-matches model word 10))
  ([model word num-matches]
   (let [matches (-> (.forSearch model)
                     (.getMatches word (inc num-matches)))]
     (->> matches
          (mapv #(.match %))
          (remove #{word} )
          (take num-matches)))))

(defn read-filtered-dataset
  [inpfile]
  (with-open [r (clojure.java.io/reader inpfile)]
    (mapv edn/read-string (line-seq r))))

(defn get-relations
  [model word1 word2 word3]
  (let [inp-words #{word1 word2 word3}
        matches (-> (.forSearch model)
        (.similarity word1 word2)
        (.getMatches word3 8))]
    (->> matches
         (mapv #(.match %))
         (remove inp-words)
         (take 5))))

;    (remove inp-words
;            (mapv #(.match %)
;        (.getMatches (.similarity (.forSearch model) word1 word2) word3 5)))

(comment
(def d2 (create-input-format "sherlock_holmes_canon.txt"))
(def appdata (read-filtered-dataset "resources/apple-data.txt"))
(def d4
  (-> d2
    word2vec
     ; (.toThrift)
      ))
(.getVocab d4)
(def srch (.forSearch d4))

(.getMatches srch "Yard" 5)
(def t1 (slurp "resources/apple-data"))

(def appvec
  (-> appdata word2vec))

(take 50 (.getVocab appvec))

(defn get-matches
  [vecobj word]
  (mapv #(.match %) (.getMatches (.forSearch vecobj) word 10)))

(get-matches appvec "powermac")
(get-matches appvec "g5")

(def appsrch (.forSearch appvec))

(let [rawvecfn  #(.getRawVector appsrch %)
      [a1 a2] (map rawvecfn ["g5" "ipod"])]
  (i-stat/cosine-similarity a1 a2))

(def k
  (let [rawvecfn  #(mat/array (.getRawVector appsrch %))]
       (mapv rawvecfn ["firewire" "usb" "tiger"])))
(apply min (k 0))
(matop/+ (k 0) (k 1))

(drop 300 (take 400 (.getVocab appvec)))
(.getVocabSize (.toThrift appvec))
(.getVectors (.toThrift appvec))

(defn get-relations
  [word1 word2 word3]
  (mapv #(.match %)
        (.getMatches (.similarity appsrch word1 word2) word3 5)))

(get-relations "agp" "video" "ipod")
(get-relations "osx" "leopard" "ati")
(get-relations "firewire" "usb" "sata")
(get-relations "ghz" "speed" "mb")
(get-relations "ipod" "nano" "g3")
(get-relations "upgrade" "g3" "new")
(get-relations "g5" "leopard" "g3")

(get-matches appvec "seagate")
(get-matches appvec "nano")
(get-matches appvec "quicktime")
)
