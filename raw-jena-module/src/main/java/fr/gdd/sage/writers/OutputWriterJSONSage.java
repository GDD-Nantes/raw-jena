package fr.gdd.sage.writers;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.fuseki.SageModule;
import fr.gdd.sage.io.SageOutput;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.json.io.JSWriter;
import org.apache.jena.ext.xerces.impl.dv.util.Base64;
import org.apache.jena.sparql.util.Context;

/**
 * Write a SageOutput to an out-stream.
 */
public class OutputWriterJSONSage implements ModuleOutputWriter {

    @Override
    public void write(IndentedWriter writer, Context context) {
        SageOutput<?> output = context.get(SageConstants.output);

        if (output == null || output.getState()==null) {
            return;
        }
        writer.print(" ,");
        writer.print(JSWriter.outputQuotedString(SageModule.class.getSimpleName()));
        writer.println(" : ");

        byte[] serialized = SerializationUtils.serialize(output);
        String encoded = Base64.encode(serialized);
        writer.println(JSWriter.outputQuotedString(encoded));
    }

}
