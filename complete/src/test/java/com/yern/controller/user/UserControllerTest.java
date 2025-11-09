package com.yern.controller.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yern.model.LocalDateTimeDeserializer;
import com.yern.model.user.User;
import com.yern.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: double check that auth is working here
@SpringBootTest(classes = {
    UserController.class
})
@AutoConfigureMockMvc
@EnableWebMvc
@DisplayName("User Controller Tests")
public class UserControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    @Value("${api.users-endpoint}")
    private String userEndpoint;

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
        .create();

    // Method: Get User
    @Test
    @UserDetailsImplMock(
        username = "testuser", userId = 1L, password = "password"
    )
    @DisplayName(
        "Get User: throws error when no matching 'id' is found"
    )
    public void getUser_shouldReturnNull_whenNoMatchingIdFound() throws Exception {
        User user = new User(
                "first",
                "last",
                "pconnelly@bloop.com"
        );
        user.setId(Long.valueOf("1"));

        when(userService.getUserById(user.getId())).thenReturn(null);

        MvcResult result = this.mockMvc.perform(
            get(userEndpoint + "/" + user.getId())
            .accept("application/json")
        )
        .andExpect(status().isOk())
        .andReturn();

        assertTrue(
            result.getResponse().getContentAsString().equals("null")
        );
    }

    @Test
    @UserDetailsImplMock(
        username = "testuser", userId = 1L, password = "password"
    )
    @DisplayName(
            "Get User: returns a User when a matching 'id' is found"
    )
    public void getUser_shouldReturnUser_whenMatchingIdFound() throws Exception {
        User user = new User(
                "first",
                "last",
                "pconnelly@bloop.com"
        );
        user.setId(Long.valueOf("1"));

        when(userService.getUserById(user.getId())).thenReturn(user);

        MvcResult result = this.mockMvc.perform(
            get(userEndpoint + "/" + user.getId())
                .accept("application/json")
                .param("id", String.valueOf(user.getId()))
        )
        .andExpect(status().isOk())
        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        User foundUser = gson.fromJson(responseBody, User.class);

        assertEquals(user.getFirstName(), foundUser.getFirstName());
        assertEquals(user.getLastName(), foundUser.getLastName());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
    }

    // Method: Get User by Email
    @Test
    @UserDetailsImplMock(
        username = "testuser", userId = 1L, password = "password"
    )
    @DisplayName(
        "Get User by Email: throws error when 'email' param is not provided."
    )
	public void getUserByEmail_shouldThrow_whenNoEmailParamProvided() throws Exception {

        this.mockMvc.perform(
            get(userEndpoint)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
        )
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertNotNull(result.getResolvedException()))
        .andExpect(result -> assertEquals(
                "Required request parameter 'email' for method parameter type String is not present",
                Objects.requireNonNull(result.getResolvedException()).getMessage())
        );
    }

    @Test
    @UserDetailsImplMock(
        username = "testuser", userId = 1L, password = "password"
    )
    @DisplayName(
        "Get User by Email: returns the expected user when a matching email is found."
    )
    public void getUserByEmail_shouldReturnUser_whenMatchingEmailFound() throws Exception {
        String email = "testuser";
        User user = new User(
            "first",
            "last",
            email
        );

        when(userService.getUserByEmail(email)).thenReturn(user);

        MvcResult result = this.mockMvc.perform(
            get(userEndpoint)
            .accept("application/json")
            .param("email", email)
        )
        .andExpect(status().isOk())
        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        User foundUser = gson.fromJson(responseBody, User.class);

        assertEquals(user.getFirstName(), foundUser.getFirstName());
        assertEquals(user.getLastName(), foundUser.getLastName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    @UserDetailsImplMock(
        username = "testuser", userId = 1L, password = "password"
    )
    @DisplayName(
        "Get User by Email: returns null when a matching user is found."
    )
    public void getUserByEmail_shouldReturnNull_whenNoMatchingUserFound() throws Exception {
        String email = "testuser";

        when(userService.getUserByEmail(email)).thenReturn(null);

        MvcResult result = this.mockMvc.perform(
            get(userEndpoint)
            .param("email", email)
        )
        .andExpect(status().isOk())
        .andReturn();

        assertTrue(
            result.getResponse().getContentAsString().equals("null")
        );
    }

    @Test
    @DisplayName(
        "Create User: creates an entry in the users table and returns the created user"
    )
    public void createUser_shouldCreateAndReturnAUser_whenValidUserProvided() {

    }
}
