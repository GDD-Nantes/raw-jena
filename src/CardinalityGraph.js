import {Chart, registerables} from "chart.js";
Chart.register(...registerables);


export class CardinalityGraph {
    
    constructor(data, viewPoint) {
        let canvas = document.createElement("canvas");
        viewPoint.appendChild(canvas);
        
        new Chart(canvas, {
            type: 'line',
            data: {
                labels: data.map(e => e.x),
                datasets: [
                    { label: '',
                      backgroundColor: 'rgba(235, 203, 139,  0.3)',
                      type: 'line',
                      data: data.map(e => e.y + e.ci),
                      fill: false,
                      pointRadius: 0
                    },
                    { label: '',
                      backgroundColor: 'rgba(235, 203, 139,  0.3)',
                      type: 'line',
                      data: data.map(e => e.y - e.ci),
                      fill: '-1',
                      pointRadius: 0 },
                    { label: 'Estimated cardinality',
                      type: 'line',
                      backgroundColor: 'rgba(163, 190, 140,  1)',
                      data: data.map(e => e.y),  
                      borderWidth: 5,
                    }
                ]
            },
            options: {
                animation: {
                    duration: 0
                },
                plugins: {
                    title: {
                        display: true,
                        text: "Estimated " + data[data.length-1].y.toLocaleString() + " ± "+ data[data.length-1].ci.toLocaleString() + " elements",
                    },
                    legend: {
                        display: false
                    }
                },
                scales: {
                    // y: { beginAtZero: true },
                    x: {
                        title:{
                            display:true,
                            text: "Number of random walks"
                        },
                        beginAtZero: true
                    },
                    y: {
                        title:{
                            display:true,
                            text: "Cardinality estimation"
                        }
                    }
                }
            }
        });
    }
    
}

