package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ExternalCepGeocoderTests {

    @Test
    void coordenadaCorreiosDoCepInformadoEExataEArmazenadaEmCache() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500100"))
            .andRespond(withSuccess("""
                {"cep":"72500100","city":"Brasília","state":"DF","service":"correios",
                 "location":{"coordinates":{"longitude":"-47.99","latitude":"-16.02"}}}
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500100");
        geocoder.geocodificarOpcoes("72500100");

        assertEquals(-16.02, coordenadas.latitude(), 0.000001);
        assertEquals(-47.99, coordenadas.longitude(), 0.000001);
        assertEquals("BrasilAPI/Correios", coordenadas.fonte());
        assertFalse(coordenadas.aproximada());
        server.verify();
    }

    @Test
    void coordenadasOpenCepSaoIgnoradasE72500100UsaFallbackDeBairro() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500100"))
            .andRespond(withSuccess("""
                {"cep":"72500100","service":"open-cep",
                 "location":{"coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.test/ws/72500100/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-100","logradouro":"Quadra AC 200","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Quadra%20AC%20200", "[]");
        expectNominatim(server, "q=Santa%20Maria", """
            [{"lat":"-16.0272972","lon":"-47.9889102","address":{"city":"Santa Maria"}}]
            """);
        expectNominatim(server, "q=Bras%C3%ADlia", """
            [{"lat":"-16.01","lon":"-48.01","address":{"city":"Brasília"}}]
            """);
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500100");

        assertEquals(-16.0272972, coordenadas.latitude(), 0.000001);
        assertEquals(-47.9889102, coordenadas.longitude(), 0.000001);
        assertEquals("OpenStreetMap", coordenadas.fonte());
        assertTrue(coordenadas.aproximada());
        assertEquals(2, geocoder.geocodificarOpcoes("72500100").size());
        server.verify();
    }

    @Test
    void cep72500107SemResultadoPrecisoUsaReferenciaDeBairroELocalidade() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withSuccess("""
                {"cep":"72500107","service":"open-cep",
                 "location":{"coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-107","logradouro":"Quadra AC 200 Bloco G","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Quadra%20AC%20200%20Bloco%20G", "[]");
        expectNominatim(server, "q=Santa%20Maria", """
            [{"lat":"-16.0171229","lon":"-48.0131328","address":{"city":"Santa Maria"}}]
            """);
        expectNominatim(server, "q=Bras%C3%ADlia", """
            [{"lat":"-16.01","lon":"-48.01","address":{"city":"Brasília"}}]
            """);
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        var opcoes = geocoder.geocodificarOpcoes("72500107");

        assertEquals(2, opcoes.size());
        assertEquals(-16.0171229, opcoes.get(0).latitude(), 0.000001);
        assertEquals(-48.0131328, opcoes.get(0).longitude(), 0.000001);
        assertTrue(opcoes.get(0).aproximada());
        server.verify();
    }

    @Test
    void cepSemBairroPodeUsarReferenciaDaLocalidade() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500101"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://viacep.test/ws/72500101/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-101","logradouro":"","bairro":"","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Bras%C3%ADlia", "[]");
        expectNominatim(server, "q=Bras%C3%ADlia", """
            [{"lat":"-16.02","lon":"-48.02","address":{"city":"Brasília"}}]
            """);
        expectNominatim(server, "q=Bras%C3%ADlia", """
            [{"lat":"-16.03","lon":"-48.03","address":{"city":"Brasília"}}]
            """);
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500101");

        assertEquals(-16.03, coordenadas.latitude(), 0.000001);
        assertEquals(-48.03, coordenadas.longitude(), 0.000001);
        assertTrue(coordenadas.aproximada());
        server.verify();
    }

    @Test
    void falhaBrasilApiUsaViaCepENominatim() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500102"))
            .andRespond(withServerError());
        server.expect(requestTo("https://viacep.test/ws/72500102/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-102","logradouro":"Quadra AC 200 Bloco B","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Quadra%20AC%20200%20Bloco%20B", """
            [{"lat":"-16.02","lon":"-48.02","address":{"road":"Quadra AC 200 Bloco B","city":"Santa Maria"}}]
            """);
        expectNominatim(server, "q=Santa%20Maria", "[]");
        expectNominatim(server, "q=Bras%C3%ADlia", "[]");
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500102");

        assertTrue(coordenadas.aproximada());
        assertEquals(-16.02, coordenadas.latitude(), 0.000001);
        server.verify();
    }

    @Test
    void falhaBrasilApiEViaCepUsaBuscaNominatimPorCepValidado() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withServerError());
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withServerError());
        server.expect(requestTo(org.hamcrest.Matchers.containsString("postalcode=72500107")))
            .andExpect(header("User-Agent", "FatiaPrime-test/1.0"))
            .andRespond(withSuccess("""
                [{"lat":"-16.02","lon":"-48.02","address":{"postcode":"72500-107","country_code":"br"}}]
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500107");

        assertEquals("OpenStreetMap/CEP", coordenadas.fonte());
        assertTrue(coordenadas.aproximada());
        server.verify();
    }

    @Test
    void cepValidoSemCorrespondenciaDeCepNoNominatimRetornaFalhaTecnica() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withServerError());
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withServerError());
        server.expect(requestTo(org.hamcrest.Matchers.containsString("postalcode=72500107")))
            .andRespond(withSuccess("""
                [{"lat":"-16.02","lon":"-48.02","address":{"postcode":"72500-100","country_code":"br"}}]
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        assertThrows(CepGeocoder.GeocodificacaoIndisponivelException.class,
            () -> geocoder.geocodificar("72500107"));
        server.verify();
    }

    @Test
    void cepInexistenteEIdentificadoPeloViaCep() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/00000000"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://viacep.test/ws/00000000/json/"))
            .andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        assertThrows(CepGeocoder.CepNaoEncontradoException.class, () -> geocoder.geocodificar("00000000"));
        server.verify();
    }

    private void expectNominatim(MockRestServiceServer server, String query, String response) {
        server.expect(requestTo(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("https://nominatim.test/search"),
                org.hamcrest.Matchers.containsString(query))))
            .andExpect(header("User-Agent", "FatiaPrime-test/1.0"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    private FreteProperties properties() {
        FreteProperties properties = new FreteProperties();
        properties.setBrasilApiUrl("https://brasilapi.test");
        properties.setViaCepUrl("https://viacep.test/ws");
        properties.setNominatimUrl("https://nominatim.test/search");
        properties.setNominatimUserAgent("FatiaPrime-test/1.0");
        return properties;
    }
}
