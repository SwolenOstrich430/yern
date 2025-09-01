package com.yern.controller;

import com.yern.repository.UserRepository;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

	@Test
	public void noParamGreetingShouldReturnDefaultMessage() throws Exception {

        ResultActions result = this.mockMvc.perform(
            get("${api.users-endpoint}")
        );

        String a = "";
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(
//             jsonPath("$.content").
//        );
	}
}
