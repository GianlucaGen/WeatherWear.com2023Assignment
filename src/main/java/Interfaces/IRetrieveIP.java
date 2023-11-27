package Interfaces;

import java.io.IOException;

public interface IRetrieveIP {

    //get IP
    String fetchIp() throws IOException;


    //extract infor from JSON
    String JSONExtract(String json) throws IOException;


    //extract content using tag name
    String getNodeValue(org.w3c.dom.Document document, String tagName) throws IOException;
}

