

export class Iteration {

    ids2cardinalities = new Map();
    sumCardinality = 0.; // (TODO) maybe ids2sumCardinality
    size = 0.; 

    constructor(cardinalities) {
        for (let i = 0; i < cardinalities.length; ++i) {
            console.log(cardinalities[i]);
            let keys = new Set();
            let values = new Array(); // (TODO) when more than BGP, need to make plan
            for (let j in cardinalities[i]) {
                keys.add(j);
                values.push(cardinalities[i][j]);
            }
            this._add(keys,values);
        }
        console.log(this.ids2cardinalities);
    }


    _add(ids, cardinality) {
        let key = [...ids].join(" "); // transform to string 
        if (!this.ids2cardinalities.has(key)) {
            this.ids2cardinalities.set(key, new Array());
        }
        this.ids2cardinalities.get(key).push(cardinality);
    }

}
