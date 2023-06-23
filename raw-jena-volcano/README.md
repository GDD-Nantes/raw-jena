# RAW ⨯ Jena 

**Jena** allows users to execute a query from start to finish, granted
it finishes before a preset timeout.  **Jena ⨯ Sampling** extends Jena
by providing sampling capabilities to users:

- [ ] A request does not simply execute but provides a random view of
  the query execution. In other words, it returns a tree of random
  walks representing a random subset of the full execution tree. This
  enables query optimizations with the idea that spending time in
  exploring will result in massive execution time gains. 

- [ ] For this, we implement a way to create randomly jumping range
  iterators where each call to `next()` produces a random value within
  the initial range by descending in the `BPlusTree` at random.
