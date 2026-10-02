package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FreteDistanciaTests {

    @Test
    void haversineCalculaDistanciasConhecidas() {
        double longitudeDeDezKm = Math.toDegrees(10.0 / 6371.0088);

        assertEquals(10.0, Haversine.distanciaKm(0, 0, 0, longitudeDeDezKm), 0.0001);
        assertEquals(0.0, Haversine.distanciaKm(-16.0, -47.0, -16.0, -47.0), 0.0001);
    }

    @Test
    void aplicaFaixasNosLimitesIncluindoZeroEDistanciaMaxima() {
        FreteProperties properties = properties();
        FreteController controller = new FreteController(cep -> coordenadaA(cep), properties);

        assertCotacao(controller, "99990000", "0 a 10 km", "5.00", 5.0);
        assertCotacao(controller, "99990001", "0 a 10 km", "5.00", 10.0);
        assertCotacao(controller, "99990002", "Acima de 10 ate 20 km", "8.00", 10.1);
        assertCotacao(controller, "99990003", "Acima de 10 ate 20 km", "8.00", 20.0);
        assertCotacao(controller, "99990004", "Acima de 20 ate 30 km", "12.00", 20.1);
        assertCotacao(controller, "99990005", "Acima de 20 ate 30 km", "12.00", 30.0);
    }

    @Test
    void cepComHifenENormalizadoUsaMesmaCoordenada() {
        FreteController controller = new FreteController(cep -> {
            assertEquals("72500100", cep);
            return coordenadaParaDistancia(5.0);
        }, properties());

        assertEquals("72500100", controller.consultar("72500-100").cep());
    }

    @Test
    void foraDoRaioRetorna404() {
        FreteController controller = new FreteController(cep -> coordenadaParaDistancia(30.01), properties());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> controller.consultar("72500100")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ainda não entregamos nessa região.", exception.getReason());
    }

    @Test
    void cepInvalidoRetorna400AntesDeConsultarGeocoder() {
        FreteController controller = new FreteController(
            cep -> { throw new AssertionError("Geocoder não deve receber CEP inválido"); }, properties());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> controller.consultar("7250-100")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void indisponibilidadeDoGeocoderRetorna503ECoordenadasDaLojaFaltantesTambem() {
        FreteController indisponivel = new FreteController(cep -> {
            throw new CepGeocoder.GeocodificacaoIndisponivelException();
        }, properties());
        FreteProperties semCoordenadas = properties();
        semCoordenadas.setLojaLat(null);
        FreteController naoConfigurado = new FreteController(cep -> coordenadaParaDistancia(5), semCoordenadas);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, assertThrows(
            ResponseStatusException.class, () -> indisponivel.consultar("72500100")).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, assertThrows(
            ResponseStatusException.class, () -> naoConfigurado.consultar("72500100")).getStatusCode());
    }

    private void assertCotacao(
        FreteController controller,
        String cep,
        String regiao,
        String valor,
        double distancia
    ) {
        FreteResponse response = controller.consultar(cep);
        assertEquals(regiao, response.regiao());
        assertEquals(new BigDecimal(valor), response.valorFrete());
        assertEquals(distancia, response.distanciaKm(), 0.0001);
    }

    private FreteProperties properties() {
        FreteProperties properties = new FreteProperties();
        properties.setLojaLat(0.0);
        properties.setLojaLng(0.0);
        properties.setRaioKm(30.0);
        properties.setFaixas(List.of(
            faixa(10, "5.00", "0 a 10 km"),
            faixa(20, "8.00", "Acima de 10 ate 20 km"),
            faixa(30, "12.00", "Acima de 20 ate 30 km")
        ));
        return properties;
    }

    private FreteProperties.Faixa faixa(double limite, String valor, String nome) {
        FreteProperties.Faixa faixa = new FreteProperties.Faixa();
        faixa.setAteKm(limite);
        faixa.setValor(new BigDecimal(valor));
        faixa.setNome(nome);
        return faixa;
    }

    private CepGeocoder.Coordenadas coordenadaA(String cep) {
        double distancia = switch (cep) {
            case "99990000" -> 5.0;
            case "99990001" -> 10.0;
            case "99990002" -> 10.1;
            case "99990003" -> 20.0;
            case "99990004" -> 20.1;
            case "99990005" -> 30.0;
            default -> 0.0;
        };
        return coordenadaParaDistancia(distancia);
    }

    private CepGeocoder.Coordenadas coordenadaParaDistancia(double distanciaKm) {
        double longitude = Math.toDegrees(distanciaKm / 6371.0088);
        return new CepGeocoder.Coordenadas(0, longitude, "teste");
    }
}