package testHexlet.src.test.java.hexlet.project.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String BEARER = "Bearer ";

    private static Path getFixturePath(String fileName) {
        return Paths.get("src", "test", "java", "testHexlet", "src", "test", "resources", "fixtures", fileName)
            .toAbsolutePath().normalize();
    }


    private static String readFixture(String fileName) throws Exception {
        Path filePath = getFixturePath(fileName);
        return Files.readString(filePath).trim();
    }

    public static Map getTestData() throws Exception {
        return mapper.readValue(readFixture("testData.json"), Map.class);
    }

    public static String login(MockMvc mockMvc, Map userData) throws Exception {
        Map<String, String> loginData = Map.of(
                "email", userData.get("email").toString(),
                "password", userData.get("password").toString()
        );
        var token = mockMvc.perform(
                post("/api/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginData))
                ).andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
        return BEARER + token;
    }

    public static int getUserIdByEmail(MockMvc mockMvc, String email) throws Exception {
        var response = mockMvc
            .perform(get("/api/users"))
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        List<Map> users = mapper.readValue(body, List.class);
        var existingUser = users.stream()
            .filter(user -> user.get("email").equals(email))
            .findAny()
            .get();

        return (int) existingUser.get("id");
    }

    public static int getStatusIdByName(MockMvc mockMvc, String name, String token) throws Exception {
        var response = mockMvc
            .perform(
                get("/api/statuses")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        List<Map> statuses = mapper.readValue(body, List.class);
        var existingStatus = statuses.stream()
            .filter(status -> status.get("name").equals(name))
            .findAny()
            .get();

        return (int) existingStatus.get("id");
    }

    public static int getLabelIdByName(MockMvc mockMvc, String name, String token) throws Exception {
        var response = mockMvc
            .perform(
                get("/api/labels")
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        List<Map> labels = mapper.readValue(body, List.class);
        var existingLabel = labels.stream()
            .filter(label -> label.get("name").equals(name))
            .findAny()
            .get();

        return (int) existingLabel.get("id");
    }

    private static List getEntities(String entitiesName) throws Exception {
        return mapper.readValue(readFixture(entitiesName + ".json"), List.class);
    }

    public static List getUsers() throws Exception {
        return getEntities("users");
    }

    public static List getStatuses() throws Exception {
        return getEntities("statuses");
    }

    public static List getLabels() throws Exception {
        return getEntities("labels");
    }

    private static void prepareEntities(MockMvc mockMvc, String entitiesName, String token) throws Exception {
        List<Map> entities = getEntities(entitiesName);

        for (var entity : entities) {
            mockMvc
                .perform(
                    post("/api/" + entitiesName)
                        .header(AUTHORIZATION, token)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entity))
                )
                .andReturn()
                .getResponse();
        }
    }

    private static void prepareEntities(MockMvc mockMvc, String entitiesName) throws Exception {
        prepareEntities(mockMvc, entitiesName, "");
    }

    public static void prepareUsers(MockMvc mockMvc) throws Exception {
        prepareEntities(mockMvc, "users");
    }

    public static void prepareStatuses(MockMvc mockMvc, String token) throws Exception {
        prepareEntities(mockMvc, "statuses", token);
    }

    public static void prepareLabels(MockMvc mockMvc, String token) throws Exception {
        prepareEntities(mockMvc, "labels", token);
    }

    public static Map prepareTaskData(MockMvc mockMvc, String token, Map<String, String> taskData) throws Exception {
        var statusId = TestUtils.getStatusIdByName(mockMvc, taskData.get("statusName"), token);
        var executorId = TestUtils.getUserIdByEmail(mockMvc, taskData.get("executorEmail"));
        var labelIds = new ArrayList<Integer>();
        if (taskData.get("label1Name") != null) {
            var label1Id = TestUtils.getLabelIdByName(mockMvc, taskData.get("label1Name"), token);
            labelIds.add(label1Id);
        }
        if (taskData.get("label2Name") != null) {
            var label2Id = TestUtils.getLabelIdByName(mockMvc, taskData.get("label2Name"), token);
            labelIds.add(label2Id);
        }
        Map<String, Object> task = (Map) taskData;
        task.put("taskStatusId", statusId);
        task.put("executorId", executorId);
        task.put("labelIds", labelIds);

        return task;
    }

    public static Map createTask(MockMvc mockMvc, String token, Map entity) throws Exception {
        var response = mockMvc
            .perform(
                post("/api/tasks")
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(entity))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        return mapper.readValue(body, Map.class);
    }
}
