
export class PlanView {
    // Compute the tree height; this approach will allow the height of the
    // SVG to scale according to the breadth (width) of the tree layout.

    constructor(data, viewPoint) {
        // (TODO) viewPoint's bounding box does not work well
        // find out another way
        const width = viewPoint.getBoundingClientRect().width;
        const height = viewPoint.getBoundingClientRect().height;

        const root = d3.hierarchy(data);
        
        var treeLayout = d3.tree();
        treeLayout.size([width, height]);

        treeLayout(root);

        
        
        const svg = d3.create("svg")
              .attr("width", width)
              .attr("height", height);
        
        const rootG = svg.append("g")
              .attr("id", "rootG");

        const node = rootG.append("g")
              .selectAll()
              .data(root.descendants())
              .join('circle')
              .attr('cx', function(d) {return d.x;})
              .attr('cy', function(d) {return d.y;})
              .attr('r', 4);
            
        const link = rootG.append("g")
              .attr("fill", "none")
              .attr("stroke-opacity", 0.4)
              .attr("stroke-width", 1.5)
              .selectAll()
              .data(root.links())
              .join("path")
              .attr("d", d3.linkVertical()
                    .x(d => d.x)
                    .y(d => d.y));

        viewPoint.appendChild(svg.node());

        var text = rootG.append("g")
            .selectAll()
            .data(root.descendants())
            .join("text")
            .attr("class", "text_plan")
            .attr("x", d => d.x + 7)
            .attr("y", d => d.y)
        text.append("tspan")
            .attr("class", "text_plan desc_plan")
            .attr("x", d => d.x + 7)
            .attr("dy", "0em")
            .text(d => d.data.name);
        text.append("tspan")
            .attr("class", "text_plan type_plan")
            .attr("x", d => d.x - 7)
            .text(d => d.data.type + "#" + d.data.id);
        text.append("tspan")
            .attr("class", "text_plan card_plan")
            .attr("x", d => d.x + 5)
            .attr("dy", "1.2em")
            .text(d => "~ " + Number(d.data.cardinality).toLocaleString()  + " elements sampled over " +
                  Number(d.data.walks).toLocaleString() + " walks");
        
        ///////////////////////////////////////////////////////////////////
        
        function zoomed({ transform }) {
            rootG.attr("transform", transform);
        }
        
        let zoom = d3.zoom().on("zoom", zoomed);        
        svg.call(zoom);
        
        viewPoint.appendChild(svg.node());

        // Put most of elements in the camera field
        const width2 = rootG.node().getBBox().width;
        const height2 = rootG.node().getBBox().height;

        svg.attr("viewBox", (width2/2) + " -20 " + width2 + " " + (height2+20));

    }
}
