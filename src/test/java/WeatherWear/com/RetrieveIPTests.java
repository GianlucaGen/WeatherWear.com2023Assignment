package WeatherWear.com;

//IMPORTS

import edu.um.cps3230.RetrieveIP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class RetrieveIPTests {
    private RetrieveIP ServiceIP;
    private Document mockedDocument;


    @Test
    void testFetchIPAddress_SuccessfulPrimaryRequest() throws Exception {
        //expected JSON
        String expectedJSONResponse = "{\"query\":\"123.4.5.678\"}";

        // Set up the HttpClient and relevant objects
        HttpClient httpClient = mock(HttpClient.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);

        //simulate mocked objs
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(expectedJSONResponse.getBytes()));
        when(response.getEntity()).thenReturn(httpEntity);

        //execute request
        when(httpClient.execute(any())).thenReturn(response);
        //instance of RetrieveIP with mock
        RetrieveIP ipService = new RetrieveIP(httpClient);
        //test perform
        String ipAddress = ipService.fetchIp();
        //ensure fetched IP Address same as expected value
        assertEquals("123.4.5.678", ipAddress);
    }



    @Test
    void testSuccessfulJSON() throws IOException {
        String jsonResponse = "{\"query\":\"123.4.5.678\"}";// Mock JSON
        JsonNode jsonNode = mock(JsonNode.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        when(objectMapper.readTree(any(String.class))).thenReturn(jsonNode);
        when(jsonNode.get("query")).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("123.4.5.678");

        //mock instance of RetrieveIP
        RetrieveIP ipService = new RetrieveIP(mock(HttpClient.class));
        ipService.setObjectMapper(objectMapper);

        //perform test
        String ipAddress = ipService.JSONExtract(jsonResponse);

        assertEquals("123.4.5.678", ipAddress); //verification
    }



    @Test
    void testGetIPAddressWithHTTPException() throws IOException {
        //mock the HttpClient (throw IOException when execute)
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any())).thenThrow(IOException.class);

        //create RetrieveIP
        RetrieveIP ipService = new RetrieveIP(httpClient);

        //get IP address when HTTP request exception
        String ipAddress = ipService.fetchIp();

        //verification
        assertTrue(ipAddress.equals("ERROR - IP fetch failed") || ipAddress.equals("F"));
    }



    @BeforeEach
    void setup() {
        mockedDocument = Mockito.mock(Document.class);
        ServiceIP = new RetrieveIP(null);
    }

    @Test
    void getNodeValue_WhenValidTagNameProvided_ReturnsExpectedValue() {
        //arranging
        NodeList mockedNodeList = Mockito.mock(NodeList.class);
        Node mockedNode = Mockito.mock(Node.class);
        when(mockedDocument.getElementsByTagName("query")).thenReturn(mockedNodeList);
        when(mockedNodeList.getLength()).thenReturn(1);
        when(mockedNodeList.item(0)).thenReturn(mockedNode);
        when(mockedNode.getTextContent()).thenReturn("ExpectedValue");
        //call method tested
        String value = ServiceIP.getNodeValue(mockedDocument, "query");
        //verification
        assertEquals("ExpectedValue", value);
    }


    @Test
    void getNodeValue_NoMatchingTag_ReturnsF() {
        //arrange --> Mock NodeList when  no matching tag
        NodeList emptyNodeList = Mockito.mock(NodeList.class);
        when(mockedDocument.getElementsByTagName("empty")).thenReturn(emptyNodeList);
        when(emptyNodeList.getLength()).thenReturn(0);
        //call method being tested
        String result = ServiceIP.getNodeValue(mockedDocument, "empty");
        //assert --> verify result "F"
        assertEquals("F", result);
    }


    @Test
    void fetchIp_WhenNetworkErrorOccurs_ReturnsErrorMessage() throws IOException {
        //arrange --> Mock HTTP client (simulate network error)
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException());
        RetrieveIP serviceIP = new RetrieveIP(httpClient);
        // call method tested
        String IP = serviceIP.fetchIp();
        //verify result
        assertEquals("ERROR - IP fetch failed", IP);
    }


    @Test
    void execute_WhenIOExceptionOccurs_ReturnsErrorMessage() throws IOException {
        //arrange --> mocked HttpClient
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        //create instance of IPService
        RetrieveIP ipService = new RetrieveIP(httpClient);
        //create mocked request
        HttpGet request = Mockito.mock(HttpGet.class);
        //act --> Simulate IOException
        when(httpClient.execute(request)).thenThrow(IOException.class);
        //act --> setup expectations for behavior
        String result = ipService.execute(request);
        //verification
        assertEquals("ERROR - IP fetch failed", result);
    }

}