package edu.um.cps3230;


//IMPORTS
import Interfaces.IRetrieveWeather;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class RetrieveWeather implements IRetrieveWeather {
    private static final String API_KEY = "f7d83a7b1amsh4be5404f539cc83p19261ajsn90b02ebe9371";
    private static final String API_URL = "https://weatherapi-com.p.rapidapi.com/";
    private HttpClient httpClient;

    // Constructor initializing the HTTP client
    public RetrieveWeather() {
        this.httpClient = HttpClients.createDefault();
    }

    // Setter method for providing a custom HTTP client
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static String generateWeatherRecommendation(double temp, double perc) {
        String var1, var2, vara, varb;

        if (temp < 15.0) {
            var1 = "cold";
            var2 = "warm";
        } else {
            var1 = "warm";
            var2 = "light";
        }

        if (perc == 0.0) {
            vara = "not";
            varb = "don't";
        } else {
            vara = "currently";
            varb = "do";
        }

        return String.format("It is " + var1 + " so you should wear " + var2 + " clothing.\nIt is " + vara + " raining so you " + varb + " need an umbrella.");
    }


    // Method to get current weather based on IP address
    public String getCurrentWeather(String ipAddress) {

        // Creating an HTTP GET request for current weather
        HttpGet httpGet = createHttpGetRequest(API_URL + "current.json?q=" + ipAddress);
        httpGet.setHeader("X-RapidAPI-Key", API_KEY);

        // Getting weather data using the HTTP request
        return getReco(httpGet);
    }

    // Method to get future weather recommendation based on IATA code and date
    public String getForecastRecommendation(String iataCode, String dateOfArrival) throws IOException {

        // Creating an HTTP GET request for future weather recommendation
        String futWeathApiUrl = API_URL + "forecast.json?q=iata:" + iataCode + "&dt=" + dateOfArrival;
        HttpGet httpGet = createHttpGetRequest(futWeathApiUrl);
        httpGet.setHeader("X-RapidAPI-Key", API_KEY);

        // Getting future weather recommendation using the HTTP request
        return getReco(httpGet);
    }

    // Creating an HTTP GET request
    private HttpGet createHttpGetRequest(String url) {
        return new HttpGet(url);
    }

    // Method to fetch weather data using the HTTP request
    public String getReco(HttpGet httpGet) {
        try {
            HttpResponse response = httpClient.execute(httpGet); // Executing the HTTP GET request
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper objectMapper = new ObjectMapper(); // Creating an ObjectMapper instance for JSON parsing
                JsonNode jsonNode = objectMapper.readTree(entity.getContent()); // Parsing the JSON response

                double temperature = getTemp(jsonNode); // Getting temperature from the JSON response
                double precipitation = getPrec(jsonNode); // Getting precipitation from the JSON response

                return generateWeatherRecommendation(temperature, precipitation); // Generating weather recommendation based on temperature and precipitation
            }

            return "Unable to fetch weather data."; // Return message if weather data couldn't be fetched
        } catch (IOException e) {
            return "Unable to fetch weather data."; // Return message if an IO exception occurs
        }
    }

    public double getPrec(JsonNode jsonNode) {
        return jsonNode.get("current").get("precip_mm").asDouble();
    }

    public double getTemp(JsonNode jsonNode) {
        return jsonNode.get("current").get("temp_c").asDouble();
    }




}

