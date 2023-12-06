import {Plan2Graph} from "./Plan2Graph.js";
import { translate } from 'sparqlalgebrajs';

// Plugin that prints a SPARQL logical plan as a graph.
export class PlanPlugin {

    priority = 10;
    hideFromSelection = false;
    
    constructor(yasr) {
        this.yasr = yasr;
    }

    draw() {
        // #1 create the canvas that will hold the plan
        const div = document.createElement("div");
        div.setAttribute("class", "raw_graph");

        const n = document.createElementNS("http://www.w3.org/2000/svg", "g");
        n.setAttribute("class", "links");
        const l = document.createElementNS("http://www.w3.org/2000/svg", "g");
        l.setAttribute("class", "nodes");        
        const t = document.createElementNS("http://www.w3.org/2000/svg", "g");
        t.setAttribute("class", "texts");        

        const canvas = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        canvas.setAttribute("id", "plan");
        canvas.setAttribute("viewBox", "0 0 300 300");

        canvas.appendChild(l);
        canvas.appendChild(n);
        canvas.appendChild(t);
        div.appendChild(canvas);
        this.yasr.resultsEl.appendChild(div);

        const plan = translate(`
PREFIX wd: <http://wd>
PREFIX wdt: <http://wdt>
PREFIX owl: <http://owl>
PREFIX dbo: <http://dbo>

SELECT * WHERE { {
   SERVICE <https://query.wikidata.org/sparql> {
     ?x wdt:P39 wd:Q11696 . #tp1
     ?x wdt:P102 ?party . } #tp2
   SERVICE <https://dbpedia.org/sparql> {
     ?y owl:sameAs ?x . #tp3
     ?y dbo:predecessor ?predecessor . #tp4
     ?y dbo:successor ?successor . } #tp5
 } UNION {
   SERVICE <https://query.wikidata.org/sparql> {
     ?x wdt:P39 wd:Q11696 . #tp1
     ?x wdt:P102 ?party .  #tp2
     ?y owl:sameAs ?x . } #tp3
   SERVICE <https://dbpedia.org/sparql> {
     ?y dbo:predecessor ?predecessor . #tp4
     ?y dbo:successor ?successor . } #tp5
 }
}
`);
        console.log(plan);

        var visitor = new Plan2Graph();
        visitor.visit(plan);
        console.log(visitor);

        var width = 300, height = 300
        var nodes =  visitor.nodes;
        var links = visitor.links; 

        function updateLinks() {
	    d3.select('.links')
		.selectAll('line')
		.data(links)
		.join('line')
		.attr('x1', d => d.source.x)
		.attr('y1', d => d.source.y)
		.attr('x2', d => d.target.x)
		.attr('y2', d => d.target.y)
        };
        
        function updateNodes() {
	    d3.select('.nodes')
		.selectAll('circle')
		.data(nodes)
                .join('circle')
                .attr('r', 5)
                .attr('fill', d => d.color)
                .attr('cx', d => d.x)
                .attr('cy', d => d.y)
        };

        function updateTexts() {
            d3.select('.texts')
		.selectAll('text')
		.data(nodes)
                .join('text')
		.text(d => d.type)
		.attr('x', d => d.x)
		.attr('y', d => d.y)
		.attr('dy', 5)
                .attr("text-anchor", "middle");
        };

                
        function ticked() {
            updateLinks();
            updateNodes();
            updateTexts();
        };
        
        var simulation = d3.forceSimulation(nodes)
            .force('charge', d3.forceManyBody())
            .force('center', d3.forceCenter(width / 2, height / 2))
            .force('link', d3.forceLink().links(links))
            .on('tick', ticked);
    }

    canHandleResults() {
        // TODO Check if the plan is valid
        return true;
    }

    getIcon() {
        const textIcon = document.createElement("div");
        textIcon.setAttribute("class", "svgImg plugin_icon");
        // taken from https://www.svgrepo.com/svg/363159/tree-evergreen-bold
        // COLLECTION: Phosphor Bold Icons
        // LICENSE: MIT License
        // AUTHOR = phosphor
        textIcon.innerHTML = `
<svg viewBox="0 0 256 256" id="Flat" xmlns="http://www.w3.org/2000/svg"><g id="SVGRepo_bgCarrier" stroke-width="0"></g><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g><g id="SVGRepo_iconCarrier"> <path d="M233.47217,184.63281,192.53564,132H208a12.0002,12.0002,0,0,0,9.51172-19.31641l-80-104a12.00029,12.00029,0,0,0-19.02344,0l-80,104A12.0002,12.0002,0,0,0,48,132H63.46436L22.52783,184.63281A11.99982,11.99982,0,0,0,32,204h84v36a12,12,0,0,0,24,0V204h84a11.99982,11.99982,0,0,0,9.47217-19.36719ZM56.53564,180l40.93653-52.63281A11.99982,11.99982,0,0,0,88,108H72.37012L128,35.68164,183.62988,108H168a11.99982,11.99982,0,0,0-9.47217,19.36719L199.46436,180Z"></path> </g></svg>`
        return textIcon;
    }
}
