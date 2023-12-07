import {Plan2Graph} from "./Plan2Graph.js";
import { translate } from 'sparqlalgebrajs';

/**
 * Plugin that prints a SPARQL logical plan as a graph. The
 * main goal is to have a clear representation of SPARQL
 * SERVICE queries.
 */
export class PlanPlugin {

    priority = 10;
    hideFromSelection = false;
    
    constructor(yasr) {
        this.yasr = yasr;
    }

    getQueryAsString() {
        var query = decodeURIComponent(this.yasr.config.getPlainQueryLinkToEndpoint()); // ugly
        const startOfQuery = query.indexOf("?query=") + 7;
        return query.substring(startOfQuery, query.length);
    }    

    draw() {
        // #1 create the canvas that will hold the plan
        const div = document.createElement("div");
        div.setAttribute("class", "raw_graph");

        const r = document.createElementNS("http://www.w3.org/2000/svg", "g");
        r.setAttribute("id", "rootG");
        const n = document.createElementNS("http://www.w3.org/2000/svg", "g");
        n.setAttribute("class", "links");
        const l = document.createElementNS("http://www.w3.org/2000/svg", "g");
        l.setAttribute("class", "nodes");        
        const t = document.createElementNS("http://www.w3.org/2000/svg", "g");
        t.setAttribute("class", "texts");        

        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("id", "plan");
        svg.setAttribute("viewBox", "0 0 300 300");

        svg.appendChild(r);
        r.appendChild(n);
        r.appendChild(l);
        r.appendChild(t);
        div.appendChild(svg);
        this.yasr.resultsEl.appendChild(div);

        // #2 build the plan
        const query = this.getQueryAsString();
        const plan = translate(query, {sparqlStar: true});

        var visitor = new Plan2Graph();
        visitor.visit(plan);

        var width = 300, height = 300

        var glinks = d3.select('.links')
	    .selectAll('line')
	    .data(visitor.links)
	    .join('line')
	    .attr('x1', d => d.source.x)
	    .attr('y1', d => d.source.y)
	    .attr('x2', d => d.target.x)
	    .attr('y2', d => d.target.y);

        var glabels = d3.select('.links') // TODO create "G" for lines and labels
            .selectAll("text")
            .data(visitor.links)
            .join("text")
            .text(d => d.label)
            .attr("font-size", "0.5em");
        
        function updateLinks() {
            glinks.attr('x1', d => d.source.x)
	        .attr('y1', d => d.source.y)
	        .attr('x2', d => d.target.x)
	        .attr('y2', d => d.target.y);

            glabels.attr("x", d => (d.source.x + d.target.x) / 2)
                .attr("y", d => (d.source.y + d.target.y) / 2)
        };


	var gnodes = d3.select('.nodes') // initialize nodes
            .selectAll('g')
	    .data(visitor.nodes)
            .join('g')
            .attr('x', d => d.x)
            .attr('y', d => d.y)
            .attr('id', d => d.id)
            .call(d3.drag()
                  .on("start", dragstarted)
                  .on("drag", dragged)
                  .on("end", dragended));

        gnodes.append('circle')
            .attr('r', 5)
            .attr('fill', d => d.color)
            .attr('stroke', d => (d.color === "white" && "black") || d.color);
        
        gnodes.append('text')
	    .text(d => d.type)
	    .attr('dy', "0.25em") // vertical centering
            .attr("text-anchor", "middle")
            .attr("font-size", d => d.size);
        

        
        function updateNodes() {
            gnodes.attr("transform", d => "translate("+d.x+","+d.y+")")
                .call(d3.drag()
                      .on("start", dragstarted)
                      .on("drag", dragged)
                      .on("end", dragended))
        };

        function ticked() {
            updateLinks();
            updateNodes();
        };
        
        var simulation = d3.forceSimulation(visitor.nodes)
            .force('charge', d3.forceManyBody())
            .force('center', d3.forceCenter(width / 2, height / 2))
            .force('link', d3.forceLink().links(visitor.links))
            .on('tick', ticked);


        // zoom zoom zoom
        function zoomed({ transform }) {
            r.setAttribute("transform", transform);
        }
        
        let zoom = d3.zoom().on("zoom", zoomed);        
        d3.select("#plan").call(zoom);

        // dragable
        const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
        g.setAttribute("cursor", "grab");
        svg.appendChild(g);

        function getNode(nodes, id) { // could be more efficient with a map
            for (var i in nodes) {
                if (nodes[i].id === id) {
                    return nodes[i];
                };
            };
        }
        
        function dragstarted() {
            d3.select(this).raise();
            g.setAttribute("cursor", "grabbing");
        }
        
        function dragged(event, d) {
            var n = getNode(visitor.nodes, d.id);
            n.x = event.x;
            n.y = event.y;
            simulation.alpha(0.05).restart();
        }
        
        function dragended() {
            g.setAttribute("cursor", "grab");
        }
    }

    canHandleResults() {
        try {
            var query = decodeURIComponent(this.yasr.config.getPlainQueryLinkToEndpoint());
            const startOfQuery = query.indexOf("?query=") + 7;
            query = query.substring(startOfQuery, query.length);
            translate(query, {sparqlStar: true});
            // TODO maybe add a "can I visit it" check?
        } catch (error) {
            return false;
        }
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
