package fr.gdd.sage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.persistent.Watdiv10M;
import fr.gdd.sage.io.SageInput;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class SageFusekiServerTest {
    private static Logger log = LoggerFactory.getLogger(SageFusekiServerTest.class);

    @Disabled
    @Test
    public void start_a_server_and_run_a_query_on_it() throws IOException, ParserConfigurationException, SAXException {
        long LIMIT = 999;

        Watdiv10M watdiv = new Watdiv10M(Optional.of("../target/"));
        FusekiServer server = SageFusekiServer.buildServer(watdiv.dbPath_asStr, null);
        server.start();

        String url_asString = server.serverURL() + "watdiv10M/query";
        log.debug("URL to query: {}", url_asString);

        loopSendReceive(url_asString,
                "SELECT * WHERE {?v0 <http://purl.org/goodrelations/includes> ?v1.}", LIMIT);

        server.stop();
    }

    static public void loopSendReceive(String url, String query, long LIMIT) throws IOException {
        // creating the post request piggybacking query and sage-specific input
        long numberOfResults = 0;
        long numberOfPreempt = 0;
        JsonNode output = null;
        do {
            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "application/sparql-results+json");
            SageInput<?> sageInput = new SageInput<>().setLimit(LIMIT);

            var mapper = new ObjectMapper();
            String sageInput_asJson = null;
            try {
                sageInput_asJson = mapper.writeValueAsString(sageInput);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            log.info("Input to send to Sage: {}", sageInput_asJson);

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(
                    new BasicNameValuePair("query", query));
            urlParameters.add(new BasicNameValuePair(SageConstants.input.getSymbol(), sageInput_asJson));
            if (Objects.nonNull(output)) {
                urlParameters.add(new BasicNameValuePair(SageConstants.output.getSymbol(), output.textValue()));
            }

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            String result = "";
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {
                result = EntityUtils.toString(response.getEntity());
            }

            JsonNode nameNode = mapper.readTree(result);

            output = nameNode.get("SageModule");

            JsonNode results = nameNode.get("results");
            JsonNode bindings = Objects.nonNull(results) ? results.get("bindings") : null;
            if (Objects.nonNull(bindings)) {
                numberOfResults += bindings.size();
            }
            numberOfPreempt +=1;
        } while (Objects.nonNull(output));

        log.info("Number of preempts {}", numberOfPreempt-1);
        log.info("Number of results {}", numberOfResults);
    }

}