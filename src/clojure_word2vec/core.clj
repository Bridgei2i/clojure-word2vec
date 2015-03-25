(ns clojure-word2vec.core
  (:require [clojure.java.io :as io]
            ;[clojure.core.matrix :as mat]
            ;[clojure.core.matrix.operators :as matop]
            [incanter.stats :as i-stat]
            )
  (:import [com.medallia.word2vec Word2VecTrainerBuilder Word2VecModel]
           [com.medallia.word2vec.thrift Word2VecModelThrift]
           [com.medallia.word2vec.neuralnetwork NeuralNetworkType]
           [com.medallia.word2vec.util Common]))

(defn word2vec
   "Return a trained instance of Word2Vec,
  The first argument is a seq of seqs, where each seq is a list of words.
  The rest of the arguments are hyperparameters used in training the
  neural net."
  ([sentences &
    {:keys [ min-vocab-frequency window-size type layer-size
             use-negative-samples downsampling-rate num-iterations num-threads]
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
  "Takes a text file and creates the input format required
  for training the word2vec model"
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
  "Given a trained word2vec model and a search word,
  it returns 10 (default) words using a distance metric,
  which is Euclidean distance in this case"
  ([model word] (get-matches model word 10))
  ([model word num-matches]
   (let [matches (-> (.forSearch model)
                     (.getMatches word (inc num-matches)))]
     (->> matches
          (mapv #(.match %))
          (remove #{word} )
          (take num-matches)))))

(defn get-relations
  "Given a trained word2vec model, and a relationship between word1 and word2,
  find the closest relationship to word3.
  For example, if Paris is to France, then Berlin is to ?
  -Germany would a probable answer.
  This function returns the top 5 probable answers"
  [model word1 word2 word3]
  (let [inp-words #{word1 word2 word3}
        matches (-> (.forSearch model)
        (.similarity word1 word2)
        (.getMatches word3 8))]
    (->> matches
         (mapv #(.match %))
         (remove inp-words)
         (take 5))))
