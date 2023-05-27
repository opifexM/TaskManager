package test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.data.MapEntry.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

import hexlet.code.AppApplication;
import hexlet.project.utils.TestUtils;

@Order(4)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = AppApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application.yml"
)
@AutoConfigureMockMvc
@Transactional
public class TaskTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Map> testData;
    private String token;
    private static Map<String, String> firstTaskData;
    private static Map<String, String> secondTaskData;
    private static Map<String, String> thirdTaskData;
    private Map<String, Object> firstTask;
    private Map<String, Object> secondTask;
    private Map<String, Object> thirdTask;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws Exception {
        testData = TestUtils.getTestData();
        Map<String, Map> tasksData = testData.get("tasks");
        firstTaskData = tasksData.get("first");
        secondTaskData = tasksData.get("second");
        thirdTaskData = tasksData.get("third");
    }

    @BeforeEach
    void beforeEach() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> existingUserData = usersData.get("existing");
        token = TestUtils.login(mockMvc, existingUserData);
        TestUtils.prepareStatuses(mockMvc, token);
        TestUtils.prepareLabels(mockMvc, token);

        // prepare tasks data
        firstTask = TestUtils.prepareTaskData(mockMvc, token, firstTaskData);
        secondTask = TestUtils.prepareTaskData(mockMvc, token, secondTaskData);
        thirdTask = TestUtils.prepareTaskData(mockMvc, token, thirdTaskData);
    }

    @Test
    @Order(1)
    void testCreateTask() throws Exception {
        var response = mockMvc
            .perform(
                post("/api/tasks")
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(firstTask))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(201);
        var createdTask = mapper.readValue(body, Map.class);
        assertThat(createdTask).contains(entry("name", firstTaskData.get("name")),
                                         entry("description", firstTaskData.get("description"))
        );
        assertThat(createdTask).containsKeys("id", "createdAt", "author", "executor", "taskStatus", "labels");
    }

    @Test
    @Order(2)
    void testShowTask() throws Exception {
        var existingTask = TestUtils.createTask(mockMvc, token, firstTask);
        var existingTaskId = existingTask.get("id");

        var response = mockMvc
            .perform(
                get("/api/tasks/" + existingTaskId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var task = mapper.readValue(body, Map.class);
        assertThat(task).contains(entry("name", firstTaskData.get("name")),
                                  entry("description", firstTaskData.get("description"))
        );
        assertThat(task).containsKeys("id", "createdAt", "author", "executor", "taskStatus", "labels");
    }

    @Test
    @Order(3)
    void testUpdateTask() throws Exception {
        var existingTask = TestUtils.createTask(mockMvc, token, firstTask);
        var existingTaskId = existingTask.get("id");

        var response = mockMvc
            .perform(
                put("/api/tasks/" + existingTaskId)
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(secondTask))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedTask = mapper.readValue(body, Map.class);
        assertThat(updatedTask).contains(entry("name", secondTaskData.get("name")),
                                         entry("description", secondTaskData.get("description"))
        );
        assertThat(updatedTask).containsKeys("id", "createdAt", "author", "executor", "taskStatus", "labels");

        response = mockMvc
            .perform(
                get("/api/tasks")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).contains(secondTaskData.get("name"));
    }

    @Test
    @Order(4)
    void testDeleteTask() throws Exception {
        var existingTask = TestUtils.createTask(mockMvc, token, firstTask);
        var existingTaskId = existingTask.get("id");

        var response = mockMvc
            .perform(
                delete("/api/tasks/" + existingTaskId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);

        response = mockMvc
            .perform(
                get("/api/tasks")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).doesNotContain(firstTaskData.get("name"));
    }

    @Test
    @Order(5)
    void testListTasks() throws Exception {
        TestUtils.createTask(mockMvc, token, firstTask);
        TestUtils.createTask(mockMvc, token, secondTask);
        TestUtils.createTask(mockMvc, token, thirdTask);

        var url = "/api/tasks";
        var response1 = mockMvc
            .perform(
                get(url)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body1 = response1.getContentAsString();

        assertThat(response1.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body1, List.class));
        assertThat(body1).contains(firstTaskData.get("name"));
        assertThat(body1).contains(secondTaskData.get("name"));
        assertThat(body1).contains(thirdTaskData.get("name"));

        var taskStatus = TestUtils.getStatusIdByName(mockMvc, secondTaskData.get("statusName"), token);
        url = "/api/tasks?taskStatus=" + taskStatus;

        var response2 = mockMvc
            .perform(
                get(url)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();

        var body2 = response2.getContentAsString();

        assertThat(response2.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body2, List.class));
        assertThat(body2).doesNotContain(firstTaskData.get("name"));
        assertThat(body2).contains(secondTaskData.get("name"));
        assertThat(body2).contains(thirdTaskData.get("name"));

        var label = TestUtils.getLabelIdByName(mockMvc, secondTaskData.get("label1Name"), token);
        url = "/api/tasks?taskStatus=" + taskStatus + "&labelsId=" + label;

        var response3 = mockMvc
            .perform(
                get(url)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();

        var body3 = response3.getContentAsString();

        assertThat(response3.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body3, List.class));
        assertThat(body3).doesNotContain(firstTaskData.get("name"));
        assertThat(body3).contains(secondTaskData.get("name"));
        assertThat(body3).doesNotContain(thirdTaskData.get("name"));
    }
}
