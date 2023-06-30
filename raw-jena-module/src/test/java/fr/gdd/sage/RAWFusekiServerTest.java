package fr.gdd.sage;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RAWFusekiServerTest {
    private static Logger log = LoggerFactory.getLogger(RAWFusekiServerTest.class);

    @EnabledIfEnvironmentVariable(named = "WATDIV", matches = "true")
    @Test
    public void start_a_server_and_run_a_query_on_it() throws IOException, ParserConfigurationException, SAXException {
        long LIMIT = 2;

        Watdiv10M watdiv = new Watdiv10M(Optional.of("../target/"));
        Dataset dataset = TDB2Factory.connectDataset(watdiv.dbPath_asStr);
        FusekiServer server = RAWFusekiServer.buildServer(watdiv.dbPath_asStr, dataset, null);
        server.start();

        String url_asString = server.serverURL() + "watdiv10M/query";
        log.debug("URL to query: {}", url_asString);

        // creating the post request piggybacking query and sage-specific input
        HttpPost post = new HttpPost(url_asString);
        SageInput<?> sageInput = new SageInput<>().setLimit(LIMIT); // only get LIMIT random walk(s) please


        var mapper = new ObjectMapper();
        String sageInput_asJson = null;
        try {
            sageInput_asJson = mapper.writeValueAsString(sageInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.debug("Input to random walk server to send: {}", sageInput_asJson);


        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(
                new BasicNameValuePair("query", "SELECT * WHERE {	?v0 <http://purl.org/goodrelations/includes> ?v1.}"));
        urlParameters.add(new BasicNameValuePair(SageConstants.input.getSymbol(), sageInput_asJson));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)){
            result = EntityUtils.toString(response.getEntity());
        }

        log.debug("Result : {}", result);
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(result)));
        NodeList xmlResults = document.getElementsByTagName("result");
        assertEquals(LIMIT, xmlResults.getLength());

        server.stop();
    }

}