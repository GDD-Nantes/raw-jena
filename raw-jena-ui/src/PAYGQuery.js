
/// Pay-as-you-go query just saves the information of a query and merge
/// the information when possible
export class PAYGQuery {

    constructor(query) {
        this.query = query;
        this.totalDuration = 0;
        this.bindings = [];
        this.nbTimes = 0;
        this.node2cardinality = {};
        this.node2nbWalks = {};

        this.plan = "";
        this.cardinalities = [];
        this.walks = []

        this.cardinalityOverWalks = [
            // {
            //     x: 0,
            //     y: 0,
            //     ci: 0,
            // }
        ];
    }

    addDuration(duration) {
        // (TODO) check if the query is the proper one.
        this.totalDuration += duration;
        this.nbTimes += 1;
    }

    addBindings(binding) {
        this.bindings = this.bindings.concat(binding);
    }

    addRAWAggregated(aggregated) {
        this.mergeAggregated(this.node2cardinality, aggregated.node2cardinality);
        this.mergeAggregated(this.node2nbWalks, aggregated.node2nbWalks);
    }

    updatePlan(plan) {
        this.plan = JSON.parse(plan);
    }

    addWalks(walks) {
        this.walks = this.walks.concat(walks);
    }

    addCardinalities(cardinalities) {
        this.cardinalities = this.cardinalities.concat(cardinalities);
    }

    updateEstimateAndCI() {
        const newX = this.cardinalities.length;
        const newY = Math.round(this.estimateCount());
        const newCI = Math.round(this.confidence(0.95, this.cardinalities));
        this.cardinalityOverWalks.push({
            x: newX,
            y: newY,
            ci: newCI
        });
    }



    estimateCount() {
        let count = 1;
        for (let k in this.node2cardinality) {
            count *= this.node2cardinality[k]/this.node2nbWalks[k];
        }
        return count;
    }

    estimateCountOf(cardinalities) {
        let count = 1;
        for (let k in cardinalities) {
            count *= cardinalities[k];
        }
        
        return count;
    }

    variance(allCardinalities) {
        const globalAverage = this.estimateCount();
        let sumOfPowered = 0;
        for (let i = 0; i < allCardinalities.length; ++i) {
            let count = this.estimateCountOf(allCardinalities[i]);
            let powered = Math.pow(count - globalAverage, 2);
            let overN = powered/allCardinalities.length;
            sumOfPowered += overN;
        }
        
        return Math.sqrt(sumOfPowered)/Math.sqrt(allCardinalities.length);
    }

    confidence(rate, allCardinalities) {
        return rate*this.variance(allCardinalities)/Math.sqrt(allCardinalities.length);
    }


    
    mergeAggregated(thisMap, aggregated) {
        for (let k in aggregated) {
            if (k in thisMap) {
                thisMap[k] = thisMap[k] + aggregated[k];
            } else {
                thisMap[k] = aggregated[k];
            }
        }
    }

    equals(o) {
        return o.query === this.query;
    }

}
