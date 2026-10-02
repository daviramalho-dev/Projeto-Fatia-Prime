package com.example.Fatia.Prime;

import com.example.Fatia.Prime.CepGeocoder.CepNaoEncontradoException;
import com.example.Fatia.Prime.CepGeocoder.Coordenadas;
import com.example.Fatia.Prime.CepGeocoder.GeocodificacaoIndisponivelException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@Component
public class ExternalCepGeocoder implements CepGeocoder {

    private static final long NOMINATIM_MIN_INTERVAL_NANOS = 1_000_000_000L;

    private final FreteProperties properties;
    private final RestClient restClient;
    private final ConcurrentMap<String, Coordenadas> cache = new ConcurrentHashMap<>();
    private final Object nominatimRateLimit = new Object();
    private long lastNominatimRequestNanos;

    @Autowired
    public ExternalCepGeocoder(FreteProperties properties) {
        this(properties, criarRestClient(properties));
    }

    ExternalCepGeocoder(FreteProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public Coordenadas geocodificar(String cep) {
        return cache.computeIfAbsent(cep, this::consultar);
    }

    private Coordenadas consultar(String cep) {
        Coordenadas brasilApi = buscarBrasilApi(cep);
        if (brasilApi != null) return brasilApi;

        EnderecoCep endereco = buscarViaCep(cep);
        return geocodificarEndereco(endereco);
    }

    private Coordenadas buscarBrasilApi(String cep) {
        try {
            JsonNode resposta = restClient.get()
                .uri(properties.getBrasilApiUrl() + "/" + cep)
                .retrieve()
                .body(JsonNode.class);
            if (resposta == null || "open-cep".equalsIgnoreCase(resposta.path("service").asText())) {
                return null;
            }
            JsonNode coordenadas = resposta.path("location").path("coordinates");
            double longitude = coordenada(coordenadas.path("longitude"));
            double latitude = coordenada(coordenadas.path("latitude"));
            if (!coordenadasValidas(latitude, longitude)) return null;
            return new Coordenadas(latitude, longitude, "BrasilAPI");
        } catch (RestClientException exception) {
            return null;
        }
    }

    private EnderecoCep buscarViaCep(String cep) {
        JsonNode resposta;
        try {
            resposta = restClient.get()
                .uri(properties.getViaCepUrl() + "/" + cep + "/json/")
                .retrieve()
                .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new GeocodificacaoIndisponivelException(exception);
        }
        if (resposta == null) throw new GeocodificacaoIndisponivelException();
        JsonNode erro = resposta.path("erro");
        if (erro.asBoolean(false) || "true".equalsIgnoreCase(erro.asText())) {
            throw new CepNaoEncontradoException();
        }

        EnderecoCep endereco = new EnderecoCep(
            texto(resposta, "logradouro"),
            texto(resposta, "bairro"),
            texto(resposta, "localidade"),
            texto(resposta, "uf")
        );
        if (endereco.logradouro().isBlank() && endereco.bairro().isBlank()) {
            throw new GeocodificacaoIndisponivelException();
        }
        return endereco;
    }

    private Coordenadas geocodificarEndereco(EnderecoCep endereco) {
        String query = String.join(", ", List.of(
                endereco.logradouro(), endereco.bairro(), endereco.localidade(), endereco.uf(), "Brasil")
            .stream().filter(value -> !value.isBlank()).toList());
        String url = UriComponentsBuilder.fromUriString(properties.getNominatimUrl())
            .queryParam("q", query)
            .queryParam("format", "jsonv2")
            .queryParam("addressdetails", "1")
            .queryParam("limit", "1")
            .build()
            .encode()
            .toUriString();

        try {
            respeitarLimiteNominatim();
            JsonNode respostas = restClient.get()
                .uri(URI.create(url))
                .header("User-Agent", properties.getNominatimUserAgent())
                .retrieve()
                .body(JsonNode.class);
            if (respostas == null || !respostas.isArray() || respostas.isEmpty()) {
                throw new GeocodificacaoIndisponivelException();
            }
            JsonNode resultado = respostas.get(0);
            double latitude = coordenada(resultado.path("lat"));
            double longitude = coordenada(resultado.path("lon"));
            if (!coordenadasValidas(latitude, longitude) || !localidadeCompativel(endereco, resultado.path("address"))) {
                throw new GeocodificacaoIndisponivelException();
            }
            return new Coordenadas(latitude, longitude, "OpenStreetMap");
        } catch (RestClientException exception) {
            throw new GeocodificacaoIndisponivelException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeocodificacaoIndisponivelException(exception);
        }
    }

    private void respeitarLimiteNominatim() throws InterruptedException {
        synchronized (nominatimRateLimit) {
            long agora = System.nanoTime();
            long espera = NOMINATIM_MIN_INTERVAL_NANOS - (agora - lastNominatimRequestNanos);
            if (lastNominatimRequestNanos != 0 && espera > 0) {
                long millis = espera / 1_000_000;
                int nanos = (int) (espera % 1_000_000);
                Thread.sleep(millis, nanos);
            }
            lastNominatimRequestNanos = System.nanoTime();
        }
    }

    private boolean localidadeCompativel(EnderecoCep endereco, JsonNode address) {
        String bairroEsperado = normalizar(endereco.bairro());
        String ruaEsperada = normalizar(endereco.logradouro());
        if (!bairroEsperado.isBlank()) {
            for (String campo : List.of("suburb", "city_district", "town", "city", "village", "municipality")) {
                String localidade = normalizar(texto(address, campo));
                if (!localidade.isBlank()
                    && (localidade.equals(bairroEsperado) || localidade.contains(bairroEsperado) || bairroEsperado.contains(localidade))) {
                    return true;
                }
            }
        }
        if (!ruaEsperada.isBlank()) {
            for (String campo : List.of("road", "residential", "pedestrian")) {
                String rua = normalizar(texto(address, campo));
                if (!rua.isBlank() && (rua.equals(ruaEsperada) || rua.contains(ruaEsperada) || ruaEsperada.contains(rua))) {
                    return true;
                }
            }
        }
        return bairroEsperado.isBlank() && ruaEsperada.isBlank()
            && normalizar(texto(address, "city")).equals(normalizar(endereco.localidade()));
    }

    private double coordenada(JsonNode valor) {
        try {
            return valor.isNumber() ? valor.asDouble() : Double.parseDouble(valor.asText());
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
    }

    private boolean coordenadasValidas(double latitude, double longitude) {
        return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
            && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
    }

    private String texto(JsonNode node, String campo) {
        return node.path(campo).asText("").trim();
    }

    private String normalizar(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private static RestClient criarRestClient(FreteProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.getConnectTimeout())
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private record EnderecoCep(String logradouro, String bairro, String localidade, String uf) {
    }
}