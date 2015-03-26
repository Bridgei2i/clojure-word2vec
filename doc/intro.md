# Introduction to clojure-word2vec


let's read the apple dataset and train a word2vec model on the data

```clojure
(def appvec
  (-> (read-filtered-dataset "resources/apple-data.txt.gz") word2vec))
```

see the top view words in the vocabulary

```clojure
(take 20 (.getVocab appvec))
```

the total number of words  in the vocabulary

```clojure
(.getVocabSize (.toThrift appvec))
```
calculate the cosine similarity between 2 words
this is done by fetching the raw word vectors and using
incanter's cosine-similarity API

```clojure
(defn cosine-sim
  [model word1 word2]
  (let [rawvecfn  #(.getRawVector (.forSearch model) %)
```
calculate

```clojure
(cosine-sim appvec "g5" "ipod")
```

Some examples of the relations API
in the original paper, the example offered was
if Paris is related to France, Berlin is related to ?
and the query would find Germany as the answer.
in the Apple dataset (as with any other dataset),
the relationship found are usually noisy. Lets look at some
good answers

if nano is a 'kind of' ipod, then g3 is a

```clojure
(get-relations appvec "nano" "ipod"  "g3")
```

an ibook

when we query for a G5 instead (a desktop computer)

```clojure
(get-relations appvec "nano" "ipod"  "g5")
```

we don't find a desktop in the top 5 answers

if ghz is a measure of speed, then gb is a measure of

```clojure
(get-relations appvec "ghz" "speed" "gb")
```

data, the 4th item on the list

if 300gb is the measure of a drive, then 2ghz is a

```clojure
(get-relations appvec "300gb" "drive" "2ghz")
```

measure of a processor (2nd item)

sub-type of a product :airport-extreme (a wifi base station made by apple)
what's an ipod's type

```clojure
(get-relations appvec "airport" "extreme" "ipod")
```
nano (2nd item)

we can use the get-matches API to return the words
that are closest (by euclidean distance) to the argument

```clojure
(get-matches appvec "radeon")
```

```clojure
(get-matches appvec "seagate")
```

```clojure
(get-matches appvec "nano")
```

```clojure
(get-matches appvec "projector")
```

```clojure
(get-matches appvec "raid")
```

```clojure
(get-matches appvec "quicktime")
```

```clojure
(get-matches appvec "powermac")
```
