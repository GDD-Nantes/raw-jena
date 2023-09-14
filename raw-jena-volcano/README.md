# RAW ⨯ Jena Volcano

Apache Jena uses an iterator model, also known as Volcano model, to
iterate over the solutions of queries. This module provides a query
engine that explores queries at random:

- [X] At its core, it provides a scan iterator that allows for getting
  a random triple/quad from a triple/quad pattern. RAW explores each
  depth of the `BPlusTree` that stores the pattern bindings. At each
  step, it chooses a node at random until reaching a leaf. The time
  complexity is `O(log(|D|))` where `|D|` is the dataset size.

- [X] It can also provide an estimated cardinality of a triple/quad
  pattern. Again, it explores the `BPlusTree` to get statistical
  evidences of the filling rate of nodes within the desired pattern
  range. A small cardinality is accurate but estimates become less and
  less accurate as the actual cardinility increases. 
  
- [X] RAW is not limited to single triple/quad pattern. RAW can
  process SPARQL queries including triple/quad patterns, basic graph
  patterns…
  
- [X] RAW returns many insights about its random walks. At most, it
  returns a query plan where every single random walk is mapped with
  its respective cardinality.
  
