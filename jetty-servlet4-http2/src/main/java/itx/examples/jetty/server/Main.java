package itx.examples.jetty.server;

import itx.examples.jetty.common.SystemUtils;
import itx.examples.jetty.common.services.EchoService;
import itx.examples.jetty.common.services.MessageServiceAsync;
import itx.examples.jetty.common.services.SystemInfoService;
import itx.examples.jetty.server.rest.RestApplication;
import itx.examples.jetty.server.rest.SystemInfoRest;
import itx.examples.jetty.server.services.EchoServiceImpl;
import itx.examples.jetty.server.services.MessageServiceImpl;
import itx.examples.jetty.server.services.SystemInfoServiceImpl;
import itx.examples.jetty.server.servlet.DataAsyncServlet;
import itx.examples.jetty.server.servlet.DataSyncServlet;
import itx.examples.jetty.server.servlet.EchoServiceServlet;
import itx.examples.jetty.server.servlet.SystemInfoServlet;
import itx.examples.jetty.server.streams.StreamEchoProcessorFactory;
import itx.examples.jetty.server.streams.StreamMessageProcessorFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

public class Main {

    final private static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static Server server;

    public static void main(String[] args) {
        try {
            LOG.info("Jetty server starting ...");
            String password = "secret";
            KeyStore keyStore = SystemUtils.loadJKSKeyStore("server.jks", password);
            String baseUri;
            ServerBuilder serverBuilder = new ServerBuilder();
            MessageServiceAsync messageService = new MessageServiceImpl();
            SystemInfoService systemInfoService = new SystemInfoServiceImpl();
            EchoService echoService = new EchoServiceImpl();

            baseUri = "/data/sync";
            ServletHolder servletHolderDataSync = new ServletHolder(new DataSyncServlet(baseUri, messageService));
            serverBuilder.addServletHolder(baseUri + "/*", servletHolderDataSync);

            baseUri = "/data/async";
            ServletHolder servletHolderDataAsync = new ServletHolder(new DataAsyncServlet(baseUri, messageService));
            serverBuilder.addServletHolder(baseUri + "/*", servletHolderDataAsync);

            baseUri = "/data/echo";
            ServletHolder servletHolderEcho = new ServletHolder(new EchoServiceServlet(baseUri, echoService));
            serverBuilder.addServletHolder(baseUri + "/*", servletHolderEcho);

            baseUri = "/data/system/info";
            ServletHolder servletHolderSystemInfo = new ServletHolder(new SystemInfoServlet(baseUri, systemInfoService));
            serverBuilder.addServletHolder(baseUri + "/*", servletHolderSystemInfo);

            serverBuilder.addStreamProcessorFactory("/stream/echo", new StreamEchoProcessorFactory(echoService));
            serverBuilder.addStreamProcessorFactory("/stream/messages", new StreamMessageProcessorFactory(messageService));

            serverBuilder.setRestUriPrefix("/rest/*");
            serverBuilder.setResourceConfig(new RestApplication(systemInfoService));

            serverBuilder.setStaticResourceBasePath("/web");
            serverBuilder.setStaticResourceBaseUrn("/static/*");

            serverBuilder.setKeyStore(keyStore);
            serverBuilder.setSecureHttpPort(8443);
            serverBuilder.setHttpPort(8080);
            serverBuilder.setKeyStorePassword(password);

            server = serverBuilder.build();
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        server.stop();
                        LOG.info("stopped.");
                    } catch (Exception e) {
                        LOG.error("ERROR: ", e);
                    }
                }
            });
            server.join();
        } catch (Exception e) {
            LOG.error("ERROR: ", e);
        }
    }

}
