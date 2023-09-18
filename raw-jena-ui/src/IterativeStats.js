import {Iteration} from "./Iteration.js";

export class IterativeStats {

    iterations = new Array();
    
    constructor () {
    }

    add(cardinalities) {
        this.iterations.push(new Iteration(cardinalities))
    }
    

}
