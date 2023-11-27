package WeatherWear.com;

//IMPORTS

import edu.um.cps3230.RetrieveWeather;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetrieveWeatherTests {

    @Test
    void testGetFutureWeather_InvalidJsonResponse() throws IOException {
        HttpClient httpClient = mock(HttpClient.class); //mock HttpClient (invalid JSON response)
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);

        //setup --> Invalid JSON
        String invalidJSONResponse = "This is not a valid JSON response";
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(invalidJSONResponse.getBytes(StandardCharsets.UTF_8)));

        //mock execution of request
        when(httpClient.execute(any())).thenReturn(httpResponse);

        //weatherService with mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);

        //get future weather recommendation for mocked iata code and date
        String airportCode = "JFK";
        String dateOfArrival = "2023-12-4";
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        //comparison & verification
        assertEquals("Unable to fetch weather data.", weatherRecommendation);
    }

    @Test
    void testGetCurrentWeather_NullEntity() throws IOException {
        //mock  HttpClient (null HttpEntity)
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        //mock execution
        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(null);

        //create WeatherService with mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);

        //get current weather for mocked IP
        String ipAddress = "192.168.1.1";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        assertEquals("Unable to fetch weather data.", weatherInfo); //comparison & verification
    }

    @Test
    void testGetFutureWeather_NullEntity() throws IOException {
        //mock  HttpClient (return null HttpEntity)
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        //mock execution of request
        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(null);

        //WeatherService with mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);

        //get future weather recommend for mocked iata code and date
        String airportCode = "JFK";
        String dateOfArrival = "2023-11-30";
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        assertEquals("Unable to fetch weather data.", weatherRecommendation); //comparison & verification
    }

    @Test
    void testGetCurrentWeatherSuccessful_withMockedHTTPClient() throws IOException {
        HttpClient httpClient = mock(HttpClient.class); //mock HttpClient
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);

        //setup --> expected JSON
        String expectedJSONResponse = "{\"current\":{\"temp_c\":20.0,\"precip_mm\":5.0}}";


        when(httpResponse.getEntity()).thenReturn(httpEntity);//mocking behavior
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(expectedJSONResponse.getBytes(StandardCharsets.UTF_8))); // Use ByteArrayInputStream

        when(httpClient.execute(any())).thenReturn(httpResponse);//mock execution of HTTP request

        //WeatherService with mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient); // Set the mocked HttpClient

        //get current weather for mocked IP
        String ipAddress = "192.168.1.1";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //comparison & verification
        String expectedWeatherInfo = "It is warm so you should wear light clothing.\nIt is currently raining so you do need an umbrella.";
        assertEquals(expectedWeatherInfo, weatherInfo);
    }

    @Test
    void testGetFutureWeatherRecommendationSuccessful_withMockedHTTPClient() throws IOException {
        //mock HttpClient
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);

        //set up --> expected JSON 
        String expectedJSONResponse = "{\"current\":{\"temp_c\":25.0,\"precip_mm\":0.0}}";

        //mocking
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(expectedJSONResponse.getBytes(StandardCharsets.UTF_8))); // Use ByteArrayInputStream

        //mock the execution of the HTTP request
        when(httpClient.execute(any())).thenReturn(httpResponse);

        //create WeatherService with the mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient); // Set the mocked HttpClient

        //get future weather recommendation for mocked airport code and date
        String airportCode = "MLT"; // Mocked airport code
        String dateOfArrival = "2023-11-30"; // Mocked date
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        //comparison & verification
        String expectedRecommendation = "It is warm so you should wear light clothing.\nIt is not raining so you don't need an umbrella.";
        assertEquals(expectedRecommendation, weatherRecommendation);
    }

    @Test
    void testGetFutureWeather_NetworkError() throws IOException {
        // Mock the HttpClient to throw an IOException when executed
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any())).thenThrow(IOException.class);

        // Create WeatherService with the mocked HttpClient
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient); // Set the mocked HttpClient

        // Exercise: Get future weather recommendation when a network error occurs
        String airportCode = "LHR"; // Mocked airport code
        String dateOfArrival = "2023-11-30"; // Mocked date
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        //comparison & verification
        assertEquals("Unable to fetch weather data.", weatherRecommendation);
    }


    //The two mehods below have both been tested, within the mainTests file and here also

    @Test
    void testGenerateWeather_WarmAndRaining() {
        //set up variables
        double temperature = 20.0;
        double precipitation = 5.0;

        String recommendation = RetrieveWeather.generateWeatherRecommendation(temperature, precipitation);

        //comparison & verification
        String expectedRecommendation = "It is warm so you should wear light clothing.\nIt is currently raining so you do need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void testGenerateWeather_ColdAndNotRaining() {
        //set up variables
        double temperature = 7.0;
        double precipitation = 0.0;

        String recommendation = RetrieveWeather.generateWeatherRecommendation(temperature, precipitation);

        //comparison & verification
        String expectedRecommendation = "It is cold so you should wear warm clothing.\nIt is not raining so you don't need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void testGetCurrentWeather_HttpStatusCode404() throws IOException {
        //mock HttpClient (simulate a 404 HTTP)
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);

        //setup -> 404 status code
        when(statusLine.getStatusCode()).thenReturn(404);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        RetrieveWeather weatherService = new RetrieveWeather();//WeatherService with the mocked HttpClient
        weatherService.setHttpClient(httpClient);

        //current weather for mocked IP
        String ipAddress = "123.456.7.8";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //comparison & verification (Check if the method returns an appropriate message for 404 status)
        assertEquals("Unable to fetch weather data.", weatherInfo);
    }

    @Test
    void testGetCurrentWeather_ServiceUnavailable() throws IOException {
        //mock  HttpClient - simulate service unavailability (connection refused)
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any())).thenThrow(HttpHostConnectException.class);
        RetrieveWeather weatherService = new RetrieveWeather(); //create WeatherService with mocked HttpClient
        weatherService.setHttpClient(httpClient);

        //get current weather for mocked IP
        String ipAddress = "123.456.8.9";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //comparison & verification (check if  method returns appropriate message for unavailability)
        assertEquals("Unable to fetch weather data.", weatherInfo);
    }


    @Test
    void testGetCurrentWeather_IOError() throws IOException {
        //mock HttpClient (throw IOException upon execute)
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any())).thenThrow(IOException.class);

        //create WeatherService with mocked client
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);

        //get current weather (IO error occurs)
        String ipAddress = "123.456.7.8";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //comparison & verification (check if returns appropriate message for IO err)
        assertEquals("Unable to fetch weather data.", weatherInfo);
    }


    @Test
    void testGetCurrentWeather_InvalidJsonResponse() throws IOException {
        //mock HttpClient (return invalid JSON)
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);

        //setup --> Invalid JSON
        String invalidJSONResponse = "This is not a valid JSON response";
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(invalidJSONResponse.getBytes(StandardCharsets.UTF_8)));

        //mock execution of HTTP
        when(httpClient.execute(any())).thenReturn(httpResponse);

        //create WeatherService with mocked Client
        RetrieveWeather weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);

        //get current weather for mocked IP
        String ipAddress = "192.168.1.1";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //comparison & verification (method returns appropriate message for JSON invalid)
        assertEquals("Unable to fetch weather data.", weatherInfo);
    }


}


