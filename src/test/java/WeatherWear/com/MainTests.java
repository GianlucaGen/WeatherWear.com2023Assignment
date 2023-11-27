package WeatherWear.com;

//IMPORTS

import edu.um.cps3230.Main;
import edu.um.cps3230.RetrieveIP;
import edu.um.cps3230.RetrieveWeather;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;



public class MainTests {

    private RetrieveWeather weatherService;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        weatherService = new RetrieveWeather();
        weatherService.setHttpClient(httpClient);
    }

    @Test
    void generateWeatherRecommendation_WarmAndRaining() {
        String recommendation = RetrieveWeather.generateWeatherRecommendation(20.0, 5.0);
        String expectedRecommendation = "It is warm so you should wear light clothing.\nIt is currently raining so you do need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void generateWeatherRecommendation_ColdAndRaining() {
        String recommendation = RetrieveWeather.generateWeatherRecommendation(5.0, 5.0);
        String expectedRecommendation = "It is cold so you should wear warm clothing.\nIt is currently raining so you do need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void generateWeatherRecommendation_WarmAndNotRaining() {
        String recommendation = RetrieveWeather.generateWeatherRecommendation(20.0, 0.0);
        String expectedRecommendation = "It is warm so you should wear light clothing.\nIt is not raining so you don't need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void generateWeatherRecommendation_ColdAndNotRaining() {
        String recommendation = RetrieveWeather.generateWeatherRecommendation(5.0, 0.0);
        String expectedRecommendation = "It is cold so you should wear warm clothing.\nIt is not raining so you don't need an umbrella.";
        assertEquals(expectedRecommendation, recommendation);
    }

    @Test
    void getCurrentWeather_Successful() throws IOException {
        //arrange
        setupHttpResponse("{\"current\":{\"temp_c\":30.0,\"precip_mm\":10.0}}");

        //act
        String ipAddress = "123.456.7.8";
        String weatherInfo = weatherService.getCurrentWeather(ipAddress);

        //assert
        String expectedWeatherInfo = "It is warm so you should wear light clothing.\nIt is currently raining so you do need an umbrella.";
        assertEquals(expectedWeatherInfo, weatherInfo);
    }

    @Test
    void getFutureWeatherRecommendation_Successful() throws IOException {
        // Mock successful HTTP response
        setupHttpResponse("{\"current\":{\"temp_c\":30.0,\"precip_mm\":0.0}}");

        String airportCode = "JFK";
        String dateOfArrival = "2023-11-30";
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        String expectedRecommendation = "It is warm so you should wear light clothing.\nIt is not raining so you don't need an umbrella.";
        assertEquals(expectedRecommendation, weatherRecommendation);
    }

    @Test
    void getFutureWeatherRecommendation_NetworkError() throws IOException {
        // Mock network error
        when(httpClient.execute(any())).thenThrow(IOException.class);

        String airportCode = "LHR";
        String dateOfArrival = "2023-11-30";
        String weatherRecommendation = weatherService.getForecastRecommendation(airportCode, dateOfArrival);

        assertEquals("Unable to fetch weather data.", weatherRecommendation);
    }



    private void setupHttpResponse(String jsonResponse) throws IOException {
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8)));

        when(httpClient.execute(any())).thenReturn(httpResponse);
    }

    @Test
    void testDisplayMenu_ShouldPrintCorrectMenu() {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Act
        Main.displayMenu();

        // Assert
        String capturedOutput = outputStream.toString().trim();

        assertTrue(Pattern.matches(
                "\\s*WeatherWear\\.com\\s*\\n" +
                        "\\s*---------------\\s*\\n" +
                        "\\s*1\\. Recommend clothing for current location\\s*\\n" +
                        "\\s*2\\. Recommend clothing for future location\\s*\\n" +
                        "\\s*3\\. Exit\\s*\\n" +
                        "\\s*Enter choice:\\s*",
                capturedOutput
        ));
    }

    @Test
    void testGetUserChoice_ValidInput_ShouldReturnInput() {
        // Arrange
        String input = "42\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        Scanner scannerMock = Mockito.mock(Scanner.class);

        // Mock necessary behavior for the test
        when(scannerMock.hasNextInt()).thenReturn(true);
        when(scannerMock.nextInt()).thenReturn(42);

        // Act
        int result = Main.getUserChoice(scannerMock);

        // Assert
        String capturedOutput = outputStream.toString().trim();
        assertEquals(42, result);
        assertEquals("", capturedOutput);  // No error message should be printed for valid input
    }

    @Test
    void testGetUserChoice_InvalidInputThenValidInput_ShouldReturnValidInput() {
        // Arrange
        String invalidInput = "invalid\n";
        String validInput = "42\n";
        System.setIn(new ByteArrayInputStream((invalidInput + validInput).getBytes()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        Scanner scannerMock = Mockito.mock(Scanner.class);

        // Mock necessary behavior for the test
        when(scannerMock.hasNextInt()).thenReturn(false, true);
        when(scannerMock.nextInt()).thenReturn(42);

        // Act
        int result = Main.getUserChoice(scannerMock);

        // Assert
        String capturedOutput = outputStream.toString().trim();
        assertEquals(42, result);
        assertTrue(capturedOutput.contains("Invalid input. Please enter a number."));
    }

    @Test
    void testRecommendClothingForCurrentLocation_ShouldPrintCurrentWeatherRecommendation() {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        RetrieveWeather weatherServiceMock = Mockito.mock(RetrieveWeather.class);
        RetrieveIP ipServiceMock = mock(RetrieveIP.class);

        // Mock necessary behavior for the test
        when(ipServiceMock.fetchIp()).thenReturn("123.456.7.8");
        when(weatherServiceMock.getCurrentWeather("123.456.7.8"))
                .thenReturn("It is warm so you should wear light clothing.");

        // Act
        Main.recommendClothingForCurrentLocation(weatherServiceMock, ipServiceMock);

        // Assert
        String capturedOutput = outputStream.toString().trim();
        assertEquals("Current Weather Recommendation:\nIt is warm so you should wear light clothing.", capturedOutput);
    }

    @Test
    void testHandleFutureWeatherRecommendation_ShouldPrintErrorMessageOnIOException() throws IOException {
        // Arrange
        ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOutputStream));

        Scanner scannerMock = Mockito.mock(Scanner.class);
        RetrieveWeather weatherServiceMock = Mockito.mock(RetrieveWeather.class);

        // Mock scanner input
        when(scannerMock.next()).thenReturn("JFK", "2023-11-30");

        // Mock necessary behavior for the test
        when(weatherServiceMock.getForecastRecommendation("JFK", "2023-11-30"))
                .thenThrow(new IOException("Simulated IO exception"));

        // Act
        Main.handleFutureWeatherRecommendation(scannerMock, weatherServiceMock);

        // Assert
        String errorOutput = errorOutputStream.toString().trim();
        assertEquals("Error getting future weather recommendation: Simulated IO exception", errorOutput);

        // Verify that the scanner was used as expected
        verify(scannerMock, times(2)).next();
    }


    @Test
    void testHandleFutureWeatherRecommendation_ShouldPrintFutureWeatherRecommendation() throws IOException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        Scanner scannerMock = Mockito.mock(Scanner.class);
        RetrieveWeather weatherServiceMock = Mockito.mock(RetrieveWeather.class);

        // Mock scanner input
        when(scannerMock.next()).thenReturn("JFK", "2023-11-30");

        // Mock necessary behavior for the test
        when(weatherServiceMock.getForecastRecommendation("JFK", "2023-11-30"))
                .thenReturn("It is warm so you should wear light clothing.");

        // Act
        Main.handleFutureWeatherRecommendation(scannerMock, weatherServiceMock);

        // Assert
        String capturedOutput = outputStream.toString().trim();
        assertEquals("Enter airport IATA code: Enter date of arrival (YYYY-MM-DD [max: 10 days from current day] --> \nFuture Weather Recommendation:\nIt is warm so you should wear light clothing.", capturedOutput);

        // Verify that the scanner was used as expected
        verify(scannerMock, times(2)).next();
    }









}

