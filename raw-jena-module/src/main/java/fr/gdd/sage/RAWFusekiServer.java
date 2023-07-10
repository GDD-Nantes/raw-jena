package fr.gdd.sage;

import fr.gdd.sage.databases.persistent.Watdiv10M;
import fr.gdd.sage.fuseki.RAWModule;
import org.apache.jena.dboe.trans.bplustree.ProgressJenaIterator;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.ActionServerStatus;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.mgt.Explain.InfoLevel;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A Fuseki server using RAW (RAndom Walks for query sampling)
 * <br />
 * Note: This is a usage example of {@link RAWModule} within
 * an embedded Fuseki server. It does not aim to be an actual server. For this,
 * you need to implement your own and register SageModule as module.
 **/
public class RAWFusekiServer {

    @CommandLine.Option(names = "--database",
            description = "The path to your TDB2 database (default: downloads Watdiv10M).")
    public String database;

    @CommandLine.Option(names = "--port", description = "The port that gives access to the database (default: 3330).")
    public Integer port;
    private static Integer DEFAULT_PORT = 3330;

    @CommandLine.Option(names = "--limit", description = "The maximum number of random walks per query (default: 10K).")
    public Long limit;
    private static Long DEFAULT_LIMIT = 10000L;

    @CommandLine.Option(names = "--timeout", description = "The maximal duration of random walks (default: 60K ms).")
    public Long timeout;
    private static Long DEFAULT_TIMEOUT = 60000L;

    @CommandLine.Option(names = "--ui", description = "The path to your UI folder (default: None).")
    public String ui;

    @CommandLine.Option(names = {"-v", "--verbosity"}, description = "The verbosity level (ALL, INFO, FINE) (default: None).")
    public String verbosity;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean usageHelpRequested;

    /* **************************************************************************** */

    public static void main( String[] args ) {
        RAWFusekiServer serverOptions = new RAWFusekiServer();
        new CommandLine(serverOptions).parse(args);

        if (serverOptions.usageHelpRequested) {
            CommandLine.usage(new RAWFusekiServer(), System.out);
            return;
        }

        if (Objects.isNull(serverOptions.timeout)) {
            // as in most public endpoints
            serverOptions.timeout = DEFAULT_TIMEOUT;
        }

        if (Objects.isNull(serverOptions.limit)) {
            // as in dbpedia
            serverOptions.limit = DEFAULT_LIMIT;
        }

        if (Objects.isNull(serverOptions.port)) {
            serverOptions.port = DEFAULT_PORT;
        }

        if (Objects.isNull(serverOptions.database)) {
            Watdiv10M watdiv = new Watdiv10M(Optional.empty());
            serverOptions.database = watdiv.dbPath_asStr;
        }

        if (Objects.isNull(serverOptions.verbosity)) {
            ARQ.setExecutionLogging(InfoLevel.NONE);
        } else {
            switch (serverOptions.verbosity) {
                case "ALL" -> ARQ.setExecutionLogging(InfoLevel.ALL);
                case "INFO" -> ARQ.setExecutionLogging(InfoLevel.INFO);
                case "FINE" -> ARQ.setExecutionLogging(InfoLevel.FINE);
                case "NONE" -> ARQ.setExecutionLogging(InfoLevel.NONE);
                default -> {
                    System.out.println("Option for verbosity not recognized.");
                    System.exit(1);
                }
            }
        }

        Dataset dataset = TDB2Factory.connectDataset(serverOptions.database);
        dataset.getContext().setIfUndef(RAWConstants.limit, serverOptions.limit);
        dataset.getContext().setIfUndef(RAWConstants.timeout, serverOptions.timeout);

        FusekiServer server = buildServer(serverOptions.database, dataset, serverOptions.port, serverOptions.ui);
        server.start();
    }

    /**
     * Build a random walk fuseki server.
     * @param datasetPath The path to the TDB2 database.
     * @param ui The path to the ui.
     * @return A fuseki server not yet running.
     */
    static FusekiServer buildServer(String datasetPath, Dataset dataset, Integer port, String ui) {
        FusekiModules.add(new RAWModule());

        ProgressJenaIterator.NB_WALKS = 42; // (TODO) let it be configurable

        FusekiServer.Builder serverBuilder = FusekiServer.create()
                // .parseConfigFile("configurations/sage.ttl")
                .enablePing(true)
                .enableCompact(true)
                .enableCors(true)
                .enableStats(true)
                .enableTasks(true)
                .enableMetrics(true)
                .port(port)
                .numServerThreads(1, 10)
                // .loopback(false)
                .serverAuthPolicy(Auth.ANY_ANON)
                .addProcessor("/$/server", new ActionServerStatus())
                //.addProcessor("/$/datasets/*", new ActionDatasets())
                .add(Path.of(datasetPath).getFileName().toString(), dataset)
                // .auth(AuthScheme.BASIC)
                .addEndpoint(Path.of(datasetPath).getFileName().toString(),
                        Path.of(datasetPath).getFileName().toString(),
                        Operation.Query, Auth.ANY_ANON);

        if (Objects.nonNull(ui)) { // add UI if need be
            serverBuilder.staticFileBase(ui);
        }

        return serverBuilder.build();

    }
}
