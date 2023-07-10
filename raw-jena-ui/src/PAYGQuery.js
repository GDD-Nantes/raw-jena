
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
        this.vars = []

        this.lazyCountEstimate = null;

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
        this.ids = this.getIds(this.plan);
        
        this.totalDuration += duration;
        this.nbTimes += 1;
        this.bindings = this.bindings.concat(bindings);
        
        this.mergeAggregated(this.node2cardinality, rawAggregated.node2cardinality);
        this.mergeAggregated(this.node2nbWalks, rawAggregated.node2nbWalks);

        let parsedWalks = JSON.parse(raw.bindings);

        this.mergeVars(parsedWalks.head.vars);
        this.walks = this.walks.concat(parsedWalks.results.bindings);
        this.cardinalities = this.cardinalities.concat(raw.cardinalities);

        this.lazyCountEstimate = null;
            
        this.updateEstimateAndCI();
    }

    /// (TODO) more generic function to know when RW are successful or not
    getIds(node) {
        if (node.children) {
            return [node.id].concat(this.getIds(node.children[0]));
        }
        return [node.id];
    }

    mergeVars(vars) {
        for (let i in vars) {
            if (!this.vars.includes(vars[i])) {
                this.vars.push(vars[i]);
            }
        }
    }

    getAugmentedPlan() {
        var cloned = JSON.parse(JSON.stringify(this.plan));
        this._getAugmentedPlan(cloned, []);
        return cloned;
    }

    _getAugmentedPlan(node, ids) {
        if (node.id) {
            ids.push(node.id);
            // node.cardinality = (this.node2cardinality[node.id]/this.node2nbWalks[node.id]).toFixed(1);
            node.cardinality = this.countEstimateAvg(ids);
            node.walks = this.node2nbWalks[node.id];
        };
        if (node.children) {
            node.children.forEach(c => {
                this._getAugmentedPlan(c, ids);
            });
        }
    }

    updateEstimateAndCI() {
        const newX = this.cardinalities.length;
        const newY = Math.round(this.countEstimateAvg(this.ids));
        const newCI = Math.round(this.confidence(0.99, this.cardinalities));
        this.cardinalityOverWalks.push({
            x: newX,
            y: newY,
            ci: newCI
        });
    }


    


    countEstimateAvg(ids) {
        if (ids === this.ids && this.lazyCountEstimate) {
            return this.lazyCountEstimate;
        }

        
        let sum = 0;
        for (let i in this.cardinalities) {
            sum += this.countEstimateOf(this.cardinalities[i], ids)/this.cardinalities.length;
        }

        if (ids === this.ids) {
            this.lazyCountEstimate = sum;
        }
        
        return sum;
    }
    
    countEstimateOf(cardinalities, ids) {
        if (ids.every(id => Object.keys(cardinalities).includes(String(id)))) {            
            let cardinality = 1;
            ids.forEach(id => {
                cardinality *= cardinalities[String(id)];
            });
            return cardinality;
        }
        
        return 0;
    }

    variance(allCardinalities) {
        const globalAverage = this.countEstimateAvg(this.ids);
        let sumOfPowered = 0;
        for (let i = 0; i < allCardinalities.length; ++i) {
            let count = this.countEstimateOf(allCardinalities[i], this.ids);
            let powered = Math.pow(count - globalAverage, 2);
            let overN = powered/allCardinalities.length;
            sumOfPowered += overN;
        }
        
        return sumOfPowered;
    }

    confidence(rate, allCardinalities) {
        return rate*Math.sqrt(this.variance(allCardinalities))/Math.sqrt(allCardinalities.length);
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
