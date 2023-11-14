package hu.agnos.report.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.agnos.cube.meta.resultDto.CubeList;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.Optional;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ruzsaz
 */
public class CubeService {

    public static CubeList getCubeList(String cubeServerUri) {
        CubeList cubeList = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(cubeServerUri + "/cube_list"))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());           
            cubeList = new ObjectMapper().readValue(response.body(), CubeList.class);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            LoggerFactory.getLogger(CubeService.class).error("Error at downloading cube list", ex);
        } finally {
            
        }
        return cubeList;
    }

}
