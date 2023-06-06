package testHexlet.src.test.java.hexlet.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.AppApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import testHexlet.src.test.java.hexlet.project.utils.TestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = AppApplication.class)
//@TestPropertySource(
//        locations = "classpath:application.yml"
//)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class UserTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Map> testData;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws Exception {
        testData = TestUtils.getTestData();
    }

    @Test
    @Order(1)
    void testCreateUser() throws Exception {
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> newUserData = usersData.get("new");

        var response = mockMvc
            .perform(
                post("/api/users")
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newUserData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(201);
        var createdUser = mapper.readValue(body, Map.class);
        assertThat(createdUser).contains(entry("firstName", newUserData.get("firstName")),
                                         entry("lastName", newUserData.get("lastName")),
                                         entry("email", newUserData.get("email")));
        assertThat(createdUser).containsKeys("id", "createdAt");
        assertThat(createdUser).doesNotContainKeys("password");
    }

    @Test
    @Order(2)
    void testListUsers() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        List<Map> loadedUsers = TestUtils.getUsers();

        var response = mockMvc
            .perform(get("/api/users"))
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertDoesNotThrow(() -> mapper.readValue(body, List.class));
        for (Map<String, String> user : loadedUsers) {
            assertThat(body).contains(user.get("email"));
        }
    }

    @Test
    @Order(3)
    void testSignIn() throws Exception {
        TestUtils.prepareUsers(mockMvc);

        Map<String, Map> usersData = testData.get("users");
        Map<String, String> existingUserData = usersData.get("existing");

        Map<String, String> loginData = Map.of(
                "email", existingUserData.get("email"),
                "password", existingUserData.get("password")
        );

        String content = mapper.writeValueAsString(loginData);
        var response = mockMvc
            .perform(
                post("/api/login")
                    .contentType(APPLICATION_JSON)
                    .content(content)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).isNotEqualTo("");
    }

    @Test
    @Order(4)
    void testShowUser() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> existingUserData = usersData.get("existing");
        var token = TestUtils.login(mockMvc, existingUserData);
        var existingUserId = TestUtils.getUserIdByEmail(mockMvc, existingUserData.get("email"));

        var response = mockMvc
            .perform(
                get("/api/users/" + existingUserId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var user = mapper.readValue(body, Map.class);
        assertThat(user).contains(entry("firstName", existingUserData.get("firstName")),
                                  entry("lastName", existingUserData.get("lastName")),
                                  entry("email", existingUserData.get("email")));
        assertThat(user).containsKeys("id", "createdAt");
        assertThat(user).doesNotContainKeys("password");
    }

    @Test
    @Order(5)
    void testUpdateUser() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> newUserData = usersData.get("new");
        Map<String, String> existingUserData = usersData.get("existing");
        var token = TestUtils.login(mockMvc, existingUserData);
        var existingUserId = TestUtils.getUserIdByEmail(mockMvc, existingUserData.get("email"));

        var response = mockMvc
            .perform(
                put("/api/users/" + existingUserId)
                    .header(AUTHORIZATION, token)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newUserData))
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedUser = mapper.readValue(body, Map.class);
        assertThat(updatedUser).contains(entry("firstName", newUserData.get("firstName")),
                                         entry("lastName", newUserData.get("lastName")),
                                         entry("email", newUserData.get("email")));
        assertThat(updatedUser).containsKeys("id", "createdAt");
        assertThat(updatedUser).doesNotContainKeys("password");

        response = mockMvc
            .perform(get("/api/users"))
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).contains(newUserData.get("email"));
    }

    @Test
    @Order(6)
    void testDeleteUser() throws Exception {
        TestUtils.prepareUsers(mockMvc);
        Map<String, Map> usersData = testData.get("users");
        Map<String, String> existingUserData = usersData.get("existing");
        var token = TestUtils.login(mockMvc, existingUserData);
        var existingUserId = TestUtils.getUserIdByEmail(mockMvc, existingUserData.get("email"));

        var response = mockMvc
            .perform(
                delete("/api/users/" + existingUserId)
                    .header(AUTHORIZATION, token)
            )
            .andReturn()
            .getResponse();
        var body = response.getContentAsString();

        assertThat(response.getStatus()).isEqualTo(200);

        response = mockMvc
            .perform(get("/api/users"))
            .andReturn()
            .getResponse();
        body = response.getContentAsString();

        assertThat(body).doesNotContain(existingUserData.get("email"));
    }
}
