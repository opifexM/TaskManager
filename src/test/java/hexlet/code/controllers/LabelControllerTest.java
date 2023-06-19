package hexlet.code.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import hexlet.code.domain.label.LabelDto;
import hexlet.code.domain.label.LabelOperationDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
class LabelControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Faker faker = new Faker();

    private static final PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    static {
        postgres.start();
    }

    HttpEntity<String> requestWithJWTToken;
    private String apiUserLoginUrl;
    private String apiUserUrl;
    private String apiLabelUrl;
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
        apiLabelUrl = TestHelper.BASE_URL + port + TestHelper.API_LABELS;
    }

    @BeforeEach
    void registerAndLoginBeforeEachTest() {
        // registration + login
        requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);
    }

    @Test
    void shouldCreateLabelSuccessfully() {
        // create label
        String labelName = faker.color().name() + faker.number().digits(3);
        LabelDto newLabel = testHelper.createNewLabel(labelName, requestWithJWTToken, apiLabelUrl);

        assertThat(newLabel)
                .isNotNull()
                .satisfies(l -> {
                    assertThat(l.getId()).isPositive();
                    assertThat(l.getName()).isEqualTo(labelName);
                });
    }

    @Test
    void shouldReturnLabelDetailsWhenValidLabelIdProvided() {
        // create label
        LabelDto newLabel = testHelper.createNewLabel(
                faker.color().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiLabelUrl);

        // check label
        Long labelId = newLabel.getId();
        assertThat(labelId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiLabelUrl + "/{id}")
                .buildAndExpand(labelId)
                .toUriString();

        ResponseEntity<LabelDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LabelDto currentLabel = response.getBody();

        assertThat(currentLabel)
                .isNotNull()
                .satisfies(l -> {
                    assertThat(l.getId()).isEqualTo(labelId);
                    assertThat(l.getName()).isEqualTo(newLabel.getName());
                });
    }

    @Test
    void shouldReturnAllCreatedLabelsSuccessfully() {
        // create label
        List<LabelDto> labelForCreateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LabelDto newLabel = testHelper.createNewLabel(
                    faker.color().name() + faker.number().digits(3),
                    requestWithJWTToken,
                    apiLabelUrl);
            assertThat(newLabel).isNotNull();
            labelForCreateList.add(newLabel);
        }
        assertThat(labelForCreateList).isNotEmpty();

        // check label list
        ResponseEntity<List<LabelDto>> response = restTemplate.exchange(
                apiLabelUrl,
                HttpMethod.GET,
                requestWithJWTToken,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<LabelDto> returnedLabelList = response.getBody();
        assertThat(returnedLabelList)
                .isNotNull()
                .isNotEmpty();

        for (LabelDto label : returnedLabelList) {
            assertThat(returnedLabelList.stream().anyMatch(
                    labelFromCreateList -> label.getName().equals(labelFromCreateList.getName())
            )).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLabelOperationDto")
    void shouldReturnBadRequestWhenInvalidLabelDataProvided(LabelOperationDto labelForCreate)
            throws JsonProcessingException {
        // create label
        String userJson = OBJECT_MAPPER.writeValueAsString(labelForCreate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<LabelDto> response = restTemplate.exchange(apiLabelUrl, HttpMethod.POST,
                requestWithBodyAndToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Stream<LabelOperationDto> provideInvalidLabelOperationDto() {
        LabelOperationDto labelWithInvalidName = new LabelOperationDto("");
        LabelOperationDto labelWithInvalidNameLargeSize = new LabelOperationDto("T".repeat(100));
        return Stream.of(labelWithInvalidName, labelWithInvalidNameLargeSize);
    }

    @Test
    void shouldUpdateLabelDetailsSuccessfully() throws JsonProcessingException {
        // create label
        LabelDto newLabel = testHelper.createNewLabel(
                faker.color().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiLabelUrl);

        // check label
        Long labelId = newLabel.getId();
        assertThat(labelId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiLabelUrl + "/{id}")
                .buildAndExpand(labelId)
                .toUriString();

        ResponseEntity<LabelDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LabelDto currentLabel = response.getBody();

        assertThat(currentLabel)
                .isNotNull()
                .satisfies(l -> {
                    assertThat(l.getId()).isEqualTo(labelId);
                    assertThat(l.getName()).isEqualTo(newLabel.getName());
                });

        // update label
        LabelOperationDto labelForUpdate = new LabelOperationDto(faker.color().name());
        String userJson = OBJECT_MAPPER.writeValueAsString(labelForUpdate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());

        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        LabelDto updatedLabel = response.getBody();
        assertThat(updatedLabel)
                .isNotNull()
                .satisfies(l -> {
                    assertThat(l.getId()).isEqualTo(labelId);
                    assertThat(l.getName()).isEqualTo(labelForUpdate.getName());
                });
    }

    @Test
    void shouldDeleteLabelSuccessfullyAndReturnNotFoundAfterDeletion() {
        // create label
        LabelDto newLabel = testHelper.createNewLabel(
                faker.color().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiLabelUrl);

        // check label
        Long labelId = newLabel.getId();
        assertThat(labelId).isNotNull().isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiLabelUrl + "/{id}")
                .buildAndExpand(labelId)
                .toUriString();

        ResponseEntity<LabelDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LabelDto currentLabel = response.getBody();

        assertThat(currentLabel)
                .isNotNull()
                .satisfies(l -> {
                    assertThat(l.getId()).isEqualTo(labelId);
                    assertThat(l.getName()).isEqualTo(newLabel.getName());
                });

        // delete status
        response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldThrowDuplicateLabelExceptionWhenLabelNameAlreadyExists() throws JsonProcessingException {
        // create label
        String labelName = faker.color().name() + faker.number().digits(3);
        testHelper.createNewLabel(labelName, requestWithJWTToken, apiLabelUrl);

        // create another label with the same name
        LabelOperationDto duplicateLabel = new LabelOperationDto(labelName);
        String duplicateLabelJson = OBJECT_MAPPER.writeValueAsString(duplicateLabel);
        HttpEntity<String> requestWithBodyAndToken =
                new HttpEntity<>(duplicateLabelJson, requestWithJWTToken.getHeaders());

        // check DuplicateLabelException
        ResponseEntity<LabelDto> response = restTemplate.exchange(apiLabelUrl, HttpMethod.POST,
                requestWithBodyAndToken, LabelDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
