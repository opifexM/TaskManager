package hexlet.code;

import hexlet.code.domain.user.UserDto;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

public class TestHelper {

    private final TestRestTemplate restTemplate;

    public TestHelper(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UserDto> registerUser(String userJson, String apiUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .toUriString();
        return restTemplate.exchange(url, HttpMethod.POST, request, UserDto.class);
    }

    public HttpEntity<String> loginUser(String userJson, String apiUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        headers.set("Authorization", "Bearer " + Objects.requireNonNull(response.getBody()).trim());
        return new HttpEntity<>("", headers);
    }
}
