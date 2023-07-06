import {PAYGQuery} from "./PAYGQuery";
import {PlanView} from "./PlanView";
import {CardinalityGraph} from "./CardinalityGraph";
// import {Yasr} from "yasgui";

// Plugin that reads results of a SPARQL server.
// If the answer includes RAW fields, it handles the additional data.
export class RAWPlugin {

    priority = 10;
    hideFromSelection = false;
    payg = new PAYGQuery("meow");
    
    constructor(yasr) {
        this.yasr = yasr;
    }

    draw() {
        // #2 create the cardinality & confidence graph
        const tableGraph = document.createElement("table");
        tableGraph.setAttribute("class", "tableGraph");
        const tr = document.createElement("tr");

        tableGraph.appendChild(tr);
        this.yasr.resultsEl.appendChild(tableGraph);

        const cardinalityContainer = document.createElement("td");
        cardinalityContainer.setAttribute("class", "raw_graph");
        new CardinalityGraph(this.payg.cardinalityOverWalks, cardinalityContainer);
        tr.appendChild(cardinalityContainer);

        // #3 create the query plan graph
        const planContainer = document.createElement("td");
        planContainer.setAttribute("class", "raw_graph");
        tr.appendChild(planContainer); // so it gets a width
        new PlanView(this.payg.getAugmentedPlan(), planContainer);

        // #4 (TODO) print the table of bindings of random walks
        const tdResults = document.createElement("td");
        tdResults.setAttribute("class", "raw_graph");
        tdResults.setAttribute("colspan", "2");
        const trResults = document.createElement("tr");
        trResults.appendChild(tdResults);
        tableGraph.appendChild(trResults);

        const yasr = new Yasr(tdResults);

        let json = {
            results: {
                bindings: this.payg.walks
            },
            head: {
                vars: ["x1", "x2", "x3", "x4"]
            }
        };

        var actualYasrResults = tdResults.getElementsByClassName("yasr_results")[0];
        var yasrDiv = tdResults.getElementsByClassName("yasr")[0];
        tdResults.removeChild(yasrDiv);
        tdResults.appendChild(actualYasrResults);        

        yasr.setResponse(json, 42);
    }

    canHandleResults() {
        return this.yasr.results && this.yasr.results.json && this.yasr.results.json.RAWOutput;
    }

    getIcon() {
        const textIcon = document.createElement("div");
        textIcon.setAttribute("class", "svgImg plugin_icon");
        // taken from https://www.svgrepo.com/svg/488669/random
        // COLLECTION: Licons Oval Line Interface Icons
        // LICENSE: MIT License
        // AUTHOR = Klever Space
        textIcon.innerHTML = `
<svg fill="#000000" viewBox="0 7 18 18" version="1.1" xmlns="http://www.w3.org/2000/svg"><g id="SVGRepo_bgCarrier" stroke-width="0"></g><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g><g id="SVGRepo_iconCarrier"> <title>random</title> <path d="M14.92 17.56c-0.32-0.32-0.88-0.32-1.2 0s-0.32 0.88 0 1.2l0.76 0.76h-3.76c-0.6 0-1.080-0.32-1.6-0.96-0.28-0.36-0.8-0.44-1.2-0.16-0.36 0.28-0.44 0.8-0.16 1.2 0.84 1.12 1.8 1.64 2.92 1.64h3.76l-0.76 0.76c-0.32 0.32-0.32 0.88 0 1.2 0.16 0.16 0.4 0.24 0.6 0.24s0.44-0.080 0.6-0.24l2.2-2.2c0.32-0.32 0.32-0.88 0-1.2l-2.16-2.24zM10.72 12.48h3.76l-0.76 0.76c-0.32 0.32-0.32 0.88 0 1.2 0.16 0.16 0.4 0.24 0.6 0.24s0.44-0.080 0.6-0.24l2.2-2.2c0.32-0.32 0.32-0.88 0-1.2l-2.2-2.2c-0.32-0.32-0.88-0.32-1.2 0s-0.32 0.88 0 1.2l0.76 0.76h-3.76c-2.48 0-3.64 2.56-4.68 4.84-0.88 2-1.76 3.84-3.12 3.84h-2.080c-0.48 0-0.84 0.36-0.84 0.84s0.36 0.88 0.84 0.88h2.080c2.48 0 3.64-2.56 4.68-4.84 0.88-2 1.72-3.88 3.12-3.88zM0.84 12.48h2.080c0.6 0 1.080 0.28 1.56 0.92 0.16 0.2 0.4 0.32 0.68 0.32 0.2 0 0.36-0.040 0.52-0.16 0.36-0.28 0.44-0.8 0.16-1.2-0.84-1.040-1.8-1.6-2.92-1.6h-2.080c-0.48 0.040-0.84 0.4-0.84 0.88s0.36 0.84 0.84 0.84z"></path> </g></svg>`
        return textIcon;
    }
}
