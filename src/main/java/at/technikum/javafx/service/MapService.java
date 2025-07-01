package at.technikum.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Responsible for generating the Leaflet JS file from a RouteResult.
 */
public class MapService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Writes out a directions.js file containing a `directions` object with bbox and geometry.
     *
     * @param route     the result from ORS containing geometry and bbox
     * @param outputDir directory where leaflet.html and directions.js live
     * @throws IOException if writing the file fails
     */
    public void writeDirectionsJs(RouteResult route, Path outputDir) throws IOException {
        // Build a JSON object: { "bbox": [...], "geometry": { ... } }
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode bboxNode = root.putArray("bbox");
        for (double v : route.getBbox()) {
            bboxNode.add(v);
        }
        root.set("geometry", route.getGeometry());

        // Serialize and wrap in a JS var declaration
        String js = "var directions = " + MAPPER.writeValueAsString(root) + ";";

        // Write (or overwrite) directions.js in the target folder
        Files.writeString(
                outputDir.resolve("directions.js"),
                js,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}