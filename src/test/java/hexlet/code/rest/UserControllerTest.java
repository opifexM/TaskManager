package hexlet.code.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    public static final String API_USERS = "/api/users";
    public static final String API_LOGIN = "/api/login";
    private static final String BASE_URL = "http://localhost:";

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterAll
    static void cleanUp() {
        postgres.stop();
    }

    private String jsonRead(String jsonFilePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + jsonFilePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<String> registerUser(String userJson, String apiUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        String url = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .toUriString();
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private void registerTestUser() {
        String userJson = jsonRead("fixtures/newUser.json");
        ResponseEntity<String> response = registerUser(userJson, BASE_URL + port + API_USERS);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpEntity<String> loginTestUser() {
        String userJson = jsonRead("fixtures/login.json");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_LOGIN)
                .toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        headers.set("Authorization", "Bearer " + Objects.requireNonNull(response.getBody()).trim());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return new HttpEntity<>("", headers);
    }

    @Test
    @Transactional
    void shouldRegisterAndLoginUserSuccessfully() {
        registerTestUser();
        loginTestUser();
    }

    @Test
    @Transactional
    void shouldReturnUserDetailsWhenValidUserIdProvided() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user).isNotNull();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @Transactional
    void shouldReturnAllRegisteredUsersSuccessfully() {
        registerUserList();

        ParameterizedTypeReference<List<UserDto>> responseType = new ParameterizedTypeReference<>() {
        };
        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS)
                .toUriString();

        ResponseEntity<List<UserDto>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
        List<UserDto> userDtoList = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userDtoList).isNotNull();
        assertThat(userDtoList).isNotEmpty();

        UserDto user = userDtoList.get(0);
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getFirstName()).isEqualTo("Ivan");
        assertThat(user.getLastName()).isEqualTo("Petrov");
        assertThat(user.getEmail()).isEqualTo("ivan@google.com");

        user = userDtoList.get(1);
        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getFirstName()).isEqualTo("Petr");
        assertThat(user.getLastName()).isEqualTo("Sidorov");
        assertThat(user.getEmail()).isEqualTo("petr@yahoo.com");
    }

    @SneakyThrows
    private void registerUserList() {
        List<User> userListForRegistration = OBJECT_MAPPER.readValue(
                jsonRead("fixtures/usersForRegistration.json"),
                new TypeReference<>() {
                });

        for (User user : userListForRegistration) {
            String userJson = OBJECT_MAPPER.writeValueAsString(user);
            ResponseEntity<String> response = registerUser(userJson, BASE_URL + port + API_USERS);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @ParameterizedTest
    @Transactional
    @ValueSource(strings = {
            "fixtures/invalidNewUser1.json",
            "fixtures/invalidNewUser2.json",
            "fixtures/invalidNewUser3.json"
    })
    void shouldReturnBadRequestWhenInvalidUserDataProvided(String jsonFilePath) {
        String userJson = jsonRead(jsonFilePath);
        ResponseEntity<String> response = registerUser(userJson, "http://localhost:" + port + API_USERS);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Transactional
    void shouldUpdateUserDetailsSuccessfully() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");

        String userForUpdate = jsonRead("fixtures/newUserForUpdate.json");
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userForUpdate, requestWithJWTToken.getHeaders());
        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, UserDto.class);
        UserDto updatedUser = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user).isNotNull();
        assertThat(updatedUser.getFirstName()).isEqualTo("Mike");
        assertThat(updatedUser.getLastName()).isEqualTo("Whisky");
        assertThat(updatedUser.getEmail()).isEqualTo("mike.whisky@example.com");
    }

    @Test
    @Transactional
    void shouldDeleteUserSuccessfullyAndReturnNotFoundAfterDeletion() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user).isNotNull();

        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}