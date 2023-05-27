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

@Order(2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = AppApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application.yml"
)
@AutoConfigureMockMvc
@Transactional
public class TaskStatusTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Map> testData;
    private String token;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws Exception {
        testData = TestUtils.getTestData();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> existingUserData = usersData.get("existing");
        token = TestUtils.login(mockMvc, existingUserData);
    }

    @Test
    @Order(1)
    void testCreateStatus() throws Exception {
        Map<String, Map> statusesData = testData.get("statuses");
        Map<String, String> newStatusData = statusesData.get("new");

        var response = mockMvc
            .perform(
                post("/api/statuses")
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newStatusData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(201);
        var createdStatus = mapper.readValue(body, Map.class);
        assertThat(createdStatus).contains(entry("name", newStatusData.get("name")));
        assertThat(createdStatus).containsKeys("id", "createdAt");
    }

    @Test
    @Order(2)
    void testListStatuses() throws Exception {
        TestUtils.prepareStatuses(mockMvc, token);
        List<Map> loadedStatuses = TestUtils.getStatuses();

        var response = mockMvc
            .perform(
                get("/api/statuses")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body, List.class));
        for (Map<String, String> status : loadedStatuses) {
            assertThat(body).contains(status.get("name"));
        }
    }

    @Test
    @Order(3)
    void testShowStatus() throws Exception {
        TestUtils.prepareStatuses(mockMvc, token);
        Map<String, Map> statusesData = testData.get("statuses");
        Map<String, String> existingStatusData = statusesData.get("existing");
        var existingStatusId = TestUtils.getStatusIdByName(mockMvc, existingStatusData.get("name"), token);

        var response = mockMvc
            .perform(
                get("/api/statuses/" + existingStatusId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var status = mapper.readValue(body, Map.class);
        assertThat(status).contains(entry("name", existingStatusData.get("name")));
        assertThat(status).containsKeys("id", "createdAt");
    }

    @Test
    @Order(4)
    void testUpdateStatus() throws Exception {
        TestUtils.prepareStatuses(mockMvc, token);
        Map<String, Map> statusesData = testData.get("statuses");
        Map<String, String> newStatusData = statusesData.get("new");
        Map<String, String> existingStatusData = statusesData.get("existing");
        var existingStatusId = TestUtils.getStatusIdByName(mockMvc, existingStatusData.get("name"), token);

        var response = mockMvc
            .perform(
                put("/api/statuses/" + existingStatusId)
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newStatusData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedStatus = mapper.readValue(body, Map.class);
        assertThat(updatedStatus).contains(entry("name", newStatusData.get("name")));
        assertThat(updatedStatus).containsKeys("id", "createdAt");

        response = mockMvc
            .perform(
                get("/api/statuses")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).contains(newStatusData.get("name"));
    }

    @Test
    @Order(5)
    void testDeleteStatus() throws Exception {
        TestUtils.prepareStatuses(mockMvc, token);
        Map<String, Map> statusesData = testData.get("statuses");
        Map<String, String> existingStatusData = statusesData.get("existing");
        var existingStatusId = TestUtils.getStatusIdByName(mockMvc, existingStatusData.get("name"), token);

        var response = mockMvc
            .perform(
                delete("/api/statuses/" + existingStatusId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);

        response = mockMvc
            .perform(
                get("/api/statuses")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).doesNotContain(existingStatusData.get("name"));
    }
}
