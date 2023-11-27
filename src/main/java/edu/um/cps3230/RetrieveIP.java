package edu.um.cps3230;


//IMPORTS

import Interfaces.IRetrieveIP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveIP implements IRetrieveIP {

    String search = "query";
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public RetrieveIP(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private static final String URL1 = "http://ip-api.com/json/";
    private static final String URL2 = "http://api.weatherapi.com/v1/ip.json?key=b6bc7bc6576547bd9c793817232511&q=auto:ip";


    private final HttpClient httpClient;
    private ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(RetrieveIP.class.getName());



    public String fetchIp() {
        try {
            String ipAddress = fetchIPAddress();

            if ("F".equals(ipAddress)) {
                ipAddress = fetchIPAddressBackup();
                search = "ip";

                if ("F".equals(ipAddress)) {
                    return "ERROR - IP fetch [BOTH SERVICES] failed";
                }
            }

            return ipAddress;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch IP address.", e);
            return "ERROR - IP fetch failed";
        }
    }

    private String fetchIPAddress() throws IOException {
        System.out.println(URL1);
        HttpGet request = createHttpGetRequest(URL1);
        request.setConfig(RequestConfig.custom()
                .setConnectTimeout(1000)
                .build());

        return execute(request);
    }


    private String fetchIPAddressBackup() throws IOException {
        System.out.println(URL2);//just to confirm api functinaloty
        HttpGet request = createHttpGetRequest(URL2);
        return execute(request);
    }

    private HttpGet createHttpGetRequest(String url) {
        return new HttpGet(url);
    }

    public String execute(HttpGet request) {
        try {
            org.apache.http.HttpResponse response = httpClient.execute(request);
            String content = EntityUtils.toString(response.getEntity());//extract from response
            return JSONExtract(content);
        } catch (IOException e) {
            //log  exceptions
            LOGGER.log(Level.SEVERE, "Execution Failed", e);
            //error message in case of failure
            return "ERROR - IP fetch failed";
        }
    }


    public String JSONExtract(String json) {
        try {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }

            JsonNode jsonNode = objectMapper.readTree(json);
            JsonNode queryNode = jsonNode.get(search);

            if (queryNode != null) {
                return queryNode.asText();
            } else {
                LOGGER.log(Level.WARNING, "Service Failed");
                return "F";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "JSON extracting failed", e);
            return "F";
        }
    }




    public String getNodeValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            return nodeList.item(i).getTextContent();
        }
        return "F";
    }

}

