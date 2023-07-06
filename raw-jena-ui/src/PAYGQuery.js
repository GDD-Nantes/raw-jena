
/// Pay-as-you-go query just saves the information of a query and merge
/// the information when possible
export class PAYGQuery {

    constructor() {
        this.reset();
    }

    // Reset the structure, so it can start with a new pay-as-you-go
    // query.
    reset() {
        this.totalDuration = 0;
        this.bindings = [];
        this.nbTimes = 0;
        this.node2cardinality = {};
        this.node2nbWalks = {};
        
        this.plan = {};
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
    
    
    // Merge the newly arrived data with the current one if possible.
    update(duration, bindings, raw, rawAggregated) {
        // (TODO) compare two plan instead of hacking with serialization…
        if (JSON.stringify(this.plan) !== JSON.stringify(JSON.parse(raw.plan))) { // plan is different, must be a different request
            this.reset();
        }
        this.plan = JSON.parse(raw.plan);
        this.totalDuration += duration;
        this.nbTimes += 1;
        this.bindings = this.bindings.concat(bindings);
        
        this.mergeAggregated(this.node2cardinality, rawAggregated.node2cardinality);
        this.mergeAggregated(this.node2nbWalks, rawAggregated.node2nbWalks);
        
        let parsedWalks = JSON.parse(raw.bindings);
        // raw.payg.addWalks(parsedWalks.results.bindings);
        this.walks = this.walks.concat(parsedWalks.results.bindings);
        
        this.cardinalities = this.cardinalities.concat(raw.cardinalities);
        this.updateEstimateAndCI();
    }

    getAugmentedPlan() {
        var cloned = JSON.parse(JSON.stringify(this.plan));
        this._getAugmentedPlan(cloned);
        return cloned;
    }

    _getAugmentedPlan(node) {
        if (node.id) {
            // ceil to avoid 0 elements
            node.cardinality = (this.node2cardinality[node.id]/this.node2nbWalks[node.id]).toFixed(1);
            node.walks = this.node2nbWalks[node.id];
        };
        if (node.children) {
            node.children.forEach(c => {
                this._getAugmentedPlan(c);
            });
        }
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
            count *= (this.node2cardinality[k]/this.node2nbWalks[k]);
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
