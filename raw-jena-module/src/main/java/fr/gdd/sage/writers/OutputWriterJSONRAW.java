package fr.gdd.sage.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.fuseki.SageModule;
import fr.gdd.sage.io.RAWOutput;
import fr.gdd.sage.io.RAWOutputAggregated;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.json.io.JSWriter;
import org.apache.jena.sparql.util.Context;

/**
 * Write the results of random walks as JSON.
 */
public class OutputWriterJSONRAW implements ModuleOutputWriter {

    @Override
    public void write(IndentedWriter writer, Context context) {
        RAWOutputAggregated outputAggregated = context.get(RAWConstants.outputAggregated);

        writer.print(" ,");
        writer.print(JSWriter.outputQuotedString(RAWOutputAggregated.class.getSimpleName()));
        writer.println(" : ");

        ObjectMapper mapper2 = new ObjectMapper();
        String outputAggregated_asJSON = null;
        try {
            outputAggregated_asJSON = mapper2.writeValueAsString(outputAggregated);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        writer.println(outputAggregated_asJSON);

        RAWOutput output = context.get(RAWConstants.output);
        writer.print(" ,");
        writer.print(JSWriter.outputQuotedString(RAWOutput.class.getSimpleName()));
        writer.println(" : ");

        ObjectMapper mapper = new ObjectMapper();
        String output_asJSON = null;
        try {
            output_asJSON = mapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        writer.println(output_asJSON);
    }

}
