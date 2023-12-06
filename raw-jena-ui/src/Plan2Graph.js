import ColorHash from 'color-hash'

/**
 * Creates a graph based on the sparqlalgebrajs plan meant to
 * be exploited within D3.
 */
export class Plan2Graph {

    constructor() {
        this.ids = 0; // the node identifiers to allocate
        this.nodes = [];
        this.links = [];
        this.colorHash = new ColorHash();
    }
    
    visit(node, args) {
        switch (node.type) {
        case "project": return this.visitProject(node, args);
        case "bgp": return this.visitBGP(node, args);
        case "service": return this.visitService(node, args);
        case "join": return this.visitJoin(node, args);
        case "union": return this.visitUnion(node, args);
        case "pattern":
            switch (node.termType) {
            case "Triple": return this.visitTriple(node, args);
            case "Quad": return this.visitQuad(node, args);
            default: throw new Error("Type " + node.termType + " not implemented…");
            };
        default: throw new Error("Operator " + node.type + " not implemented…");
        }
    }



    visitProject(node, args) {
        var projectNode = this.addNode("π");
        Object.assign(projectNode, args);
        var child = this.visit(node.input, args);
        this.addLink(projectNode.id, child.id);
        return projectNode;
    }

    visitBGP(node, args) {
        var children = [];
        for (var i in node.patterns) {
            children.push(this.visit(node.patterns[i], args));
        };
        for (var i = 0; i < children.length - 1; ++i) {
            this.addLink(children[i], children[i+1]);
        };
        return children[0];
    }

    visitService(node, args) {
        var serviceNode = this.addNode("@");
        serviceNode.color = this.colorHash.hex(node.name.value);
        var child = this.visit(node.input, {color: serviceNode.color});
        this.addLink(serviceNode, child);
        return serviceNode;
    }

    visitJoin(node, args) {
        var joinNode = this.addNode("X");
        Object.assign(joinNode, args);
        var children = [];
        for (var i in node.input) {
            children.push(this.visit(node.input[i], args));
        }
        for (var i in children) {
            this.addLink(joinNode, children[i]);
        };
        return joinNode;
    }

    visitUnion(node, args) {
        var unionNode = this.addNode("U");
        Object.assign(unionNode, args);
        var children = [];
        for (var i in node.input) {
            children.push(this.visit(node.input[i], args));
        }
        for (var i in children) {
            this.addLink(unionNode, children[i]);
        };
        return unionNode;
    }


    visitTriple(node, args) {
        var tripleNode = this.addNode("s p o"); // TODO
        Object.assign(tripleNode, args); // merges
        return tripleNode;
    }

    visitQuad(node, args) {
        var quadNode = this.addNode("g s p o"); // TODO
        Object.assign(quadNode, args); // merges
        return quadNode;
    }


    
    addNode(type) {
        this.nodes.push(new GraphNode(this.ids, type));
        this.ids += 1;
        return this.nodes[this.nodes.length-1];
    }

    addLink(from, to) {
        this.links.push({source: from, target:to});
    }

}


class GraphNode {

    constructor(id, type) {
        this.id = id;
        this.type = type || "unknown";
        this.color = "black";
    }
}
