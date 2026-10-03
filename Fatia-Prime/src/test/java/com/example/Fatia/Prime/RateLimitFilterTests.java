package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "app.rate-limit.enabled=true")
@AutoConfigureMockMvc
class RateLimitFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CepGeocoder cepGeocoder;

    @Test
    void appliesConfiguredLimitsAndReturnsRetryAfterAndFriendlyJson() throws Exception {
        when(cepGeocoder.geocodificarOpcoes(anyString())).thenReturn(List.of(
            new CepGeocoder.Coordenadas(-16.045839, -48.033231, "test", false)
        ));

        verifyLimit(20, "192.0.2.21", "freight", 200);
        verifyLimit(5, "192.0.2.22", "order", 400);
        verifyLimit(5, "192.0.2.23", "login", 401);
        verifyLimit(15, "192.0.2.24", "query", 404);
    }

    private void verifyLimit(int allowedRequests, String ip, String endpoint, int expectedStatus) throws Exception {
        for (int i = 0; i < allowedRequests; i++) {
            MvcResult result = mockMvc.perform(requestForLimit(ip, endpoint))
                .andReturn();
            assertEquals(expectedStatus, result.getResponse().getStatus());
        }

        MvcResult limited = mockMvc.perform(requestForLimit(ip, endpoint)).andReturn();
        assertEquals(429, limited.getResponse().getStatus());
        assertEquals("60", limited.getResponse().getHeader("Retry-After"));
        assertEquals("{\"message\":\"Muitas tentativas. Aguarde um minuto e tente novamente.\"}",
            limited.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestForLimit(
        String ip,
        String endpoint
    ) {
        var builder = switch (endpoint) {
            case "freight" -> get("/api/frete/consulta").param("cep", "72500100");
            case "order" -> post("/api/pedidos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}");
            case "login" -> post("/api/auth/login").with(csrf()).param("email", "invalido").param("senha", "invalido");
            default -> get("/api/pedidos/consulta")
                .param("codigo", "FP-INEXISTENTE")
                .param("telefone", "61999998888");
        };
        return builder.with(request -> {
            request.setRemoteAddr(ip);
            return request;
        });
    }
}
