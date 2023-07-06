
// Fills the examples container with query that write in yasqe
// on click, and with a tooltip that shows the query.
export class QueryExamples {

    queries = [
        { name: "query_35",
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P106> <http://www.wikidata.org/entity/Q12299841> .
  ?x2 <http://www.wikidata.org/prop/direct/P17> ?x3 .
  ?x1 <http://www.wikidata.org/prop/direct/P19> ?x2 .
  ?x4 <http://www.wikidata.org/prop/direct/P569> ?x4 .
}` },
        { name: "query_139",
          query: "" },
        { name: "query_171",
          query: "" },
        { name: "query_183",
          query: "" },
        { name: "query_209",
          query: "" },
        { name: "query_347",
          query: "" },
        { name: "query_357",
          query: "" },
        { name: "query_358",
          query: "" },
        { name: "query_360",
          query: "" },
        { name: "query_362",
          query: "" },
        { name: "query_604",
          expect: 25276453, 
          query: `SELECT * WHERE {
    ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q5> .
    ?x2 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q5> .
    ?x1 <http://www.wikidata.org/prop/direct/P569> ?x3 .
    ?x2 <http://www.wikidata.org/prop/direct/P569> ?x3 .
    ?x1 <http://www.wikidata.org/prop/direct/P570> ?x4 .
    ?x2 <http://www.wikidata.org/prop/direct/P570> ?x4 .
}`
        },
        { name: "query_605",
          query: "" },
        { name: "query_630",
          query: "" },
        { name: "query_631",
          query: "" },
        { name: "query_633",
          query: "" },
        { name: "query_679",
          query: ""}
    ];

    constructor(dialog, yasqe) {
        const container = document.createElement("span");
        container.setAttribute("class", "examples_dialog_content");

        const table = document.createElement("table");
        const tbody = document.createElement("tbody");

        this.queries.forEach(q => {
            if (q.query === "") { return; }
            
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            const button = document.createElement("button");
            button.setAttribute("class", "examples_dialog_button")
            button.innerHTML = q.name;
            button.addEventListener("click", () => {
                yasqe.setValue(q.query);
            });
            button.setAttribute("title", q.query);
            
            td.appendChild(button);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);
        dialog.appendChild(container);
    }

}
