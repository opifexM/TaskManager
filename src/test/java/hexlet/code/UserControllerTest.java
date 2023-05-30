package hexlet.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserDto;
import hexlet.code.domain.user.UserOperationDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Transactional
// @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String API_LOGIN = "/api/login";
    private static final String API_USERS = "/api/users";

    private String baseUrlPortApiUsers;

    @LocalServerPort
    private int port;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Faker faker = new Faker();

    @Autowired
    private TestRestTemplate restTemplate;

    private TestHelper testHelper;

    private static final PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    void setupTest() {
        baseUrlPortApiUsers = UserControllerTest.BASE_URL + port + UserControllerTest.API_USERS;
        testHelper = new TestHelper(restTemplate);
    }

    @Test
    void shouldRegisterSuccessfully() throws JsonProcessingException {
        // registration
        String password = faker.internet().password();
        UserOperationDto userForRegistration =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), password);
        String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        assertThat(registerUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UserDto registerUser = registerUserResponse.getBody();
        assertThat(registerUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isNotNull().isPositive();
                    assertThat(u.getFirstName()).isEqualTo(registerUser.getFirstName());
                    assertThat(u.getLastName()).isEqualTo(registerUser.getLastName());
                    assertThat(u.getEmail()).isEqualTo(registerUser.getEmail());
                });
    }

    @Test
    void shouldLoginUserSuccessfully() throws JsonProcessingException {
        // registration
        String password = faker.internet().password();
        UserOperationDto userForRegistration =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), password);
        String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        UserDto registerUser = registerUserResponse.getBody();

        // login
        UserOperationDto userForLogin =
                new UserOperationDto(null, null, registerUser.getEmail(), password);
        userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(userJson, BASE_URL + port + API_LOGIN);
        assertThat(requestWithJWTToken).isNotNull();
    }

    @Test
    void shouldReturnUserDetailsWhenValidUserIdProvided() throws JsonProcessingException {
        // registration
        String password = faker.internet().password();
        UserOperationDto userForRegistration =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), password);
        String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        UserDto registerUser = registerUserResponse.getBody();

        // login
        UserOperationDto userForLogin =
                new UserOperationDto(null, null, registerUser.getEmail(), password);
        userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(userJson, BASE_URL + port + API_LOGIN);

        // check current user
        Long userId = registerUser.getId();
        assertThat(userId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrlPortApiUsers + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDto currentUser = response.getBody();

        assertThat(currentUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u.getFirstName()).isEqualTo(registerUser.getFirstName());
                    assertThat(u.getLastName()).isEqualTo(registerUser.getLastName());
                    assertThat(u.getEmail()).isEqualTo(registerUser.getEmail());
                });
    }

    @Test
    void shouldReturnAllRegisteredUsersSuccessfully() throws JsonProcessingException {
        List<User> registrationUserList = new ArrayList<>();

        // registration
        for (int i = 0; i < 10; i++) {
            String password = faker.internet().password();
            String email = faker.internet().emailAddress();
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            UserOperationDto userForRegistration =
                    new UserOperationDto(firstName, lastName, email , password);
            String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
            ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
            assertThat(registerUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            registrationUserList.add(user);
        }
        assertThat(registrationUserList).isNotEmpty();

        // login
        UserOperationDto userForLogin =
                new UserOperationDto(null, null, registrationUserList.get(0).getEmail(), registrationUserList.get(0).getPassword());
        String userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(userJson, BASE_URL + port + API_LOGIN);
        assertThat(requestWithJWTToken).isNotNull();

        // check user list
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(baseUrlPortApiUsers, HttpMethod.GET,
                requestWithJWTToken, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<UserDto> returnedUserList = response.getBody();
        assertThat(returnedUserList).isNotNull().isNotEmpty();

        for (User user : registrationUserList) {
            assertThat(returnedUserList.stream().anyMatch(userDto ->
                    user.getEmail().equals(userDto.getEmail()) &&
                            user.getFirstName().equals(userDto.getFirstName()) &&
                            user.getLastName().equals(userDto.getLastName())
            )).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserOperationDto")
    void shouldReturnBadRequestWhenInvalidUserDataProvided(String userJson) {
        ResponseEntity<UserDto> response = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<String> provideInvalidUserOperationDto() {
        // Invalid email
        UserOperationDto userWithInvalidEmail =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        "invalid_email", faker.internet().password());

        // Missing first name
        UserOperationDto userWithMissingFirstName =
                new UserOperationDto(null, faker.name().lastName(),
                        faker.internet().emailAddress(), faker.internet().password());

        // Invalid password
        UserOperationDto userWithInvalidPassword =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), "12"); // password less than 3 characters

        // First name exceeds max length
        UserOperationDto userWithLongFirstName =
                new UserOperationDto(faker.lorem().fixedString(51), faker.name().lastName(),
                        faker.internet().emailAddress(), faker.internet().password());

        // Last name exceeds max length
        UserOperationDto userWithLongLastName =
                new UserOperationDto(faker.name().firstName(), faker.lorem().fixedString(51),
                        faker.internet().emailAddress(), faker.internet().password());

        // Email exceeds max length
        UserOperationDto userWithLongEmail =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.lorem().fixedString(101), faker.internet().password());

        // Password exceeds max length
        UserOperationDto userWithLongPassword =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), faker.lorem().fixedString(101));

        return Stream.of(userWithInvalidEmail, userWithMissingFirstName, userWithInvalidPassword,
                        userWithLongFirstName, userWithLongLastName, userWithLongEmail, userWithLongPassword)
                .map(value -> {
                    try {
                        return OBJECT_MAPPER.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void shouldUpdateUserDetailsSuccessfully() throws JsonProcessingException {
        // registration
        String password = faker.internet().password();
        UserOperationDto userForRegistration =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), password);
        String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        UserDto registerUser = registerUserResponse.getBody();

        // login
        UserOperationDto userForLogin =
                new UserOperationDto(null, null, registerUser.getEmail(), password);
        userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(userJson, BASE_URL + port + API_LOGIN);

        // check current user
        Long userId = registerUser.getId();
        assertThat(userId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrlPortApiUsers + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDto currentUser = response.getBody();
        assertThat(currentUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u.getFirstName()).isEqualTo(registerUser.getFirstName());
                    assertThat(u.getLastName()).isEqualTo(registerUser.getLastName());
                    assertThat(u.getEmail()).isEqualTo(registerUser.getEmail());
                });

        // update
        UserOperationDto userForUpdate = new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                faker.internet().emailAddress(), faker.internet().password());
        userJson = OBJECT_MAPPER.writeValueAsString(userForUpdate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());

        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDto updatedUser = response.getBody();
        assertThat(updatedUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u.getFirstName()).isEqualTo(userForUpdate.getFirstName());
                    assertThat(u.getLastName()).isEqualTo(userForUpdate.getLastName());
                    assertThat(u.getEmail()).isEqualTo(userForUpdate.getEmail());
                });
    }

    @Test
    void shouldDeleteUserSuccessfullyAndReturnNotFoundAfterDeletion() throws JsonProcessingException {
        // registration
        String password = faker.internet().password();
        UserOperationDto userForRegistration =
                new UserOperationDto(faker.name().firstName(), faker.name().lastName(),
                        faker.internet().emailAddress(), password);
        String userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        ResponseEntity<UserDto> registerUserResponse = testHelper.registerUser(userJson, baseUrlPortApiUsers);
        UserDto registerUser = registerUserResponse.getBody();
        Long userId = Objects.requireNonNull(registerUser).getId();

        // login
        UserOperationDto userForLogin =
                new UserOperationDto(null, null, registerUser.getEmail(), password);
        userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(userJson, BASE_URL + port + API_LOGIN);

        // check current user
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrlPortApiUsers + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDto currentUser = response.getBody();
        assertThat(currentUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u.getFirstName()).isEqualTo(registerUser.getFirstName());
                    assertThat(u.getLastName()).isEqualTo(registerUser.getLastName());
                    assertThat(u.getEmail()).isEqualTo(registerUser.getEmail());
                });

        // delete
        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}