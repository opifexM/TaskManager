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

@Order(3)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = AppApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application.yml"
)
@AutoConfigureMockMvc
@Transactional
public class LabelTest {

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
    void testCreateLabel() throws Exception {
        Map<String, Map> labelsData = testData.get("labels");
        Map<String, String> newLabelData = labelsData.get("new");

        var response = mockMvc
            .perform(
                post("/api/labels")
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newLabelData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(201);
        var createdLabel = mapper.readValue(body, Map.class);
        assertThat(createdLabel).contains(entry("name", newLabelData.get("name")));
        assertThat(createdLabel).containsKeys("id", "createdAt");
    }

    @Test
    @Order(2)
    void testListLabels() throws Exception {
        TestUtils.prepareLabels(mockMvc, token);
        List<Map> loadedLabels = TestUtils.getLabels();

        var response = mockMvc
            .perform(
                get("/api/labels")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body, List.class));
        for (Map<String, String> label : loadedLabels) {
            assertThat(body).contains(label.get("name"));
        }
    }

    @Test
    @Order(3)
    void testShowLabel() throws Exception {
        TestUtils.prepareLabels(mockMvc, token);
        Map<String, Map> labelsData = testData.get("labels");
        Map<String, String> existingLabelData = labelsData.get("existing");
        var existingLabelId = TestUtils.getLabelIdByName(mockMvc, existingLabelData.get("name"), token);

        var response = mockMvc
            .perform(
                get("/api/labels/" + existingLabelId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var label = mapper.readValue(body, Map.class);
        assertThat(label).contains(entry("name", existingLabelData.get("name")));
        assertThat(label).containsKeys("id", "createdAt");
    }

    @Test
    @Order(4)
    void testUpdateLabel() throws Exception {
        TestUtils.prepareLabels(mockMvc, token);
        Map<String, Map> labelsData = testData.get("labels");
        Map<String, String> newLabelData = labelsData.get("new");
        Map<String, String> existingLabelData = labelsData.get("existing");
        var existingLabelId = TestUtils.getLabelIdByName(mockMvc, existingLabelData.get("name"), token);

        var response = mockMvc
            .perform(
                put("/api/labels/" + existingLabelId)
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newLabelData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedLabel = mapper.readValue(body, Map.class);
        assertThat(updatedLabel).contains(entry("name", newLabelData.get("name")));
        assertThat(updatedLabel).containsKeys("id", "createdAt");

        response = mockMvc
            .perform(
                get("/api/labels")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).contains(newLabelData.get("name"));
    }

    @Test
    @Order(5)
    void testDeleteLabel() throws Exception {
        TestUtils.prepareLabels(mockMvc, token);
        Map<String, Map> labelsData = testData.get("labels");
        Map<String, String> existingLabelData = labelsData.get("existing");
        var existingLabelId = TestUtils.getLabelIdByName(mockMvc, existingLabelData.get("name"), token);

        var response = mockMvc
            .perform(
                delete("/api/labels/" + existingLabelId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);

        response = mockMvc
            .perform(
                get("/api/labels")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).doesNotContain(existingLabelData.get("name"));
    }
}
