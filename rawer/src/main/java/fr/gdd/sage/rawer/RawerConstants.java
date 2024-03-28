package fr.gdd.sage.rawer;

import org.apache.jena.sparql.util.Symbol;

public class RawerConstants {

    public static final String systemVarNS = "https://sage.gdd.fr/Rawer#";
    public static final String sageSymbolPrefix = "rawer";

    static public final Symbol BACKEND = allocVariableSymbol("Backend");
    static public final Symbol TIMEOUT = allocConstantSymbol("Timeout");
    static public final Symbol DEADLINE = allocConstantSymbol("Deadline");
    static public final Symbol LIMIT = allocConstantSymbol("Limit");

    /**
     * Symbol in use in the global context.
     */
    public static Symbol allocConstantSymbol(String name) {
        return Symbol.create(systemVarNS + name);
    }

    /**
     * Symbol in use in each execution context.
     */
    public static Symbol allocVariableSymbol(String name) {
        return Symbol.create(sageSymbolPrefix + name);
    }
}
