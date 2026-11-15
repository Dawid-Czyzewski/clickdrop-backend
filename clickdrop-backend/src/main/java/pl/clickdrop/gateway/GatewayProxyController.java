package pl.clickdrop.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

@RestController
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayProxyController {

    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "host", "content-length"
    );

    private final GatewayRouter router;
    private final RestClient restClient = RestClient.create();

    public GatewayProxyController(GatewayRouter router) {
        this.router = router;
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String target = router.resolveUpstream(path) + path
                + (query == null || query.isBlank() ? "" : "?" + query);

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                headers.add(name, request.getHeader(name));
            }
        }
        headers.setAccept(MediaType.parseMediaTypes(MediaType.ALL_VALUE));

        try {
            return restClient.method(HttpMethod.valueOf(request.getMethod()))
                    .uri(target)
                    .headers(h -> h.addAll(headers))
                    .body(body)
                    .exchange((req, res) -> {
                        HttpHeaders out = new HttpHeaders();
                        res.getHeaders().forEach((k, values) -> {
                            if (!HOP_BY_HOP.contains(k.toLowerCase())) {
                                out.put(k, values);
                            }
                        });
                        byte[] responseBody = res.getBody() == null
                                ? new byte[0]
                                : StreamUtils.copyToByteArray(res.getBody());
                        return new ResponseEntity<>(responseBody, out, HttpStatusCode.valueOf(res.getStatusCode().value()));
                    });
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsByteArray());
        }
    }
}
