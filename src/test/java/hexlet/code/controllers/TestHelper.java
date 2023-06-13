package hexlet.code.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import hexlet.code.domain.label.LabelDto;
import hexlet.code.domain.label.LabelOperationDto;
import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.status.StatusOperationDto;
import hexlet.code.domain.task.TaskDto;
import hexlet.code.domain.task.TaskOperationDto;
import hexlet.code.domain.user.UserDto;
import hexlet.code.domain.user.UserOperationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class TestHelper {
    static final String BASE_URL = "http://localhost:";
    static final String API_LOGIN = "/api/login";
    static final String API_USERS = "/api/users";
    static final String API_LABELS = "/api/labels";
    static final String API_STATUS = "/api/statuses";
    static final String API_TASKS = "/api/tasks";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Faker faker = new Faker();

    @Autowired
    private TestRestTemplate restTemplate;

    public UserDto registerUser(String firstName, String lastName,
                                String emailAddress, String password, String apiUserUrl) {

        UserOperationDto userForRegistration = new UserOperationDto(firstName, lastName, emailAddress, password);
        String userJson;
        try {
            userJson = OBJECT_MAPPER.writeValueAsString(userForRegistration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        assertThat(request).isNotNull();

        ResponseEntity<UserDto> response = restTemplate.exchange(apiUserUrl, HttpMethod.POST, request, UserDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    public HttpEntity<String> loginUser(String emailAddress, String password, String apiUserLoginUrl) {

        UserOperationDto userForLogin = new UserOperationDto(null, null, emailAddress, password);
        String userJson;
        try {
            userJson = OBJECT_MAPPER.writeValueAsString(userForLogin);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        assertThat(request).isNotNull();

        ResponseEntity<String> response = restTemplate.exchange(apiUserLoginUrl, HttpMethod.POST,
                request, String.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        headers.set("Authorization", "Bearer " + response.getBody().trim());
        HttpEntity<String> requestWithJWTToken = new HttpEntity<>("", headers);
        assertThat(requestWithJWTToken).isNotNull();
        return requestWithJWTToken;
    }

    public HttpEntity<String> registerAndLoginReturnJWTToken(String apiUserUrl, String apiUserLoginUrl) {
        // user registration
        String password = faker.internet().password();
        UserDto registerUser = registerUser(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                password,
                apiUserUrl);

        // user login
        HttpEntity<String> requestWithJWTToken = loginUser(registerUser.getEmail(), password, apiUserLoginUrl);
        assertThat(requestWithJWTToken).isNotNull();
        return requestWithJWTToken;
    }

    public LabelDto createNewLabel(String labelName, HttpEntity<String> requestWithJWTToken, String apiLabelsUrl) {
        // create label
        LabelOperationDto labelForCreate = new LabelOperationDto(labelName);
        String userJson;
        try {
            userJson = OBJECT_MAPPER.writeValueAsString(labelForCreate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<LabelDto> response = restTemplate.exchange(apiLabelsUrl, HttpMethod.POST,
                requestWithBodyAndToken, LabelDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    public StatusDto createNewStatus(String statusName, HttpEntity<String> requestWithJWTToken, String apiStatusUrl) {
        // create status
        StatusOperationDto statusForCreate = new StatusOperationDto(statusName);
        String userJson;
        try {
            userJson = OBJECT_MAPPER.writeValueAsString(statusForCreate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<StatusDto> response = restTemplate.exchange(apiStatusUrl, HttpMethod.POST,
                requestWithBodyAndToken, StatusDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    public TaskDto createNewTask(String taskName, String taskDescription, Long statusId, Long executorId,
                                 Set<Long> labelIdSet, HttpEntity<String> requestWithJWTToken, String apiTaskUrl) {
        // create task
        TaskOperationDto taskForCreate = new TaskOperationDto(taskName, taskDescription, statusId,
                null, executorId, labelIdSet);
        String userJson;
        try {
            userJson = OBJECT_MAPPER.writeValueAsString(taskForCreate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<String> requestWithBodyAndToken = new HttpEntity<>(userJson, requestWithJWTToken.getHeaders());
        ResponseEntity<TaskDto> response = restTemplate.exchange(apiTaskUrl, HttpMethod.POST,
                requestWithBodyAndToken, TaskDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    public Set<Long> createLabelIdSet(int number, HttpEntity<String> requestWithJWTToken, String apiLabelUrl) {
        Set<Long> labelIdSet = new HashSet<>();
        for (int i = 0; i < number; i++) {
            LabelDto newLabel = createNewLabel(
                    faker.color().name()+ faker.number().digits(3),
                    requestWithJWTToken,
                    apiLabelUrl);
            labelIdSet.add(newLabel.getId());
        }
        return labelIdSet;
    }
}
