package fr.gdd.sage.fuseki;

import fr.gdd.raw.QueryEngineRAW;
import fr.gdd.sage.arq.QueryEngineSage;
import fr.gdd.sage.writers.ExtensibleRowSetWriterJSON;
import fr.gdd.sage.writers.ModuleOutputRegistry;
import fr.gdd.sage.writers.OutputWriterJSONRAW;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module in charge of replacing Fuseki's normal behavior for `query`
 * by one that enables users' input.
 * <br />
 * It needs to override the processor's normal behavior by one that will
 * decode/encode the input/output of every http request.
 * <br />
 * For this to work, either set full class name in a
 * `META-INF/…/…FusekiModule` file as per se in documentation, or
 * work with `addModule`.
 */
public class RAWModule implements FusekiModule {
    private static Logger logger = LoggerFactory.getLogger(RAWModule.class);

    public RAWModule() {}
    
    @Override
    public String name() {
        return "Random";
    }
    
    @Override
    public void start() {
        logger.info("start !");
        QueryEngineRAW.register();

        // replace the output by ours to include the saved state.
        // all writers are here : <https://github.com/apache/jena/tree/main/jena-arq/src/main/java/org/apache/jena/riot/rowset/rw>
        // (TODO) get them all
        RowSetWriterRegistry.register(ResultSetLang.RS_JSON, ExtensibleRowSetWriterJSON.factory);
        ModuleOutputRegistry.register(ResultSetLang.RS_JSON, new OutputWriterJSONRAW());
    }

    /**
     * Server starting - called just before server.start happens.
     */
    @Override
    public void serverBeforeStarting(FusekiServer server) {
        logger.info("Patching the processor for query operations…");

        for (var dap : server.getDataAccessPointRegistry().accessPoints()) {
            // replacing the operation registry and the processor
            server.getOperationRegistry().register(Operation.Query, new RAW_QueryDataset());
            for (Endpoint ep : dap.getDataService().getEndpoints(Operation.Query)) {
                logger.info("Patching the QUERY processor for {}", ep.getName());
                ep.setProcessor(server.getOperationRegistry().findHandler(ep.getOperation()));
            }
        }
    }

    @Override
    public void stop() {
        QueryEngineSage.unregister();
        logger.info("Stop! Have a good day!");
    }

    /**
     * Levels are described in
     * <a href="https://jena.apache.org/documentation/notes/system-initialization.html">Jena initialization page</a>.
     */
    @Override
    public int level() {
        // (TODO) find out the proper level for this module.
        return 43;
    }
}
