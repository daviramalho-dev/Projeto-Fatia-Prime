package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

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
        server.expect(ExpectedCount.times(2), requestTo("https://brasilapi.test/72500102"))
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
        server.expect(ExpectedCount.times(2), requestTo("https://brasilapi.test/72500107"))
            .andRespond(withServerError());
        server.expect(ExpectedCount.times(2), requestTo("https://viacep.test/ws/72500107/json/"))
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
        server.expect(ExpectedCount.times(2), requestTo("https://brasilapi.test/72500107"))
            .andRespond(withServerError());
        server.expect(ExpectedCount.times(2), requestTo("https://viacep.test/ws/72500107/json/"))
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
    void falhasDeGeocodificacaoNaoSaoArmazenadasEmCache() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        for (int tentativa = 0; tentativa < 2; tentativa++) {
            server.expect(ExpectedCount.times(2), requestTo("https://brasilapi.test/72500108"))
                .andRespond(withServerError());
            server.expect(ExpectedCount.times(2), requestTo("https://viacep.test/ws/72500108/json/"))
                .andRespond(withServerError());
            server.expect(requestTo(org.hamcrest.Matchers.containsString("postalcode=72500108")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        }
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        assertThrows(CepGeocoder.GeocodificacaoIndisponivelException.class,
            () -> geocoder.geocodificarOpcoes("72500108"));
        assertThrows(CepGeocoder.GeocodificacaoIndisponivelException.class,
            () -> geocoder.geocodificarOpcoes("72500108"));
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

    @Test
    void buscaPostalDoViaCepRetornaCamposEReutilizaCache() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-107","logradouro":"Quadra AC 200 Bloco G","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Endereco endereco = geocoder.buscarEndereco("72500107");
        CepGeocoder.Endereco doCache = geocoder.buscarEndereco("72500107");

        assertEquals(new CepGeocoder.Endereco(
            "72500107", "Quadra AC 200 Bloco G", "Santa Maria", "Brasília", "DF"), endereco);
        assertEquals(endereco, doCache);
        server.verify();
    }

    @Test
    void cenarioRenderNominatimBloqueadoUsaAwesomeApiECalculaFreteSemAproximar() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withSuccess(openCepGenerico("72500107"), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://awesomeapi.test/json/72500107"))
            .andExpect(header("User-Agent", "FatiaPrime-test/1.0"))
            .andRespond(withSuccess(awesome("72500107", "-16.0512196", "-47.9754279"), MediaType.APPLICATION_JSON));
        FreteProperties properties = propertiesComLoja();
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties, builder.build());

        FreteResponse resposta = new FreteController(geocoder, properties).consultar("72500-107");
        geocoder.geocodificarOpcoes("72500107");

        assertEquals("AwesomeAPI", resposta.fonteCoordenadas());
        assertFalse(resposta.calculoAproximado());
        assertEquals(new BigDecimal("5.00"), resposta.valorFrete());
        server.verify();
    }

    @Test
    void timeoutNoProvedorTemUmaRetentativaCurtaQueDaCerto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://awesomeapi.test/json/72500107"))
            .andRespond(withException(new SocketTimeoutException("Read timed out")));
        server.expect(requestTo("https://awesomeapi.test/json/72500107"))
            .andRespond(withSuccess(awesome("72500107", "-16.0512196", "-47.9754279"), MediaType.APPLICATION_JSON));
        FreteProperties properties = propertiesComLoja();
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties, builder.build());

        FreteResponse resposta = new FreteController(geocoder, properties).consultar("72500107");

        assertEquals("AwesomeAPI", resposta.fonteCoordenadas());
        assertEquals(new BigDecimal("5.00"), resposta.valorFrete());
        server.verify();
    }

    @Test
    void geocodificacaoSemResultadoNoEnderecoCaiNoCentroDoBairroAproximado() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withSuccess(openCepGenerico("72500107"), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://awesomeapi.test/json/72500107"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-107","logradouro":"Quadra AC 200 Bloco G","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Quadra%20AC%20200%20Bloco%20G", "[]");
        expectNominatim(server, "q=Santa%20Maria", """
            [{"lat":"-16.0171229","lon":"-48.0131328","address":{"city":"Santa Maria","country_code":"br"}}]
            """);
        expectNominatim(server, "q=Bras%C3%ADlia", "[]");
        FreteProperties properties = propertiesComLoja();
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties, builder.build());

        FreteResponse resposta = new FreteController(geocoder, properties).consultar("72500107");

        assertEquals("OpenStreetMap", resposta.fonteCoordenadas());
        assertTrue(resposta.calculoAproximado());
        assertEquals(new BigDecimal("5.00"), resposta.valorFrete());
        server.verify();
    }

    @Test
    void todosOsProvedoresFalhandoRetorna503ENaoRepete4xx() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(ExpectedCount.times(2), requestTo("https://brasilapi.test/72500107"))
            .andRespond(withException(new SocketTimeoutException("Read timed out")));
        server.expect(ExpectedCount.times(2), requestTo("https://awesomeapi.test/json/72500107"))
            .andRespond(withServerError());
        server.expect(ExpectedCount.times(2), requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(ExpectedCount.once(), requestTo(org.hamcrest.Matchers.containsString("postalcode=72500107")))
            .andRespond(withStatus(HttpStatus.FORBIDDEN));
        FreteProperties properties = propertiesComLoja();
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties, builder.build());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
            () -> new FreteController(geocoder, properties).consultar("72500107"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatusCode());
        server.verify();
    }

    @Test
    void cepInvalidoOuInexistenteRetorna400SemModalDeConexao() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/00000000"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(ExpectedCount.once(), requestTo("https://awesomeapi.test/json/00000000"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://viacep.test/ws/00000000/json/"))
            .andRespond(withSuccess("{\"erro\":\"true\"}", MediaType.APPLICATION_JSON));
        FreteProperties properties = propertiesComLoja();
        FreteController controller = new FreteController(new ExternalCepGeocoder(properties, builder.build()), properties);

        ResponseStatusException formatoInvalido = assertThrows(ResponseStatusException.class,
            () -> controller.consultar("7250-01"));
        ResponseStatusException inexistente = assertThrows(ResponseStatusException.class,
            () -> controller.consultar("00000-000"));

        assertEquals(HttpStatus.BAD_REQUEST, formatoInvalido.getStatusCode());
        assertEquals("Informe um CEP válido.", formatoInvalido.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, inexistente.getStatusCode());
        assertEquals("CEP não encontrado. Verifique o número informado.", inexistente.getReason());
        server.verify();
    }

    @Test
    void cepForaDoRaioRetorna404NaoEntregamos() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/01001000"))
            .andRespond(withSuccess("""
                {"cep":"01001000","city":"São Paulo","state":"SP","service":"open-cep",
                 "location":{"coordinates":{"longitude":"-46.63611","latitude":"-23.5475"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://awesomeapi.test/json/01001000"))
            .andRespond(withSuccess(awesome("01001000", "-23.5500806", "-46.6340827"), MediaType.APPLICATION_JSON));
        FreteProperties properties = propertiesComLoja();
        FreteController controller = new FreteController(new ExternalCepGeocoder(properties, builder.build()), properties);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () -> controller.consultar("01001-000"));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        assertEquals("Ainda não entregamos nessa região. Em breve abriremos novas unidades mais perto de você!",
            erro.getReason());
        server.verify();
    }

    @Test
    void cepExistenteSemLocalizacaoEmNenhumNivelRetorna422EmVezDe503() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500109"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://awesomeapi.test/json/72500109"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("https://viacep.test/ws/72500109/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500-109","logradouro":"Quadra X","bairro":"Bairro Y","localidade":"Cidade Z","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        expectNominatim(server, "q=Quadra%20X", "[]");
        expectNominatim(server, "q=Bairro%20Y", "[]");
        expectNominatim(server, "q=Cidade%20Z", "[]");
        FreteProperties properties = propertiesComLoja();
        FreteController controller = new FreteController(new ExternalCepGeocoder(properties, builder.build()), properties);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () -> controller.consultar("72500109"));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, erro.getStatusCode());
        server.verify();
    }

    private String openCepGenerico(String cep) {
        return """
            {"cep":"%s","city":"Brasília","state":"DF","service":"open-cep",
             "location":{"coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
            """.formatted(cep);
    }

    private String awesome(String cep, String lat, String lng) {
        return """
            {"cep":"%s","state":"DF","city":"Brasília","lat":"%s","lng":"%s"}
            """.formatted(cep, lat, lng);
    }

    private FreteProperties propertiesComLoja() {
        FreteProperties properties = properties();
        properties.setAwesomeApiUrl("https://awesomeapi.test/json");
        properties.setLojaLat(-16.045839);
        properties.setLojaLng(-48.033231);
        properties.setRaioKm(30);
        properties.setFaixas(List.of(
            faixa("0 a 10 km", 10, "5.00"),
            faixa("Acima de 10 ate 20 km", 20, "8.00"),
            faixa("Acima de 20 ate 30 km", 30, "12.00")));
        return properties;
    }

    private FreteProperties.Faixa faixa(String nome, double ateKm, String valor) {
        FreteProperties.Faixa faixa = new FreteProperties.Faixa();
        faixa.setNome(nome);
        faixa.setAteKm(ateKm);
        faixa.setValor(new BigDecimal(valor));
        return faixa;
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
        properties.setAwesomeApiUrl("");
        properties.setNominatimUrl("https://nominatim.test/search");
        properties.setNominatimUserAgent("FatiaPrime-test/1.0");
        return properties;
    }
}
