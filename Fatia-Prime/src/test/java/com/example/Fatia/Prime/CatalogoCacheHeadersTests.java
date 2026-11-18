package com.example.Fatia.Prime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogoCacheHeadersTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listasPublicasDoCatalogoRevalidamComEtagE304() throws Exception {
        for (String url : new String[] {"/api/produtos", "/api/categorias"}) {
            String etag = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                // ETag fraco para manter o gzip do Tomcat.
                .andExpect(header().string(HttpHeaders.ETAG, startsWith("W/\"")))
                .andExpect(header().doesNotExist(HttpHeaders.PRAGMA))
                .andExpect(header().doesNotExist(HttpHeaders.EXPIRES))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);

            mockMvc.perform(get(url).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, etag))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andExpect(content().string(""));

            mockMvc.perform(get(url).header(HttpHeaders.IF_NONE_MATCH, "\"etag-antigo\""))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, etag));
        }
    }

    @Test
    void demaisRotasDaApiContinuamSemCache() throws Exception {
        String[] urls = {
            "/api/produtos/1",
            "/api/categorias/1",
            "/api/opcoes-pizza?tipoProduto=SALGADA",
            "/api/frete/consulta",
            "/api/pedidos/consulta",
            "/api/admin/pedidos",
            "/api/auth/csrf",
        };
        for (String url : urls) {
            mockMvc.perform(get(url))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, not("no-cache")));
        }
    }
}
