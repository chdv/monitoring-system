package com.dch.app.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.List;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public class HttpLogServer {

    private Logger logger = LoggerFactory.getLogger(HttpLogServer.class);

    private static final String HOST = "http://127.0.0.1";

    private HttpServer httpServer;

    private AppMain main;

    private Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);

    private ClassTemplateLoader classTemplateLoader =
            new ClassTemplateLoader(
                    HttpLogServer.class, "/freemaker");

    public HttpLogServer(AppMain main) {
        this.main = main;
        cfg.setTemplateLoader(classTemplateLoader);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void start() throws IOException, MalformedURLException {
        int port = Integer.valueOf(JConfig.INSTANCE.getValue("http-server-port"));
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/info", new InfoHandler());
        httpServer.createContext("/graph", new GraphHandler());
        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();
        String url1 = HOST + ':' + port + "/graph";
        String url2 = HOST + ':' + port + "/info";
        logger.debug("see web log at " + url2);
        logger.debug("see graph at " + url2);
        Desktop.getDesktop().browse(URI.create(url2));
        Desktop.getDesktop().browse(URI.create(url1));
    }

    private class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringWriter stringWriter = new StringWriter();
            Map<String, Object> map = new HashMap<>();
            String response = "empty";
            if(main.getLogList().size() > 0) {
                map.put("header", main.getLogList().get(0).keySet());
                List list = new ArrayList(main.getLogList());
                Collections.reverse(list);
                map.put("logList", list);
                try {
                    Template template = cfg.getTemplate("info.ftl");
                    template.process(map, stringWriter);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                response = stringWriter.toString();
            }
//            logger.debug("response" + response);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private Comparator<String> stringComparator = new StringComparator();

    private class GraphHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringWriter stringWriter = new StringWriter();
            Map<String, Object> map = new HashMap<>();
            String response = "empty";
            int size = main.getLogList().size();
            if(size > 0) {
                List<Map<String, String>> subList = new ArrayList(main.getLogList().subList(size/2, main.getLogList().size()));
                Map<String, List<String>> data = new TreeMap<>(stringComparator);
                for(Map<String, String> map1 : subList) {
                    Iterator<Map.Entry<String, String>> it = map1.entrySet().iterator();
                    it.next();
                    while(it.hasNext()) {
                        Map.Entry<String, String> en = it.next();
                        List<String> list = data.get(en.getKey());
                        if(data.get(en.getKey()) == null) {
                            list = new ArrayList<>();
                            data.put(en.getKey(), list);
                        }
                        list.add(en.getValue());
                    }
                }

                map.put("data", data);

                try {
                    Template template = cfg.getTemplate("graph.ftl");
                    template.process(map, stringWriter);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                response = stringWriter.toString();
            }
//            logger.debug("response" + response);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
