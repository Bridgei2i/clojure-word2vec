# clojure-word2vec

The [word2vec tool](http://code.google.com/p/word2vec/)  by Mikolov et al enables us to 
create word vectors from a dataset containing text data. Unlike a binary present/absent representation
used by a bag-of-words, these word vectors can be used to compare 2 words and see if they are related.

This is a Clojure wrapper of Java implementation of word2vec [available here] (https://github.com/medallia/Word2VecJava).

## Usage

First import clojure-word2vec.core into your namespace

```clojure
(ns clojure-word2vec.examples
  (:require [clojure-word2vec.core :refer :all]
            [clojure.java.io :as io]))
```

Download a text corpus and place it in the resources folder. 
Here we'll download James Joyce's Ulysses from [Project Gutenberg](https://www.gutenberg.org/ebooks/4300.txt.utf-8).


```clojure
(def data
  (create-input-format "ulysses.txt"))
```

Create the model and train it, using the default hyperparameters
```clojure
(def model (word2vec data))
```

The hyper parameters can be specified as arguments to word2vec. 

```clojure
(def model (word2vec data :window-size 15)
```

Find the closest words to a given word

```clojure
(get-matches model "woman")
```

## License

Copyright © 2015 Bridgei2i 

Distributed under the Eclipse Public License version 1.0.
