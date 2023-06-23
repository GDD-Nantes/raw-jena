package fr.gdd.sage;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.persistent.Watdiv10M;
import fr.gdd.sage.fuseki.SageModule;
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
 * A Fuseki server using Sage.
 *
 * Note: This is a usage example of {@link fr.gdd.sage.fuseki.SageModule} within
 * an embedded Fuseki server. It does not aim to be an actual server. For this,
 * you need to implement your own and register SageModule as module.
 **/
public class SageFusekiServer {

    @CommandLine.Option(names = "--database",
            description = "The path to your TDB2 database. Note: If none is set, it downloads Watdiv10M.")
    public String database;

    @CommandLine.Option(names = "--ui", description = "The path to your UI folder.")
    public String ui;

    @CommandLine.Option(names = {"-v", "--verbosity"},
            description = "The verbosity level (ALL, INFO, FINE).")
    public String verbosity;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean usageHelpRequested;

    public static void main( String[] args ) {
        SageFusekiServer serverOptions = new SageFusekiServer();
        new CommandLine(serverOptions).parse(args);

        if (serverOptions.usageHelpRequested) {
            CommandLine.usage(new SageFusekiServer(), System.out);
            return;
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
                default -> {
                    System.out.println("Option for verbosity not recognized.");
                    System.exit(1);
                }
            }
        }

        FusekiServer server = buildServer(serverOptions.database, serverOptions.ui);
        server.start();
    }

    /**
     * Build a Sage fuseki server.
     * @param database The path to the TDB2 database.
     * @param ui The path to the ui.
     * @return A fuseki server not yet running.
     */
    static FusekiServer buildServer(String database, String ui) {
        Dataset dataset = TDB2Factory.connectDataset(database);
        dataset.getContext().set(SageConstants.limit, 100000);
        dataset.getContext().set(SageConstants.timeout, 5000);

        FusekiModules.add(new SageModule());

        FusekiServer.Builder serverBuilder = FusekiServer.create()
                // .parseConfigFile("configurations/sage.ttl")
                .enablePing(true)
                .enableCompact(true)
                // .enableCors(true)
                .enableStats(true)
                .enableTasks(true)
                .enableMetrics(true)
                .numServerThreads(1, 10)
                // .loopback(false)
                .serverAuthPolicy(Auth.ANY_ANON)
                .addProcessor("/$/server", new ActionServerStatus())
                //.addProcessor("/$/datasets/*", new ActionDatasets())
                .add(Path.of(database).getFileName().toString(), dataset)
                // .auth(AuthScheme.BASIC)
                .addEndpoint(Path.of(database).getFileName().toString(),
                        Path.of(database).getFileName().toString(),
                        Operation.Query, Auth.ANY_ANON);

        if (Objects.nonNull(ui)) { // add UI if need be
            serverBuilder.staticFileBase(ui);
        }

        return serverBuilder.build();

    }
}
