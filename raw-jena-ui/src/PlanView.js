
export class PlanView {
    // Compute the tree height; this approach will allow the height of the
    // SVG to scale according to the breadth (width) of the tree layout.

    constructor(data, viewPoint) {

        const width = viewPoint.getBoundingClientRect().width;
        const height = viewPoint.getBoundingClientRect().height;

        const root = d3.hierarchy(data);

        console.log(root);
        
        const dx = 10;
        const dy = width / (root.height + 1);

        // Create a tree layout.
        const tree = d3.tree().nodeSize([dy, dx]);

        // Sort the tree and apply the layout.
        root.sort((a, b) => d3.ascending(a.data.name, b.data.name));
        tree(root);

        // Compute the extent of the tree. Note that x and y are swapped here
        // because in the tree layout, x is the breadth, but when displayed, the
        // tree extends right rather than down.
        let x0 = Infinity;
        let x1 = -x0;
        root.each(d => {
            if (d.x > x1) x1 = d.x;
            if (d.x < x0) x0 = d.x;
        });

        // Compute the adjusted height of the tree.
        // const height = x1 - x0 + dx * 2;

        const svg = d3.create("svg")
              .attr("width", width)
              .attr("height", height)
        //.attr("viewBox", [-dy / 3, x0 - dx, width, height])
              // .attr("style", "max-width: 100%; height: auto; font: 10px sans-serif;");

        const rootG = svg.append("g");

        const link = rootG.append("g")
              .attr("fill", "none")
              .attr("stroke", "#555")
              .attr("stroke-opacity", 0.4)
              .attr("stroke-width", 1.5)
              .selectAll()
              .data(root.links())
              .join("path")
              .attr("d", d3.linkVertical()
                    .x(d => d.x)
                    .y(d => d.y));
        
        const node = rootG.append("g")
              .attr("stroke-linejoin", "round")
              .attr("stroke-width", 3)
              .selectAll()
              .data(root.descendants())
              .join("g")
              .attr("cx", d => d.x)
              .attr("cy", d => d.y);
              //.attr("transform", d => `translate(${d.y},${d.x})`);

        node.append("circle")
            .attr("fill", d => d.children ? "#555" : "#999")
            .attr("r", 2.5);

        node.append("text")
            .attr("dy", "0.31em")
            .attr("x", d => d.children ? -6 : 6)
            .attr("text-anchor", d => d.children ? "end" : "start")
            .text(d => "tp") // d.data.name)
            .clone(true).lower()
            .attr("stroke", "white");

        ////////////////////////////
        // span and zoom allowed
        
        function zoomed({ transform }) {
            rootG.attr("transform", transform);
            // node.attr("transform", transform);
            // link.attr("transform", transform);
        }
        
        let zoom = d3.zoom().on("zoom", zoomed);
        
        svg.call(zoom);
        
        viewPoint.appendChild(svg.node());
    }
}
