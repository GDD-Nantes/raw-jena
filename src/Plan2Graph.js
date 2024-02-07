import ColorHash from 'color-hash'

/**
 * Creates a graph based on the sparqlalgebrajs plan meant to
 * be exploited within D3.
 */
export class Plan2Graph {

    LEFTJOIN = "⟕";
    PRODUCT = "⨯";
    UNION   = "∪";
    
    constructor() {
        this.ids = 0; // the node identifiers to allocate
        this.nodes = [];
        this.links = [];
        this.colorHash = new ColorHash();
        this.flatten = true;
    }
    
    visit(node, args) {
        switch (node.type) {
        case "project" : return this.visitProject(node, args);
        case "slice"   : return this.visitSlice(node, args);
        case "distinct": return this.visitDistinct(node, args);
        case "orderby" : return this.visitOrderBy(node, args);

        case "values"  : return this.visitValues(node, args);
        case "leftjoin": return this.visitLeftJoin(node, args);
        case "bgp"     : return this.visitBGP(node, args);
        case "filter"  : return this.visitFilter(node, args);
        case "service" : return this.visitService(node, args);
        case "join"    : return this.visitJoin(node, args);
        case "union"   : return this.visitUnion(node, args);
        case "pattern" :
            switch (node.termType) {
            case "Triple": return this.visitTriple(node, args);
            case "Quad"  : return this.visitQuad(node, args);
            default: throw new Error("Type " + node.termType + " not implemented…");
            };
        default: throw new Error("Operator " + node.type + " not implemented…");
        }
    }



    visitProject(node, args) {
        return this.visitUnaryOperator(node, args);
    }

    visitSlice(node, args) {
        return this.visitUnaryOperator(node, args);        
    }

    visitDistinct(node, args) {
        return this.visitUnaryOperator(node, args);        
    }

    visitOrderBy(node, args) {
        return this.visitUnaryOperator(node, args);        
    }

    visitFilter(node, args) {
        return this.visitUnaryOperator(node, args);
    }

    visitLeftJoin(node, args) {
        var children = [];
        for (var i in node.input) {
            children.push(this.visit(node.input[i], args));
        };
        
        var leftJoinNode = this.addNode(this.LEFTJOIN);
        leftJoinNode.size = "0.75em";
        Object.assign(leftJoinNode, args);
        
        for (var i in children) {
            this.addLink(leftJoinNode, children[i], children.length>1 && (parseInt(i)+1));
        };
        return leftJoinNode;
    }
    
    visitBGP(node, args) {
        var children = [];
        for (var i in node.patterns) {
            children.push(this.visit(node.patterns[i], args));
        };

        if (children.length === 1) {
            return children[0]; // no intermediary nodes
        }
        
        if (this.flatten) { // multijoin
            var joinNode = this.addNode(this.PRODUCT);
            joinNode.size = "1.5em";
            Object.assign(joinNode, args);
            for (var i in children) {
                this.addLink(joinNode, children[i], children.length>1 && (parseInt(i)+1));
            };
            return joinNode;
        } else {
            for (var i = 0; i < children.length - 1; ++i) {
                this.addLink(children[i], children[i+1]);
            };
            return children[0];
        };
    }
    
    visitService(node, args) {
        // Does not exist has a node by itself, but the coloring
        // make it exist
        return this.visit(node.input, {color: (args && args.color) || this.colorHash.hex(node.name.value)});
    }

    visitJoin(node, args) {
        if (node.input.length === 2 &&
            node.input[0].type === "values" &&
            node.input[1].type === "service" &&
            node.input[0].variables[0].value === node.input[1].name.value) {
            // special kind of join
            return this.visitValuesOfServices(node, args);
        };
        
        var joinNode = this.addNode(this.PRODUCT);
        joinNode.size = "1.5em";
        Object.assign(joinNode, args);
              
        var children = [];
        for (var i in node.input) { // already flattened
            children.push(this.visit(node.input[i], args));
        }
        for (var i in children) {
            this.addLink(joinNode, children[i], children.length>1 && (parseInt(i)+1));
        };
        return joinNode;
    }

    visitValuesOfServices(node, args) {
        const values = node.input[0];
        const services = node.input[1];
        var valueToHash = "";
        console.log(values.variables);
        for (var i in values.bindings) {
            console.log(values.bindings[i]);
            valueToHash += values.bindings[i]["?"+values.variables[0].value].value;
        }
        
        var valuesNode = this.addNode("V");
        var args = {color: this.colorHash.hex(valueToHash)};
        Object.assign(valuesNode, args);
        var serviceNode = this.visit(services, args);
        this.addLink(valuesNode, serviceNode, "x"+values.bindings.length);
        return valuesNode;
    }
    

    visitUnion(node, args) {
        var unionNode = this.addNode(this.UNION);
        Object.assign(unionNode, args);
        var children = [];
        for (var i in node.input) { // already flattened
            children.push(this.visit(node.input[i], args));
        }
        for (var i in children) {
            this.addLink(unionNode, children[i]);
        };
        return unionNode;
    }

    visitValues(node, args) {
        return this.addNode("V");
    }

    visitTriple(node, args) {
        const text = this.visitTerm(node.subject) + " " +
              this.visitTerm(node.predicate) + " " +
              this.visitTerm(node.object);
        var tripleNode = this.addNode(text); // TODO
        Object.assign(tripleNode, args); // merges
        return tripleNode;
    }

    visitQuad(node, args) {
        if (node.graph.termType === "DefaultGraph") {
            return this.visitTriple(node, args);
        }
        console.log(node);
        var quadNode = this.addNode(node); // TODO
        Object.assign(quadNode, args); // merges
        return quadNode;
    }

    visitTerm(term) {
        switch (term.termType) {
        case "Variable": return ""; // "?";//+term.value;
        case "NamedNode": return ""; //  "<>";//+term.value+">";
        default: throw new Error("Type " + node.termType + " not implemented…");
        }
    }

    /**
     * Factorizes the common part of visiting a unary operator,
     * i.e. that only has one child operator.
     */
    visitUnaryOperator(node, args) {
        var n = this.addNode(node.type);
        Object.assign(n, args);
        var child = this.visit(node.input, args);
        this.addLink(n.id, child.id);
        return n;        
    }


    
    addNode(type) {
        this.nodes.push(new GraphNode(this.ids, type));
        this.ids += 1;
        return this.nodes[this.nodes.length-1];
    }

    addLink(from, to, label) {
        this.links.push(new GraphLink(from, to, label));
    }

}



class GraphNode {

    constructor(id, type) {
        this.id = id;
        this.type = type || "unknown";
        this.color = "white";
        this.size = "1em";
    }
}

class GraphLink {

    constructor(from, to, label) {
        this.source = from;
        this.target = to;
        this.label = label || "";
    }

}
