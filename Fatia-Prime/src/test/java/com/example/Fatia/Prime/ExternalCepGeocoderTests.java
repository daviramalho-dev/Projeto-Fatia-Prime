package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ExternalCepGeocoderTests {

    @Test
    void usaCoordenadasDaBrasilApiQuandoFonteNaoGenerica() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500100"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""
                {"service":"correios","location":{"type":"Point","coordinates":{"longitude":"-47.99","latitude":"-16.02"}}}
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500100");
        geocoder.geocodificar("72500100");

        assertEquals(-16.02, coordenadas.latitude(), 0.000001);
        assertEquals(-47.99, coordenadas.longitude(), 0.000001);
        assertEquals("BrasilAPI", coordenadas.fonte());
        server.verify();
    }

    @Test
    void fonteOpenCepGenericaUsaViaCepENominatimEArmazenaCache() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500100"))
            .andRespond(withSuccess("""
                {"service":"open-cep","location":{"type":"Point","coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.test/ws/72500100/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500100","logradouro":"Quadra AC 200","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("https://nominatim.test/search?q=")))
            .andExpect(header("User-Agent", "FatiaPrime-test/1.0"))
            .andRespond(withSuccess("""
                [{"lat":"-16.0272972","lon":"-47.9889102","address":{"city":"Santa Maria"}}]
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500100");
        geocoder.geocodificar("72500100");

        assertEquals(-16.0272972, coordenadas.latitude(), 0.000001);
        assertEquals(-47.9889102, coordenadas.longitude(), 0.000001);
        assertEquals("OpenStreetMap", coordenadas.fonte());
        server.verify();
    }

    @Test
    void cepComLogradouroNaoMapeadoNoOpenStreetMapUsaFallbackValidadoPeloBairro() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500107"))
            .andRespond(withSuccess("""
                {"service":"open-cep","location":{"type":"Point","coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.test/ws/72500107/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500107","logradouro":"Quadra AC 200 Bloco G","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("https://nominatim.test/search?q=Quadra%20AC%20200%20Bloco%20G")))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("https://nominatim.test/search?q=Santa%20Maria")))
            .andRespond(withSuccess("""
                [{"lat":"-16.0171229","lon":"-48.0131328","address":{"city":"Santa Maria"}}]
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500107");

        assertEquals(-16.0171229, coordenadas.latitude(), 0.000001);
        assertEquals(-48.0131328, coordenadas.longitude(), 0.000001);
        assertEquals("OpenStreetMap", coordenadas.fonte());
        server.verify();
    }

    @Test
    void outroCepValidoDoMesmoBairroTambemUsaFallbackDeLocalidade() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500101"))
            .andRespond(withSuccess("""
                {"service":"open-cep","location":{"type":"Point","coordinates":{"longitude":"-47.92972","latitude":"-15.77972"}}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.test/ws/72500101/json/"))
            .andRespond(withSuccess("""
                {"cep":"72500101","logradouro":"Quadra AC 200 Bloco A","bairro":"Santa Maria","localidade":"Brasília","uf":"DF"}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("https://nominatim.test/search?q=Quadra%20AC%20200%20Bloco%20A")))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("https://nominatim.test/search?q=Santa%20Maria")))
            .andRespond(withSuccess("""
                [{"lat":"-16.0171229","lon":"-48.0131328","address":{"city":"Santa Maria"}}]
                """, MediaType.APPLICATION_JSON));
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        CepGeocoder.Coordenadas coordenadas = geocoder.geocodificar("72500101");

        assertEquals(-16.0171229, coordenadas.latitude(), 0.000001);
        assertEquals(-48.0131328, coordenadas.longitude(), 0.000001);
        assertEquals("OpenStreetMap", coordenadas.fonte());
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
    void falhaDosProvedoresOuGeocodificacaoSemCorrespondenciaNaoProduzDistanciaForaDaArea() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://brasilapi.test/72500100"))
            .andRespond(withServerError());
        server.expect(requestTo("https://viacep.test/ws/72500100/json/"))
            .andRespond(withServerError());
        ExternalCepGeocoder geocoder = new ExternalCepGeocoder(properties(), builder.build());

        assertThrows(CepGeocoder.GeocodificacaoIndisponivelException.class,
            () -> geocoder.geocodificar("72500100"));
        server.verify();
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