
/// Pay-as-you-go query just saves the information of a query and merge
/// the information when possible
class PAYGQuery {

    constructor(query) {
        this.query = query;
        this.totalDuration = 0;
        this.bindings = [];
        this.nbTimes = 0;
    }

    addDuration(duration) {
        // (TODO) check if the query is the proper one.
        this.totalDuration += duration;
        this.nbTimes += 1;
    }

    addBindings(binding) {
        this.bindings = this.bindings.concat(binding);
    }

    equals(o) {
        return o.query === this.query;
    }

}
