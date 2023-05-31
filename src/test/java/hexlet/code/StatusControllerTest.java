package hexlet.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import hexlet.code.domain.label.LabelDto;
import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.status.StatusOperationDto;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatusControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Faker faker = new Faker();
    private static final PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    private String apiUserLoginUrl;
    private String apiUserUrl;
    private String apiStatusUrl;
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

    @BeforeAll
    void setupTest() {
        apiUserLoginUrl = TestHelper.BASE_URL + port + TestHelper.API_LOGIN;
        apiUserUrl = TestHelper.BASE_URL + port + TestHelper.API_USERS;
        apiStatusUrl = TestHelper.BASE_URL + port + TestHelper.API_LABELS;
    }

    @Test
    void shouldCreateStatusSuccessfully() {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        String statusName = faker.programmingLanguage().name() + faker.number().digits(3);
        StatusDto newStatus = testHelper.createNewStatus(statusName, requestWithJWTToken, apiStatusUrl);

        assertThat(newStatus).isNotNull().satisfies(s -> {
            assertThat(s.getId()).isPositive();
            assertThat(s.getName()).isEqualTo(statusName);
        });
    }

    @Test
    void shouldReturnStatusDetailsWhenValidLabelIdProvided() {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        LabelDto newStatus = testHelper.createNewLabel(
                faker.programmingLanguage().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiStatusUrl);

        // check status
        Long statusId = newStatus.getId();
        assertThat(statusId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiStatusUrl + "/{id}")
                .buildAndExpand(statusId)
                .toUriString();

        ResponseEntity<StatusDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StatusDto currentStatus = response.getBody();

        assertThat(currentStatus).isNotNull().satisfies(s -> {
            assertThat(s.getId()).isEqualTo(statusId);
            assertThat(s.getName()).isEqualTo(newStatus.getName());
        });
    }

    @Test
    void shouldReturnAllCreatedStatusSuccessfully() {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        List<StatusDto> statusForCreateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StatusDto newStatus = testHelper.createNewStatus(
                    faker.programmingLanguage().name() + faker.number().digits(3),
                    requestWithJWTToken,
                    apiStatusUrl);
            assertThat(newStatus).isNotNull();
            statusForCreateList.add(newStatus);
        }
        assertThat(statusForCreateList).isNotEmpty();

        // check status list
        ResponseEntity<List<StatusDto>> response = restTemplate.exchange(
                apiStatusUrl,
                HttpMethod.GET,
                requestWithJWTToken,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<StatusDto> returnedLabelList = response.getBody();
        assertThat(returnedLabelList).isNotNull().isNotEmpty();

        for (StatusDto status : returnedLabelList) {
            assertThat(returnedLabelList.stream().anyMatch(
                    labelFromCreateList -> status.getName().equals(labelFromCreateList.getName())
            )).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidStatusOperationDto")
    void shouldReturnBadRequestWhenInvalidStatusDataProvided(StatusOperationDto statusForCreate)
            throws JsonProcessingException {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        String userJson = OBJECT_MAPPER.writeValueAsString(statusForCreate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<StatusDto> response = restTemplate.exchange(apiStatusUrl, HttpMethod.POST,
                requestWithBodyAndToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Stream<StatusOperationDto> provideInvalidStatusOperationDto() {
        StatusOperationDto statusWithInvalidName = new StatusOperationDto("");
        StatusOperationDto statusWithInvalidNameLargeSize = new StatusOperationDto("T".repeat(100));
        return Stream.of(statusWithInvalidName, statusWithInvalidNameLargeSize);
    }

    @Test
    void shouldUpdateStatusDetailsSuccessfully() throws JsonProcessingException {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        LabelDto newStatus = testHelper.createNewLabel(
                faker.programmingLanguage().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiStatusUrl);

        // check status
        Long statusId = newStatus.getId();
        assertThat(statusId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiStatusUrl + "/{id}")
                .buildAndExpand(statusId)
                .toUriString();

        ResponseEntity<StatusDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StatusDto currentStatus = response.getBody();

        assertThat(currentStatus).isNotNull().satisfies(s -> {
            assertThat(s.getId()).isEqualTo(statusId);
            assertThat(s.getName()).isEqualTo(newStatus.getName());
        });

        // update status
        StatusOperationDto statusForUpdate = new StatusOperationDto(faker.color().name());
        String userJson = OBJECT_MAPPER.writeValueAsString(statusForUpdate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());

        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        StatusDto updatedStatus = response.getBody();
        assertThat(updatedStatus).isNotNull().satisfies(s -> {
            assertThat(s.getId()).isEqualTo(statusId);
            assertThat(s.getName()).isEqualTo(statusForUpdate.getName());
        });
    }

    @Test
    void shouldDeleteStatusSuccessfullyAndReturnNotFoundAfterDeletion() {
        // registration + login
        HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create status
        LabelDto newStatus = testHelper.createNewLabel(
                faker.programmingLanguage().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiStatusUrl);

        // check status
        Long statusId = newStatus.getId();
        assertThat(statusId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiStatusUrl + "/{id}")
                .buildAndExpand(statusId)
                .toUriString();

        ResponseEntity<StatusDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StatusDto currentStatus = response.getBody();

        assertThat(currentStatus).isNotNull().satisfies(s -> {
            assertThat(s.getId()).isEqualTo(statusId);
            assertThat(s.getName()).isEqualTo(newStatus.getName());
        });

        // delete status
        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, StatusDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}