
// Fills the examples container with query that write in yasqe
// on click, and with a tooltip that shows the query.
export class QueryExamples {

    queries = [
        { name: "query 139",
          query: "" },
        { name: "query 171",
          query: "" },
        { name: "query 183",
          query: "" },
        { name: "query 209",
          query: "" },
        { name: "query 347",
          description: "All cities that are a country and in this country along with their mayor",
          expect: 1,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P17> ?x2 .
  ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q515> .
  ?x2 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q6256> .
  ?x3 <http://www.wikidata.org/prop/direct/P39> <http://www.wikidata.org/entity/Q30185> .
  ?x3 <http://www.wikidata.org/prop/direct/P6> ?x1 .
}` },
        { name: "query 357",
          description: "All people that are country along with their respective countinent (Mistakes?)",
          expect: 1327,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P17> ?x2 .
  ?x2 <http://www.wikidata.org/prop/direct/P30> ?x3 .
  ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q5> .
}` },
        { name: "query 358",
          description: "All tennis national teams along with their respective country's sport governing body",
          expect: 109,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P17> ?x2 .
  ?x3 <http://www.wikidata.org/prop/direct/P17> ?x2 .
  ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q1194951> .
  ?x3 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q2485448> .
  ?x1 <http://www.wikidata.org/prop/direct/P641> <http://www.wikidata.org/entity/Q847> .
  ?x3 <http://www.wikidata.org/prop/direct/P641> <http://www.wikidata.org/entity/Q847> .
}` },
        { name: "query 360",
          description: "All cities and associated country whose mayor is female",
          expect: 10,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P17> ?x2 .
  ?x3 <http://www.wikidata.org/prop/direct/P21> <http://www.wikidata.org/entity/Q6581072> .
  ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q515> .
  ?x2 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q6256> .
  ?x3 <http://www.wikidata.org/prop/direct/P39> <http://www.wikidata.org/entity/Q30185> .
  ?x1 <http://www.wikidata.org/prop/direct/P6> ?x3 .
}` },
        { name: "query 362",
          query: "" },
        { name: "query 604",
          description: "All pairs of people with identical birth and death dates",
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
        { name: "query 605",
          expect: 4802920630,
          description: "All pairs of people that attended the same educational institution",
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q5> .
  ?x2 <http://www.wikidata.org/prop/direct/P31> <http://www.wikidata.org/entity/Q5> .
  ?x1 <http://www.wikidata.org/prop/direct/P69> ?x3 .
  ?x2 <http://www.wikidata.org/prop/direct/P69> ?x3 .
}` },
        { name: "query 633",
          description: "All people with their respective PubMed ID" ,
          expect: 34916061,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P31> ?x2 .
  ?x1 <http://www.wikidata.org/prop/direct/P698> ?x3 .
}` },
        { name: "query_631",
          query: "" },
        { name: "query_630",
          query: "" },
        { name: "query 679",
          description: "All human beings that are in publications (Mistakes?)",
          expect: 259,
          query: `SELECT * WHERE {
  ?x1 ?x2 <http://www.wikidata.org/entity/Q5> .
  ?x1 <http://www.wikidata.org/prop/direct/P1433> ?x3 .
}`},
        { name: "query 256",
          description: "The name, issue, and volume of every publication",
          expect: 29977745,
          query: `SELECT * WHERE {
  ?x1 <http://www.wikidata.org/prop/direct/P1433> ?x2 .
  ?x1 <http://www.wikidata.org/prop/direct/P433> ?x3 .
  ?x1 <http://www.wikidata.org/prop/direct/P478> ?x4 .
}`}   ];

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
            button.innerHTML = q.description;
            button.addEventListener("click", () => {
                yasqe.setValue(q.query);
            });
            button.setAttribute("title", q.name + ":\n\n" + q.query + "\n\nNumber of elements: "+ q.expect.toLocaleString());
            
            td.appendChild(button);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);
        dialog.appendChild(container);
    }

}
