package fr.gdd.sage.fuseki;

import fr.gdd.sage.arq.OpExecutorSage;
import fr.gdd.sage.arq.QueryEngineSage;
import fr.gdd.sage.writers.ExtensibleRowSetWriterJSON;
import fr.gdd.sage.writers.ModuleOutputRegistry;
import fr.gdd.sage.writers.OutputWriterJSONSage;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetWriterRegistry;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.DatabaseMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Module in charge of replacing fuseki's normal behavior for `query`
 * by one that enables preemptive evaluation of queries, i.e. one that
 * enables pausing/resuming of query execution on demand, depending on
 * arguments in http headers.
 * 
 * The module simply sets the processor of `Operation.QUERY` to ours,
 * for every dataset and endpoint.
 *
 * For this to work, either set full class name in a
 * `META-INF/…/…FusekiModule` file as per se in documentation, or
 * work with `addModule`.
 */
public class SageModule implements FusekiModule {
    Logger logger = LoggerFactory.getLogger(SageModule.class);

    public SageModule() {}
    
    @Override
    public String name() {
        return "Sage";
    }
    
    @Override
    public void start() {
        logger.info("start !");
        // for now, we simply register our preemptive query engine.
        QueryEngineSage.register();

        // replace the output by ours to include the saved state.
        // all writers are here : <https://github.com/apache/jena/tree/main/jena-arq/src/main/java/org/apache/jena/riot/rowset/rw>
        // (TODO) get them all
        RowSetWriterRegistry.register(ResultSetLang.RS_JSON, ExtensibleRowSetWriterJSON.factory);
        ModuleOutputRegistry.register(ResultSetLang.RS_JSON, new OutputWriterJSONSage());
    }

    /**
     * Server starting - called just before server.start happens.
     */
    @Override
    public void serverBeforeStarting(FusekiServer server) {
        logger.info("Patching the processor for query operations…");

        for (var dap : server.getDataAccessPointRegistry().accessPoints()) {
            if (DatabaseMgr.isTDB2(dap.getDataService().getDataset())) {
                // register the new executors for every dataset that is TDB2
                logger.info("Creating the executor of {}…", dap.getName());
                Dataset ds =  DatasetFactory.wrap(dap.getDataService().getDataset());
                // ARQ context is erased by Dataset specific context
                Context sageContext = ARQ.getContext().copy();
                sageContext.putAll(ds.getContext());
                QC.setFactory(ds.getContext(), new OpExecutorSage.OpExecutorSageFactory(sageContext));
            }
            
            // replacing the operation registry and the processor
            server.getOperationRegistry().register(Operation.Query, new Sage_QueryDataset());
            for (Endpoint ep : dap.getDataService().getEndpoints(Operation.Query)) {
                logger.info("Patching the QUERY processor for {}", ep.getName());
                ep.setProcessor(server.getOperationRegistry().findHandler(ep.getOperation()));
            }
        }
    }
    
    @Override
    public void serverStopped(FusekiServer server) {
        // (TODO) maybe put back the default behavior        
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
