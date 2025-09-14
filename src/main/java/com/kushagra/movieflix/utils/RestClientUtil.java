package com.kushagra.movieflix.utils;

import com.kushagra.movieflix.exception.RestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@Slf4j
public class RestClientUtil {

    public static String restClient(String url, Object request, HttpHeaders headers, String method) {
        log.info("Entered into restClient method in RestClientUtil");
        try {
            log.info(url);
            ResponseEntity<String> response = RestClient.builder()
                    .baseUrl(url)
                    .build()
                    .method(HttpMethod.valueOf(method))
//                    .headers(header -> header.addAll(headers))/
//                    .body(request)
//                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().toEntity(String.class);

            if (response.getStatusCode().isError()) {
                throw new RestException(response.getBody());
            }

            log.info("Response: {}",response);

            return response.getBody();
        } catch (Exception e) {
            log.info("Error while making rest call, {}", e.getMessage());
            throw new RestException(e.getMessage());
        }

    }
}
