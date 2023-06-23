package fr.gdd.sage.writers;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.util.Context;

import java.io.Serializable;

/**
 * Write the output of registered modules.
 */
public interface ModuleOutputWriter {

    void write(IndentedWriter writer, Context context);

}
