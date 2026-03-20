package com.example.hotelback.exception;

import com.example.hotelback.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validationErrorsFollowStandardSchema() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(APPLICATION_JSON)
                        .header("X-Trace-Id", "trace-validation")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Хүсэлтийн өгөгдөл буруу байна"))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.traceId").value("trace-validation"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void internalServerErrorDoesNotExposeExceptionDetails() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Серверийн алдаа гарлаа"))
                .andExpect(jsonPath("$.path").value("/test/error"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.details").doesNotExist())
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        ResponseEntity<Void> validate(@Valid @RequestBody RegisterRequest request) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/error")
        ResponseEntity<Void> error() {
            throw new RuntimeException("sensitive details");
        }
    }
}
