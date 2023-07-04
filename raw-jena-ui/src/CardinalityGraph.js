
// Build the d3 graph that displays the estimated cardinality
// along with the confidence interval. We expect to display
// that it converges over the walks towards the actual value.
export class CardinalityGraph {

    constructor(viewPoint) {
        // set the dimensions and margins of the graph
        var margin = {top: 10, right: 30, bottom: 30, left: 60},
            width = 460 - margin.left - margin.right,
            height = 400 - margin.top - margin.bottom;

        // append the svg object to the body of the page
        var svg = d3.create("svg")
            .attr("width", "100%") // width + margin.left + margin.right)
            .attr("height", "100%"); //height + margin.top + margin.bottom);
        
        viewPoint.appendChild(svg.node());

        svg.append("g")
            .attr("transform",
                  "translate(" + margin.left + "," + margin.top + ")");
        
        //Read the data
        let meow = "{\"test\": 12 }";
        let data = [
            {x: 5,
             y: 5,
             CI_right:2,
             CI_left:8},
            {x: 20,
             y:8,
             CI_right:5,
             CI_left:10
            },
            {x: 80,
             y: 12,
             CI_right: 11,
             CI_left: 13}
        ];

        
        //data.x = [5, 10, 15];
        //data.y = [12, 14, 12]
        //data.CI_right = [2, 4, 5];
        //data.CI_left = [3, 5, 3];
        
        // Add X axis --> it is a date format
        var x = d3.scaleLinear()
            .domain([1,100])
            .range([ 0, width ]);

        
        svg.append("g")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.axisBottom(x));

        // Add Y axis
        var y = d3.scaleLinear()
            .domain([0, 13])
            .range([ height, 0 ]);
        svg.append("g")
            .call(d3.axisLeft(y));

        // Show confidence interval
        svg.append("path")
            .datum(data)
            .attr("fill", "#cce5df")
            .attr("stroke", "none")
            .attr("d", d3.area()
                  .x(function(d) { return x(d.x) })
                  .y0(function(d) { return y(d.CI_right) })
                  .y1(function(d) { return y(d.CI_left) })
                 )

        // Add the line
        svg.append("path")
            .datum(data)
            .attr("fill", "none")
            .attr("stroke", "steelblue")
            .attr("stroke-width", 1.5)
            .attr("d", d3.line()
                  .x(function(d) { return x(d.x) })
                  .y(function(d) { return y(d.y) }) )
        

        
    }
    
}
