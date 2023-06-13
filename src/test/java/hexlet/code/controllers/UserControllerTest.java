package hexlet.code.controllers;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Faker faker = new Faker();
    private static final PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    private String apiUserUrl;
    private String apiUserLoginUrl;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestHelper testHelper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static Stream<UserOperationDto> provideInvalidUserOperationDto() {
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
                userWithLongFirstName, userWithLongLastName, userWithLongEmail, userWithLongPassword);
    }

    @BeforeAll
    void setupTest() {
        apiUserUrl = TestHelper.BASE_URL + port + TestHelper.API_USERS;
        apiUserLoginUrl = TestHelper.BASE_URL + port + TestHelper.API_LOGIN;
    }

    @Test
    void shouldRegisterSuccessfully() {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

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
    void shouldLoginUserSuccessfully() {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

        // user login
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(
                registerUser.getEmail(),
                password,
                apiUserLoginUrl);
        assertThat(requestWithJWTToken).isNotNull();
    }

    @Test
    void shouldReturnUserDetailsWhenValidUserIdProvided() {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

        // user login
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(
                registerUser.getEmail(),
                password,
                apiUserLoginUrl);

        // check current user
        Long userId = registerUser.getId();
        assertThat(userId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiUserUrl + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, UserDto.class);
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
    void shouldReturnAllRegisteredUsersSuccessfully() {
        List<User> registrationUserList = new ArrayList<>();

        // user registration
        for (int i = 0; i < 10; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String email = faker.internet().emailAddress();
            String password = faker.internet().password();
            testHelper.registerUser(firstName, lastName, email, password, apiUserUrl);

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            registrationUserList.add(user);
        }
        assertThat(registrationUserList).isNotEmpty();

        // user login
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(
                registrationUserList.get(0).getEmail(),
                registrationUserList.get(0).getPassword(),
                apiUserLoginUrl);

        // check user list
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(apiUserUrl, HttpMethod.GET,
                requestWithJWTToken, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<UserDto> returnedUserList = response.getBody();
        assertThat(returnedUserList)
                .isNotNull()
                .isNotEmpty();

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
    void shouldReturnBadRequestWhenInvalidUserDataProvided(UserOperationDto userForRegistration)
            throws JsonProcessingException {
        String userJson;

        userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        assertThat(request).isNotNull();

        ResponseEntity<UserDto> response = restTemplate.exchange(apiUserUrl, HttpMethod.POST, request, UserDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdateUserDetailsSuccessfully() throws JsonProcessingException {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

        // user login
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(
                registerUser.getEmail(),
                password,
                apiUserLoginUrl);

        // check current user
        Long userId = registerUser.getId();
        assertThat(userId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiUserUrl + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, UserDto.class);
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

        // update user
        UserOperationDto userForUpdate = new UserOperationDto(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.internet().password());

        String userJson = OBJECT_MAPPER.writeValueAsString(userForUpdate);
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
    void shouldDeleteUserSuccessfullyAndReturnNotFoundAfterDeletion() {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

        // user login
        HttpEntity<String> requestWithJWTToken = testHelper.loginUser(
                registerUser.getEmail(),
                password,
                apiUserLoginUrl);

        // check current user
        Long userId = registerUser.getId();
        assertThat(userId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiUserUrl + "/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, UserDto.class);
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

        // delete user
        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenUserEmailAlreadyExists() throws JsonProcessingException {
        // user registration
        String emailAddress = faker.internet().emailAddress();
        testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                emailAddress,
                faker.internet().password(),
                apiUserUrl
        );

        // create another user with the same name
        UserOperationDto duplicateUser = new UserOperationDto(
                faker.name().firstName(),
                faker.name().lastName(),
                emailAddress,
                faker.internet().password()
        );

        String userJson = OBJECT_MAPPER.writeValueAsString(duplicateUser);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        assertThat(request).isNotNull();

        // check DuplicateUserException
        ResponseEntity<UserDto> response = restTemplate.exchange(apiUserUrl, HttpMethod.POST, request, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
