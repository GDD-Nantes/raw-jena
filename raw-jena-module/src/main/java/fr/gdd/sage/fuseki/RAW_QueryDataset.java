package fr.gdd.sage.fuseki;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.SageInput;
import fr.gdd.sage.io.SageOutput;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.jena.ext.xerces.impl.dv.util.Base64;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Objects;

/**
 * The processor meant to replace the actual query of dataset. Does
 * the same job but reads and writes http header to enable
 * pausing/resuming query execution.
 */
public class RAW_QueryDataset extends SPARQL_QueryDataset {

    @Override
    protected void execute(String queryString, HttpAction action) {
        // #1 read the input from incoming http action
        String inputRetrievedFromRequest = getFromBodyOrHeader(RAWConstants.input.getSymbol(), action);

        // Deserialize from JSON
        SageInput<Serializable> sageInput = new SageInput<>();
        if (Objects.nonNull(inputRetrievedFromRequest)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                sageInput = mapper.readValue(inputRetrievedFromRequest, SageInput.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // #B the resuming state of the query execution if present in body
        String outputRetrievedFromRequest = getFromBodyOrHeader(SageConstants.output.getSymbol(), action);

        // base64 -> deserialize
        if (Objects.nonNull(outputRetrievedFromRequest)) {
            byte[] decoded = Base64.decode(outputRetrievedFromRequest);
            SageOutput<Serializable> sagePreviousOutput = SerializationUtils.deserialize(decoded);
            sageInput.setState(sagePreviousOutput.getState());
        }

        // #2 put the deserialized input in the execution context
        if (Objects.nonNull(inputRetrievedFromRequest)) {
            action.getContext().set(SageConstants.limit, sageInput.getLimit());
            action.getContext().set(SageConstants.timeout, sageInput.getTimeout());
        }
        if (Objects.nonNull(outputRetrievedFromRequest)) {
            action.getContext().set(SageConstants.state, sageInput.getState());
        }
        // cleanup context
        action.getContext().remove(SageConstants.input);
        action.getContext().remove(SageConstants.output);

        super.execute(queryString, action);
    }

    // If at some point, the saved state need to be sent in the
    // headers, it could be done by @overriding
    // `sendResults(HttpAction action, SPARQLResult result, Prologue
    // qPrologue)`


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
