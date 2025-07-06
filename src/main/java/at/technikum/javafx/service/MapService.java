package at.technikum.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MapService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public MapService() {
        log.info("MapService initialized");
    }

    public void writeDirectionsJs(RouteResult route, Path outputDir) throws IOException {
        log.info("writeDirectionsJs() called: outputDir={}", outputDir);

        Path outFile = outputDir.resolve("directions.js");

        try {
            // Build JSON structure for route data
            ObjectNode root = MAPPER.createObjectNode();
            ArrayNode bboxNode = root.putArray("bbox");
            for (double v : route.getBbox()) {
                bboxNode.add(v);
            }
            root.set("geometry", route.getGeometry());

            // Wrap JSON in a JavaScript variable declaration
            String js = "var directions = " + MAPPER.writeValueAsString(root) + ";";

            // Write to directions.js file
            Files.writeString(
                    outFile,
                    js,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            log.info("Successfully wrote directions.js to {}", outFile.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write directions.js to {}", outFile.toAbsolutePath(), e);
            throw e;
        }
    }
}