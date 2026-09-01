package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.clickdrop.auth.api.dto.UserResponse;
import pl.clickdrop.auth.application.UserService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Test
    void me_returns401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "player@clickdrop.pl")
    void me_returnsProfile_whenAuthenticated() throws Exception {
        when(userService.getCurrentUser("player@clickdrop.pl"))
                .thenReturn(new UserResponse(
                        "player",
                        "player@clickdrop.pl",
                        5,
                        1200,
                        new BigDecimal("99.50")
                ));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("player"))
                .andExpect(jsonPath("$.email").value("player@clickdrop.pl"))
                .andExpect(jsonPath("$.level").value(5))
                .andExpect(jsonPath("$.experience").value(1200))
                .andExpect(jsonPath("$.clickDollars").value(99.50));
    }
}