package at.technikum.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public class OrsService {

    private static final Logger log = LoggerFactory.getLogger(OrsService.class);
    private static final String BASE_URL;
    private static final String API_KEY;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Static block for loading ORS credentials and base URL from config file
    static {
        Properties props = new Properties();
        try (InputStream is = OrsService.class.getResourceAsStream("/ors.properties")) {
            props.load(is);
        } catch (IOException e) {
            log.error("Failed to load ORS config", e);
            throw new ExceptionInInitializerError("Failed to load ORS config: " + e.getMessage());
        }
        BASE_URL = props.getProperty("ors.base.url");
        API_KEY = props.getProperty("ors.api.key");
        log.info("OrsService configured with BASE_URL={}", BASE_URL);
    }

    private final HttpClient httpClient;

    public OrsService() {
        this.httpClient = HttpClient.newHttpClient();
        log.info("OrsService initialized");
    }

    public Optional<GeocodeResult> geocode(String address) {
        log.info("geocode() called for address='{}'", address);
        try {
            String textParam = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String uri = String.format(
                    "%s/geocode/search?api_key=%s&text=%s",
                    BASE_URL, API_KEY, textParam
            );
            log.debug("Sending geocode request to {}", uri);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                String msg = "ORS geocode failed: HTTP " + resp.statusCode();
                log.error(msg + " - body: {}", resp.body());
                throw new RuntimeException(msg);
            }

            JsonNode root = MAPPER.readTree(resp.body());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                log.warn("No features returned for geocode('{}')", address);
                return Optional.empty();
            }

            // Parse coordinates and bounding box
            JsonNode first = features.get(0);
            JsonNode geom = first.path("geometry").path("coordinates");
            double lon = geom.get(0).asDouble();
            double lat = geom.get(1).asDouble();

            JsonNode bboxNode = first.path("bbox");
            double[] bbox = new double[]{
                    bboxNode.get(0).asDouble(),
                    bboxNode.get(1).asDouble(),
                    bboxNode.get(2).asDouble(),
                    bboxNode.get(3).asDouble()
            };

            GeocodeResult result = new GeocodeResult(lat, lon, bbox);
            log.info("geocode() success for '{}': {}", address, result);
            return Optional.of(result);

        } catch (IOException | InterruptedException e) {
            log.error("Error calling ORS geocode for '{}'", address, e);
            throw new RuntimeException("Error calling ORS geocode", e);
        }
    }

    public Optional<RouteResult> directions(
            String profile,
            double fromLon, double fromLat,
            double toLon, double toLat
    ) {
        log.info("directions() called: profile='{}' from=({},{}) to=({},{})",
                profile, fromLon, fromLat, toLon, toLat);
        try {
            // Prepare request body with coordinates
            ObjectNode body = MAPPER.createObjectNode();
            ArrayNode coords = body.putArray("coordinates");
            coords.addArray().add(fromLon).add(fromLat);
            coords.addArray().add(toLon).add(toLat);
            String jsonBody = MAPPER.writeValueAsString(body);

            String uri = String.format(
                    "%s/v2/directions/%s/geojson",
                    BASE_URL, profile
            );
            log.debug("Sending directions request to {}", uri);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json, application/geo+json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.error("ORS directions failed: HTTP {} body {}", resp.statusCode(), resp.body());
                throw new RuntimeException("ORS directions failed: HTTP " + resp.statusCode());
            }

            JsonNode root = MAPPER.readTree(resp.body());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                log.warn("No route features returned for directions()");
                return Optional.empty();
            }

            // Parse route properties and geometry
            JsonNode feat = features.get(0);
            JsonNode props = feat.path("properties").path("segments").get(0);
            double distance = props.path("distance").asDouble();
            double duration = props.path("duration").asDouble();
            JsonNode geometry = feat.path("geometry");

            // Parse bounding box if available
            JsonNode bboxNode = root.path("bbox");
            double[] bbox = null;
            if (bboxNode.isArray() && bboxNode.size() == 4) {
                bbox = new double[]{
                        bboxNode.get(0).asDouble(),
                        bboxNode.get(1).asDouble(),
                        bboxNode.get(2).asDouble(),
                        bboxNode.get(3).asDouble()
                };
            }

            RouteResult result = new RouteResult(distance, duration, geometry, bbox);
            log.info("directions() success: {}", result);
            return Optional.of(result);

        } catch (IOException | InterruptedException e) {
            log.error("Error calling ORS directions", e);
            throw new RuntimeException("Error calling ORS directions", e);
        }
    }
}