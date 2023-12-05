
// Creates a D3 graph based on the sparqlalgebrajs plan
export class Plan2Graph {

    visit(node) {
        switch (node.type) {
        case "project": return this.visitProject(node);
        default: throw new Error("Operator " + node.type + " not implemented…");
        }
    }

    visitProject(node) {
        for (child in node.input) {
            this.visit(child);
        };
    }

}
