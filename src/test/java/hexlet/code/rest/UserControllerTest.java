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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return new HttpEntity<>("", headers);
    }

    @Test
    @Transactional
    void userRegisterAndLogin() {
        registerTestUser();
        loginTestUser();
    }

    @Test
    @Transactional
    void getUserById() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(user);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    @Transactional
    void getAllUsers() {
        registerUserList();

        ParameterizedTypeReference<List<UserDto>> responseType = new ParameterizedTypeReference<>() {
        };
        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS)
                .toUriString();

        ResponseEntity<List<UserDto>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
        List<UserDto> userDtoList = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(userDtoList);
        assertFalse(userDtoList.isEmpty());

        UserDto user = userDtoList.get(0);
        assertEquals(1, user.getId());
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Petrov", user.getLastName());
        assertEquals("ivan@google.com", user.getEmail());

        user = userDtoList.get(1);
        assertEquals(2, user.getId());
        assertEquals("Petr", user.getFirstName());
        assertEquals("Sidorov", user.getLastName());
        assertEquals("petr@yahoo.com", user.getEmail());
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
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @Transactional
    @ValueSource(strings = {
            "fixtures/invalidNewUser1.json",
            "fixtures/invalidNewUser2.json",
            "fixtures/invalidNewUser3.json"
    })
    void tryRegisterUsersWithInvalidData(String jsonFilePath) {
        String userJson = jsonRead(jsonFilePath);
        ResponseEntity<String> response = registerUser(userJson, "http://localhost:" + port + API_USERS);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Transactional
    void updateUser() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());

        String userForUpdate = jsonRead("fixtures/newUserForUpdate.json");
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userForUpdate, requestWithJWTToken.getHeaders());
        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, UserDto.class);
        UserDto updatedUser = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(user);
        assertEquals("Mike", updatedUser.getFirstName());
        assertEquals("Whisky", updatedUser.getLastName());
        assertEquals("mike.whisky@example.com", updatedUser.getEmail());
    }

    @Test
    @Transactional
    void deleteUser() {
        registerTestUser();
        HttpEntity<String> requestWithJWTToken = loginTestUser();

        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + API_USERS + "/{id}")
                .buildAndExpand(1L)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        UserDto user = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(user);

        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, UserDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}