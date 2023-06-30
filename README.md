# RAW ⨯ Jena

[**Apache Jena**](https://jena.apache.org/) is `a free and open source
Java framework for building Semantic Web and Linked Data
applications.` It allows users to execute SPARQL queries from start to
finish (granted they finish before a preset timeout). Unfortunately,
it lacks of a very useful feature: *sampling*. 

**RAW ⨯ Jena** fills this gap by performing **RA**ndom **W**alks over
queries. This enables getting random elements, processing
aggregations, optimizing join order etc.


## Usage

```bash 
# Get command help
mvn exec:java -pl raw-jena-module -Dexec.args="--help"

# Usage: <main class> [-h] [--database=<database>] [--limit=<limit>]
#                     [--timeout=<timeout>] [--ui=<ui>] [-v=<verbosity>]
#       --database=<database>   The path to your TDB2 database (default:
#                                 downloads Watdiv10M).
#       --limit=<limit>         The maximum number of random walks per query
#                                 (default: 10K).
#       --timeout=<timeout>     The maximal duration of random walks (default:
#                                 60K ms).
#       --ui=<ui>               The path to your UI folder.
#   -h, --help                  Display this help message.
#   -v, --verbosity=<verbosity> The verbosity level (ALL, INFO, FINE).
```

```bash
# Run a server allowing random walks as an example (TODO)
```


```bash
# Run the graphical user interface (TODO)
```
