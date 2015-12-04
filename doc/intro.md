
# Introduction to clojure-word2vec

Problem statement:
When we want to classify or cluster data, the first step is to create a representation of data, usually called the Feature Vector. Datasets consisting of images or audio files have feature vectors that are already in numeric form. If we have text data, we have to convert words /characters into numbers. 

For a number of years, the Bag of words approach was used to create a Feature Vector. This approach required the use of a dictionary which contains all the words used in the dataset.

Assume that we have a dictionary consisting of the words {"the", "sleepy","happy","cat","dog"}. If we encounter 2 sentences :"the sleepy cat" and "the happy dog", we replace the words with the index in the dictionary. Thus "the sleepy cat" becomes "0,1,3", and "the happy dog" translates to "0,2,5" .

However, the bag of words representation does not place similar words together (in a vector space model).  
Word2Vec is a tool developed by Mikolov et al, improves on this by learning high-dimension representation in a vector space model, which places similar words together. 


We'll use a dataset (consists of forum postings on Apple products) to try out the capabilities of word2vec.
We read the Apple dataset and train a word2vec model on the data. This is a modified version
of the Apple dataset, which can be downloaded [here] (http://times.cs.uiuc.edu/~wang296/Data/).

```clojure
(def appvec
  (-> (read-filtered-dataset "resources/apple-data.txt.gz") word2vec))
```

Let's view the top 20 words in the vocabulary

```clojure
(take 20 (.getVocab appvec))
```

```
("ipod" "drive" "problem" "computer" "itune" "apple" "disc" 
"nano" "song" "thank" "mac" "os" "screen" "card" "time" 
"system" "music" "g5" "display" "file")
```

The total number of words  in the vocabulary 

```clojure
(.getVocabSize (.toThrift appvec))
```
    6813


Given that word2vec transforms a word into a high dimension vector, we can
compute the closeness of 2 words by comparing the corresponding word vectors
using a distance metric like 
[cosine similarity](http://en.wikipedia.org/wiki/Cosine_similarity)

To calculate the distance between 2 words, *g5* and *ipod*,
we fetch the raw word vectors and compute distance using
the [Incanter](http://Incanter.org) cosine-similarity API.

```clojure
(defn cosine-sim
  [model word1 word2]
  (let [rawvecfn  #(.getRawVector (.forSearch model) %)

(cosine-sim appvec "g5" "ipod")

```
The resulting score is a value between 0 and 1, where a high score indicates
that the words lie close to each other in the vector space model.
```
0.9988300697889931
```

##  Some examples of the relations API:
In the original paper, the example offered was:
"if Paris is related to France, Berlin is related to ?"
and the query would find Germany as the answer.
In the Apple dataset (as with any other dataset),
the relationship found are usually noisy. Lets look at some
good answers

If Nano is a 'kind of' ipod, (The Ipod Nano was a bestselling model of the Ipod line)
then [g3](http://en.wikipedia.org/wiki/Power_Macintosh_G3_%28Blue_%26_White%29) is a

```clojure
(get-relations appvec "nano" "ipod"  "g3")
```
    ("mac" "imac" "ibook" "installation" "system")

If ghz is a measure of speed, then gb is a measure of

```clojure
(get-relations appvec "ghz" "speed" "gb")
```
    ("data" "hd" "backup" "size" "cache")

We'd expect *memory* to be the right answer, but *data*, 
the 1th item on the list, is a reasonable approximation.

If 300gb is the measure of a drive, then 2ghz is a

```clojure
(get-relations appvec "300gb" "drive" "2ghz")
```
    ("imac" "system" "processor" "upgrade" "model")
measure of a processor (3nd item)

Airport is a product line for wifi basestations, and extreme is 
one of the products in that line, (a wifi base station made by apple)
what's a model in the ipod line?

```clojure
(get-relations appvec "airport" "extreme" "ipod")
```
    ("nano" "content" "library" "music" "shuffle")
The *nano* (1nd item)

---
There's a lot of noise in the answers however.
When we query for a [G5](http://en.wikipedia.org/wiki/Power_Mac_G5) 
instead (a desktop computer)

```clojure
(get-relations appvec "nano" "ipod"  "g5")
```
    ("speed" "raid" "quad" "model" "performance")
    
we don't find a desktop in the top 5 answers.

---

We can use the *get-matches* API to return the words
that are closest (by euclidean distance) to the queried word.
Here are a few examples

Radeon is a video card
```clojure
(get-matches appvec "radeon")
```
    ("card" "ati" "dual" "g5" "agp" "nvidia" "ghz" "graphic" "pcie" "model")
The top few answers suggest that it is a card, and the manufacturer is ATI.

```clojure
(get-matches appvec "seagate")
```
    ("maxtor" "gb" "raid" "drive" "quad" "speed" "raptor" "performance" "digital" "enclosure")

```clojure
(get-matches appvec "nano")
```
    ("ipod" "gen" "track" "music" "shuffle" "content" "itune" "library" "play" "ipods")



---
## Conclusion

* Word2vec is an excellent tool to find co-occurances of words in a corpus. Depending on the kind of data, it may be possible to determine relationships as well.
* We used the Apple dataset as it has content that was annotated with Part of Speech tags (such as nouns, verbs). For this exercise, we only used words that were nouns or were part of noun phrases. 
* Word2vec can tell us what is being discussed *about* something. We can see from the relations API that customers talking about Seagate (A hard disk manufacturer) are concerned about sizes, speed, performance and enclosures.

