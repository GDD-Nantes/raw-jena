# RAW ⨯ Jena

[**Apache Jena**](https://jena.apache.org/) is `a free and open source
Java framework for building Semantic Web and Linked Data
applications.` It allows users to execute SPARQL queries from start to
finish (granted they finish before a preset timeout). Unfortunately,
it lacks of a very useful feature: *sampling* [1].

**RAW ⨯ Jena** [2] fills this gap by performing **RA**ndom **W**alks over
queries, and by processing cardinality estimates [3]. This enables
getting random elements, processing aggregations, optimizing join
order, building summaries etc.



## Dependencies and configurations

- Java 21
- `~/.m2/settings.xml` configured to include
  `https://maven.pkg.github.com/Chat-Wane/sage-jena`. See an example
  in [settings.xml](settings.xml). You need to generate
  [credentials](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages). This requirement is meant to disappear in the future as
  either we publish on Maven's central repository, or GitHub allows
  downloading without credentials.
  - Alternatively, [clone and install the old way](https://github.com/Chat-Wane/sage-jena#installation).

## Usage

```bash 
# Get command help
mvn exec:java -pl raw-jena-module -Dexec.args="--help"

# Usage: <main class> [-h] [--database=<database>] [--limit=<limit>]
#                     [--port=<port>] [--timeout=<timeout>] [--ui=<ui>]
#                     [-v=<verbosity>]
#       --database=<database>   The path to your TDB2 database (default: downloads Watdiv10M).
#       --limit=<limit>         The maximum number of random walks per query (default: 10K).
#       --port=<port>           The port that gives access to the database (default: 3330).
#       --timeout=<timeout>     The maximal duration of random walks (default: 60K ms).
#       --ui=<ui>               The path to your UI folder (default: None).
#   -h, --help                  Display this help message.
#   -v, --verbosity=<verbosity> The verbosity level (ALL, INFO, FINE) (default: None).
```

```bash
# Run a server allowing random walks on your Apache Jena TDB2 database.
# Alternatively, without arguments, it will download and serve WatDiv10M.
mvn exec:java -pl raw-jena-module -Dexec.args="--database=/path/to/jena/tdb2/database"
```

```bash
# Install the dependencies of the website
npm install --prefix ./raw-jena-ui

# Serve the Web graphical user interface
vite ./raw-jena-ui
```



# References

[1] S. Agarwal, H. Milner, A. Kleiner, A. Talwalkar, M. I. Jordan,
S. Madden, B. Mozafari, I. Stoica, <i>Knowing when you’re wrong:
Building fast and reliable approximate query processing systems.</i>

[2] J. Aimonier-Davat, M.-H. Dang, P. Molli, B. Nédelec, and
H. Skaf-Molli, <i>[RAW-JENA: Approximate Query Processing for SPARQL
Endpoints.](https://hal.science/hal-04250060v1/file/paper.pdf)</i>

[3] F. Li, B. Wu, K. Yi, Z. Zhao, <i>Wander Join and XDB: Online
Aggregation via Random Walks.</i>
