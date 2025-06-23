package at.technikum.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

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
    private static final String BASE_URL;
    private static final String API_KEY;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        Properties props = new Properties();
        try (InputStream is = OrsService.class.getResourceAsStream("/ors.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load ORS config: " + e.getMessage());
        }
        BASE_URL = props.getProperty("ors.base.url");
        API_KEY = props.getProperty("ors.api.key");
    }

    private final HttpClient httpClient;

    public OrsService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public Optional<GeocodeResult> geocode(String address) {
        try {
            String textParam = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String uri = String.format(
                    "%s/geocode/search?api_key=%s&text=%s",
                    BASE_URL, API_KEY, textParam
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("ORS geocode failed: HTTP " + resp.statusCode());
            }

            JsonNode root     = MAPPER.readTree(resp.body());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                return Optional.empty();
            }

            JsonNode first = features.get(0);
            JsonNode geom  = first.path("geometry").path("coordinates");
            double lon = geom.get(0).asDouble();
            double lat = geom.get(1).asDouble();

            JsonNode bboxNode = first.path("bbox");
            double minLon = bboxNode.get(0).asDouble();
            double minLat = bboxNode.get(1).asDouble();
            double maxLon = bboxNode.get(2).asDouble();
            double maxLat = bboxNode.get(3).asDouble();

            double[] bbox = new double[]{minLon, minLat, maxLon, maxLat};
            return Optional.of(new GeocodeResult(lat, lon, bbox));

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error calling ORS geocode", e);
        }
    }

    /**
     * Compute a route between two points.
     *
     * @param profile  transport profile, e.g. "driving-car" or "foot-walking"
     * @param fromLon  longitude of the start
     * @param fromLat  latitude of the start
     * @param toLon    longitude of the end
     * @param toLat    latitude of the end
     * @return a RouteResult with distance (m), duration (s), geometry and bbox
     */
    public Optional<RouteResult> directions(
            String profile,
            double fromLon, double fromLat,
            double toLon,   double toLat
    ) {
        try {
            // build JSON body: { "coordinates": [ [fromLon,fromLat], [toLon,toLat] ] }
            ObjectNode body = MAPPER.createObjectNode();
            ArrayNode coords = body.putArray("coordinates");
            coords.addArray().add(fromLon).add(fromLat);
            coords.addArray().add(toLon).add(toLat);
            String jsonBody = MAPPER.writeValueAsString(body);

            String uri = String.format(
                    "%s/v2/directions/%s/geojson?api_key=%s",
                    BASE_URL, profile, API_KEY
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            var resp = httpClient.send(req, BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("ORS directions failed: HTTP " + resp.statusCode());
            }

            JsonNode root     = MAPPER.readTree(resp.body());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                return Optional.empty();
            }

            JsonNode feat = features.get(0);
            JsonNode props = feat.path("properties").path("segments").get(0);
            double distance = props.path("distance").asDouble();
            double duration = props.path("duration").asDouble();
            JsonNode geometry = feat.path("geometry");

            // bbox is often on the root of the FeatureCollection
            JsonNode bboxNode  = root.path("bbox");
            double[] bbox = null;
            if (bboxNode.isArray() && bboxNode.size() == 4) {
                bbox = new double[] {
                        bboxNode.get(0).asDouble(),
                        bboxNode.get(1).asDouble(),
                        bboxNode.get(2).asDouble(),
                        bboxNode.get(3).asDouble()
                };
            }

            return Optional.of(new RouteResult(distance, duration, geometry, bbox));

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error calling ORS directions", e);
        }
    }
}