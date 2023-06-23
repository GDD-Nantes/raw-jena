package fr.gdd.sage;

import org.apache.jena.sparql.util.Symbol;

/**
 * RAW names of constants and variables to access the values of a context.
 * It mainly revolves around a limit and a timeout to ensure random walks are
 * not looping forever.
 */
public class RAWConstants {
    public static final String systemVarNS = "https://sage.gdd.fr/RAW#";
    public static final String rawSymbolPrefix = "RAW";

    public static Symbol timeout = allocConstantSymbol("Timeout");
    public static Symbol limit   = allocConstantSymbol("Limit");

    public static Symbol output = allocVariableSymbol("Output");
    public static Symbol input = allocVariableSymbol("Input");

    public static Symbol allocConstantSymbol(String name) {
        return Symbol.create(systemVarNS + name);
    }

    public static Symbol allocVariableSymbol(String name) {
        return Symbol.create(rawSymbolPrefix + name);
    }


}
