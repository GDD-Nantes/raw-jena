# Sage ⨯ Jena-Fuseki

[Fuseki](https://github.com/apache/jena/tree/main/jena-fuseki2)
conveniently adds a web server on top of
[Jena](https://github.com/apache/jena). This project provides a
`SageModule` that automatically starts with the server and intercepts
`SELECT` queries on `TDB2` storage systems to execute them with
pausing/resuming capabilities.

The HTTP response of each call piggybacks additional control
information enabling its resuming. Over calls, the merge of partial
results eventually leads to complete results which dramatically
increases the scope of SPARQL endpoints: they are now able to provide
complete answers. 

Furthermore, Sage allows modifying its timeout before pausing/resuming
which as the nice side effect to alleviate convoy effects when set
appropriately.
