package hexlet.code.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    public static final String API_USERS = "/api/users";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    void createUsers() throws IOException {
        List<User> users = OBJECT_MAPPER.readValue(jsonRead("fixtures/users.json"), new TypeReference<>() {
        });
        for (User user : users) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String userJson = OBJECT_MAPPER.writeValueAsString(user);
            HttpEntity<String> request = new HttpEntity<>(userJson, headers);
            restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, Void.class);
        }
    }

    private String jsonRead(String jsonFilePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + jsonFilePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void listAllUsers() {
        ParameterizedTypeReference<List<UserDto>> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                "http://localhost:" + port + API_USERS,
                HttpMethod.GET,
                null,
                responseType);

        List<UserDto> users = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());

        UserDto user = users.get(0);
        assertEquals(1, user.getId());
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Petrov", user.getLastName());
        assertEquals("ivan@google.com", user.getEmail());

        user = users.get(1);
        assertEquals(2, user.getId());
        assertEquals("Petr", user.getFirstName());
        assertEquals("Sidorov", user.getLastName());
        assertEquals("petr@yahoo.com", user.getEmail());
    }


    @Test
    void getUserById() {
        long userId = 1L;
        ResponseEntity<UserDto> response = restTemplate.getForEntity("http://localhost:" + port + API_USERS + "/" + userId, UserDto.class);

        UserDto user = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(user);

        assertEquals(userId, user.getId());
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Petrov", user.getLastName());
        assertEquals("ivan@google.com", user.getEmail());
    }

    @Test
    void createUser() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/newUser.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<UserDto> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, UserDto.class);

        UserDto createdUser = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());

        assertEquals("John", createdUser.getFirstName());
        assertEquals("Doe", createdUser.getLastName());
        assertEquals("john.doe@example.com", createdUser.getEmail());
    }

    @Test
    void updateUser() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/newUserForUpdate.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<UserDto> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, UserDto.class);

        UserDto createdUser = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());

        assertEquals("Mike", createdUser.getFirstName());
        assertEquals("Whisky", createdUser.getLastName());
        assertEquals("mike.whisky@example.com", createdUser.getEmail());

        userJson = jsonRead("fixtures/updateUser.json");
        request = new HttpEntity<>(userJson, headers);
        response = restTemplate.exchange("http://localhost:" + port + API_USERS + createdUser.getId(), HttpMethod.PUT, request, UserDto.class);

    }


    @Test
    void deleteUser() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/newUserForDelete.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<UserDto> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, UserDto.class);

        UserDto createdUser = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());

        restTemplate.delete("http://localhost:" + port + API_USERS + "/" + createdUser.getId());
        ResponseEntity<List<UserDto>> allUsersResponse = restTemplate.exchange(
                "http://localhost:" + port + API_USERS,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        List<UserDto> allUsers = allUsersResponse.getBody();
        assertNotNull(allUsers);
        assertFalse(allUsers.stream().anyMatch(user -> user.getEmail().equals(createdUser.getEmail())));
    }

    @Test
    void createUserWithInvalidEmail() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/invalidNewUser1.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithInvalidName() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/invalidNewUser2.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithInvalidPassword() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = jsonRead("fixtures/invalidNewUser3.json");
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + API_USERS, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


}