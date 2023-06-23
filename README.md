# RAW ⨯ Jena

[**Apache Jena**](https://jena.apache.org/) is `a free and open source
Java framework for building Semantic Web and Linked Data
applications.` It allows users to execute SPARQL queries from start to
finish (granted they finish before a preset timeout). Unfortunately,
it lacks of a very useful feature: *sampling*. 

**RAW ⨯ Jena** fills this gap by performing **RA**ndom **W**alks over
queries. This enables getting random elements, processing
aggregations, optimizing join order etc.
