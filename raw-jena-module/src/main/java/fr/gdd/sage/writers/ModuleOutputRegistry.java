package fr.gdd.sage.writers;

import org.apache.jena.riot.Lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sometimes, modules require to output their data in the messages.
 */
public class ModuleOutputRegistry {

    private static final Map<Lang, Set<ModuleOutputWriter>> registry = new HashMap<>();

    /**
     * Add a new writer to the set of writers for this type of output.
     * @param type The type of output. For instance: "JSON".
     * @param writer The writer of the module.
     */
    public static void register(Lang type, ModuleOutputWriter writer) {
        registry.putIfAbsent(type, new HashSet<>());
        registry.get(type).add(writer);
    }

    /**
     * Remove the writer of the set of writers.
     * @param type The type of output. For instance: "JSON".
     * @param writer The writer of the module.
     */
    public static void unregister(Lang type, ModuleOutputWriter writer) {
        if (registry.containsKey(type)) {
            registry.get(type).remove(writer);
        }
    }

    /**
     * @param type The type of output. For instance: "JSON".
     * @return The set of writers for the type of output.
     */
    public static Set<ModuleOutputWriter> getWriters(Lang type) {
        return registry.getOrDefault(type, new HashSet<>());
    }

}
