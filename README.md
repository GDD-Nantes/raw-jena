# RAW ⨯ Jena ⨯ UI

**Web user interface** for RAW on top of Jena. It provides a
(Yasgui)[https://github.com/TriplyDB/Yasgui] module that parses the
additional data retrieved from the RAW server and display them. Among
others, it aims to provide users with additional insights about their
queries, such as: 

- [ ] the approximate number of results of their query;

- [ ] the confidence interval for this approximation;

- [ ] the number of random walks per node in their query plan.


# Usage

```bash
# Installs the few dependencies
npm install

# Run the server that hosts the UI
npm run server
```
