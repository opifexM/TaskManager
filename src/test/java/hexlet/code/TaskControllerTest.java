package hexlet.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import hexlet.code.domain.label.LabelDto;
import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.task.TaskDto;
import hexlet.code.domain.task.TaskOperationDto;
import hexlet.code.domain.user.UserDto;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskControllerTest {

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
    private String apiTaskUrl;
    private String apiStatusUrl;
    private String apiLabelUrl;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestHelper testHelper;
    private Long executorId;
    private Long statusId;
    private Set<Long> labelIdSet;

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
        apiStatusUrl = TestHelper.BASE_URL + port + TestHelper.API_STATUS;
        apiTaskUrl = TestHelper.BASE_URL + port + TestHelper.API_TASKS;
    }

    @BeforeEach
    void registerAndLoginBeforeEachTest() {
        // registration + login
        requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);

        // create executor
        UserDto newUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.internet().password(),
                apiUserUrl);
        executorId = newUser.getId();

        // create status
        StatusDto newStatus = testHelper.createNewStatus(
                faker.animal().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiStatusUrl);
        statusId = newStatus.getId();

        // create label set
        labelIdSet = testHelper.createLabelIdSet(3, requestWithJWTToken, apiLabelUrl);
    }

    @Test
    void shouldCreateTaskSuccessfully() {
        // create task
        String taskName = faker.pokemon().name() + faker.number().digits(3);
        String taskDescription = faker.backToTheFuture().quote() + faker.number().digits(3);
        TaskDto newTask = testHelper.createNewTask(
                taskName,
                taskDescription,
                statusId,
                executorId,
                labelIdSet,
                requestWithJWTToken,
                apiTaskUrl);

        assertThat(newTask)
                .isNotNull()
                .satisfies(t -> {
                    assertThat(t.getId()).isPositive();
                    assertThat(t.getId()).isEqualTo(newTask.getId());
                    assertThat(t.getName()).isEqualTo(taskName);
                    assertThat(t.getDescription()).isEqualTo(taskDescription);
                    assertThat(t.getExecutor().getId()).isEqualTo(executorId);
                    assertThat(t.getTaskStatus().getId()).isEqualTo(statusId);
                    assertThat(t.getLabels().stream().map(LabelDto::getId).collect(Collectors.toSet()))
                            .containsExactlyInAnyOrderElementsOf(labelIdSet);
                });
    }

    @Test
    void shouldReturnTaskDetailsWhenValidLabelIdProvided() {
        // create task
        TaskDto newTask = testHelper.createNewTask(
                faker.pokemon().name() + faker.number().digits(3),
                faker.backToTheFuture().quote() + faker.number().digits(3),
                statusId,
                executorId,
                labelIdSet,
                requestWithJWTToken,
                apiTaskUrl);

        // check task
        Long taskId = newTask.getId();
        assertThat(taskId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiTaskUrl + "/{id}")
                .buildAndExpand(taskId)
                .toUriString();

        ResponseEntity<TaskDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, TaskDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TaskDto currentTask = response.getBody();

        assertThat(currentTask)
                .isNotNull()
                .satisfies(t -> {
                    assertThat(t.getId()).isPositive();
                    assertThat(t.getId()).isEqualTo(newTask.getId());
                    assertThat(t.getName()).isEqualTo(newTask.getName());
                    assertThat(t.getDescription()).isEqualTo(newTask.getDescription());
                    assertThat(t.getExecutor().getId()).isEqualTo(executorId);
                    assertThat(t.getTaskStatus().getId()).isEqualTo(statusId);
                    assertThat(t.getLabels().stream().map(LabelDto::getId).collect(Collectors.toSet()))
                            .containsExactlyInAnyOrderElementsOf(labelIdSet);
                });
    }

    @Test
    void shouldReturnAllCreatedTaskSuccessfully() {
        // create task
        List<TaskDto> taskForCreateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TaskDto newTask = testHelper.createNewTask(
                    faker.pokemon().name() + faker.number().digits(3),
                    faker.backToTheFuture().quote() + faker.number().digits(3),
                    statusId,
                    executorId,
                    labelIdSet,
                    requestWithJWTToken,
                    apiTaskUrl);
            assertThat(newTask).isNotNull();
            taskForCreateList.add(newTask);
        }
        assertThat(taskForCreateList).isNotEmpty();

        // check task list
        ResponseEntity<List<TaskDto>> response = restTemplate.exchange(
                apiTaskUrl,
                HttpMethod.GET,
                requestWithJWTToken,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TaskDto> returnedTaskList = response.getBody();
        assertThat(returnedTaskList).isNotNull().isNotEmpty();

        for (TaskDto task : returnedTaskList) {
            assertThat(task.getId()).isPositive();
            assertThat(returnedTaskList.stream().anyMatch(labelFromCreateList ->
                    task.getId().equals(labelFromCreateList.getId())
                            && task.getName().equals(labelFromCreateList.getName())
                            && task.getDescription().equals(labelFromCreateList.getDescription())
                            && task.getExecutor().getId().equals(labelFromCreateList.getExecutor().getId())
                            && task.getTaskStatus().getId().equals(labelFromCreateList.getTaskStatus().getId())
            )).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTaskOperationDto")
    void shouldReturnBadRequestWhenInvalidStatusDataProvided(TaskOperationDto taskForCreate)
            throws JsonProcessingException {
        // create task
        String userJson = OBJECT_MAPPER.writeValueAsString(taskForCreate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<TaskDto> response = restTemplate.exchange(apiTaskUrl, HttpMethod.POST,
                requestWithBodyAndToken, TaskDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Stream<TaskOperationDto> provideInvalidTaskOperationDto() {
        TaskOperationDto taskWithBlankName = new TaskOperationDto("", "description", 1L, 1L, 1L, null);
        TaskOperationDto taskWithLongName = new TaskOperationDto("T".repeat(256), "description", 1L, 1L, 1L, null);
        TaskOperationDto taskWithNullStatus = new TaskOperationDto("name", "description", null, 1L, 1L, null);
        return Stream.of(taskWithBlankName, taskWithLongName, taskWithNullStatus);
    }

    @Test
    void shouldUpdateTaskDetailsSuccessfully() throws JsonProcessingException {
        // create task
        TaskDto newTask = testHelper.createNewTask(
                faker.pokemon().name() + faker.number().digits(3),
                faker.backToTheFuture().quote() + faker.number().digits(3),
                statusId,
                executorId,
                labelIdSet,
                requestWithJWTToken,
                apiTaskUrl);

        // check task
        Long taskId = newTask.getId();
        assertThat(taskId)
                .isNotNull()
                .isPositive();
        String url = UriComponentsBuilder
                .fromHttpUrl(apiTaskUrl + "/{id}")
                .buildAndExpand(taskId)
                .toUriString();

        ResponseEntity<TaskDto> response = restTemplate.exchange(url, HttpMethod.GET,
                requestWithJWTToken, TaskDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TaskDto currentTask = response.getBody();

        assertThat(currentTask)
                .isNotNull()
                .satisfies(t -> {
                    assertThat(t.getId()).isPositive();
                    assertThat(t.getId()).isEqualTo(newTask.getId());
                    assertThat(t.getName()).isEqualTo(newTask.getName());
                    assertThat(t.getDescription()).isEqualTo(newTask.getDescription());
                    assertThat(t.getExecutor().getId()).isEqualTo(executorId);
                    assertThat(t.getTaskStatus().getId()).isEqualTo(statusId);
                    assertThat(t.getLabels().stream().map(LabelDto::getId).collect(Collectors.toSet()))
                            .containsExactlyInAnyOrderElementsOf(labelIdSet);
                });

        // update task

        // create new executor
        UserDto newUser = testHelper.registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.internet().password(),
                apiUserUrl);
        Long newExecutorId = newUser.getId();

        // create new status
        StatusDto newStatus = testHelper.createNewStatus(
                faker.animal().name() + faker.number().digits(3),
                requestWithJWTToken,
                apiStatusUrl);
        Long newStatusId = newStatus.getId();

        // create new label set
        Set<Long> newLabelIdSet = testHelper.createLabelIdSet(5, requestWithJWTToken, apiLabelUrl);

        TaskOperationDto taskForUpdate = new TaskOperationDto(
                faker.pokemon().name() + faker.number().digits(3),
                faker.backToTheFuture().quote() + faker.number().digits(3),
                newStatusId,
                null,
                newExecutorId,
                newLabelIdSet);
        String userJson = OBJECT_MAPPER.writeValueAsString(taskForUpdate);
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());

        response = restTemplate.exchange(url, HttpMethod.PUT, requestWithBodyAndToken, TaskDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        TaskDto updatedStatus = response.getBody();
        assertThat(updatedStatus)
                .isNotNull()
                .satisfies(t -> {
                    assertThat(t.getId()).isPositive();
                    assertThat(t.getName()).isEqualTo(taskForUpdate.getName());
                    assertThat(t.getDescription()).isEqualTo(taskForUpdate.getDescription());
                    assertThat(t.getExecutor().getId()).isEqualTo(newExecutorId);
                    assertThat(t.getTaskStatus().getId()).isEqualTo(newStatusId);
                    assertThat(t.getLabels().stream().map(LabelDto::getId).collect(Collectors.toSet()))
                            .containsExactlyInAnyOrderElementsOf(newLabelIdSet);
        });
    }

    // @Test
    // void shouldDeleteStatusSuccessfullyAndReturnNotFoundAfterDeletion() {
    //     // registration + login
    //     HttpEntity<String> requestWithJWTToken = testHelper.registerAndLoginReturnJWTToken(apiUserUrl, apiUserLoginUrl);
    //
    //     // create status
    //     LabelDto newStatus = testHelper.createNewLabel(
    //             faker.programmingLanguage().name() + faker.number().digits(3),
    //             requestWithJWTToken,
    //             apiTaskUrl);
    //
    //     // check status
    //     Long statusId = newStatus.getId();
    //     assertThat(statusId).isNotNull().isPositive();
    //     String url = UriComponentsBuilder
    //             .fromHttpUrl(apiTaskUrl + "/{id}")
    //             .buildAndExpand(statusId)
    //             .toUriString();
    //
    //     ResponseEntity<StatusDto> response = restTemplate.exchange(url, HttpMethod.GET,
    //             requestWithJWTToken, StatusDto.class);
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     StatusDto currentStatus = response.getBody();
    //
    //     assertThat(currentStatus).isNotNull().satisfies(s -> {
    //         assertThat(s.getId()).isEqualTo(statusId);
    //         assertThat(s.getName()).isEqualTo(newStatus.getName());
    //     });
    //
    //     // delete status
    //     response = restTemplate.exchange(url, HttpMethod.DELETE, requestWithJWTToken, StatusDto.class);
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    //
    //     response = restTemplate.exchange(url, HttpMethod.GET, requestWithJWTToken, StatusDto.class);
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    // }
}