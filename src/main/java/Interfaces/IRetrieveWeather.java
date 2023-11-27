package Interfaces;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface IRetrieveWeather {

    //gets current weather on provided IP
    String getCurrentWeather(String ipAddress) throws IOException;

    //gets weather recommendation for future dat
    String getForecastRecommendation(String iataCode, String dateOfArrival) throws IOException;

    //extracts the temp from JSON node.
    double getTemp(JsonNode jsonNode);

    //extracts the prec from JSON node.
    double getPrec(JsonNode jsonNode);
}