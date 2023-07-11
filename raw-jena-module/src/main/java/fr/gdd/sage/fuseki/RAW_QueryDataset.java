package fr.gdd.sage.fuseki;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.io.RAWInput;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ResponseResultSet;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * The processor meant to replace the actual query of dataset. Does
 * the same job but reads and writes HTTP queries to enhance user's output
 * with random walks metadata.
 */
public class RAW_QueryDataset extends SPARQL_QueryDataset {

    @Override
    protected void execute(String queryString, HttpAction action) {
        // #1 read the input from incoming HTTP action
        /*String inputRetrievedFromRequest = getFromBodyOrHeader(RAWConstants.input.getSymbol(), action);

        // Deserialize from JSON
        RAWInput rawInput = new RAWInput();
        if (Objects.nonNull(inputRetrievedFromRequest)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                rawInput = mapper.readValue(inputRetrievedFromRequest, RAWInput.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }*/

        String timeoutFromRequest = getFromBodyOrHeader(RAWConstants.argTimeout, action);
        String limitFromRequest = getFromBodyOrHeader(RAWConstants.argLimit, action);
        RAWInput rawInput = new RAWInput(
                Objects.nonNull(timeoutFromRequest) ? Long.parseLong(timeoutFromRequest) : null,
                Objects.nonNull(limitFromRequest) ? Long.parseLong(limitFromRequest) : null);

        action.getContext().set(RAWConstants.input, rawInput);

        super.execute(queryString, action);
    }

    static public String getFromBodyOrHeader(String key, HttpAction action) {
        HttpServletRequest req = action.getRequest();
        // With the body of the request
        String retrievedFromRequest = null;
        if (Objects.nonNull(req.getParameter(key))) {
            retrievedFromRequest = req.getParameter(key);
        }
        // or with the header
        if (Objects.nonNull(req.getHeader(key))) {
            retrievedFromRequest = req.getHeader(key);
        }
        // (TODO) check if exclusive: header xor body
        return retrievedFromRequest;
    }
}
