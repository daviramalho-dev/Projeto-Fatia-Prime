package com.example.Fatia.Prime;

import com.example.Fatia.Prime.CepGeocoder.CepNaoEncontradoException;
import com.example.Fatia.Prime.CepGeocoder.Coordenadas;
import com.example.Fatia.Prime.CepGeocoder.GeocodificacaoIndisponivelException;
import java.net.URI;
import java.net.http.HttpClient;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final String BRASILIA_OPEN_CEP_LATITUDE = "-15.77972";
    private static final String BRASILIA_OPEN_CEP_LONGITUDE = "-47.92972";

    private final FreteProperties properties;
    private final RestClient restClient;
    private final ConcurrentMap<String, List<Coordenadas>> cache = new ConcurrentHashMap<>();
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
        return geocodificarOpcoes(cep).get(0);
    }

    @Override
    public List<Coordenadas> geocodificarOpcoes(String cep) {
        return cache.computeIfAbsent(cep, this::consultar);
    }

    private List<Coordenadas> consultar(String cep) {
        Coordenadas brasilApi = buscarBrasilApi(cep);
        if (brasilApi != null) return List.of(brasilApi);

        EnderecoCep endereco;
        try {
            endereco = buscarViaCep(cep);
        } catch (GeocodificacaoIndisponivelException exception) {
            List<Coordenadas> porCep = buscarNoNominatimPorCep(cep);
            if (!porCep.isEmpty()) return porCep;
            throw new GeocodificacaoIndisponivelException(exception);
        }
        return geocodificarEndereco(cep, endereco);
    }

    private Coordenadas buscarBrasilApi(String cep) {
        try {
            JsonNode resposta = restClient.get()
                .uri(properties.getBrasilApiUrl() + "/" + cep)
                .retrieve()
                .body(JsonNode.class);
            if (resposta == null
                || !"correios".equalsIgnoreCase(texto(resposta, "service"))
                || !cep.equals(normalizarCep(texto(resposta, "cep")))
                || texto(resposta, "city").isBlank()
                || texto(resposta, "state").isBlank()) {
                return null;
            }

            JsonNode coordenadas = resposta.path("location").path("coordinates");
            double longitude = coordenada(coordenadas.path("longitude"));
            double latitude = coordenada(coordenadas.path("latitude"));
            if (!coordenadasValidas(latitude, longitude)
                || latitude < -34 || latitude > 6 || longitude < -74 || longitude > -34
                || coordenadaGenericaBrasilia(latitude, longitude)) {
                return null;
            }
            return new Coordenadas(latitude, longitude, "BrasilAPI/Correios", false);
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
        if (erro.asBoolean(false) || "true".equalsIgnoreCase(erro.asString(""))) {
            throw new CepNaoEncontradoException();
        }

        EnderecoCep endereco = new EnderecoCep(
            texto(resposta, "logradouro"),
            texto(resposta, "bairro"),
            texto(resposta, "localidade"),
            texto(resposta, "uf")
        );
        if (endereco.logradouro().isBlank() && endereco.bairro().isBlank()
            && endereco.localidade().isBlank()) {
            throw new GeocodificacaoIndisponivelException();
        }
        return endereco;
    }

    private List<Coordenadas> geocodificarEndereco(String cep, EnderecoCep endereco) {
        Map<String, Coordenadas> coordenadas = new LinkedHashMap<>();
        GeocodificacaoIndisponivelException falhaProvedor = null;
        List<Consulta> consultas = List.of(
            new Consulta(
                String.join(", ", List.of(
                    endereco.logradouro(), endereco.bairro(), endereco.localidade(), endereco.uf(), "Brasil")
                    .stream().filter(value -> !value.isBlank()).toList()),
                NivelBusca.ENDERECO
            ),
            new Consulta(
                String.join(", ", List.of(
                    endereco.bairro(), endereco.localidade(), endereco.uf(), "Brasil")
                    .stream().filter(value -> !value.isBlank()).toList()),
                NivelBusca.BAIRRO
            ),
            new Consulta(
                String.join(", ", List.of(endereco.localidade(), endereco.uf(), "Brasil")
                    .stream().filter(value -> !value.isBlank()).toList()),
                NivelBusca.LOCALIDADE
            )
        );

        for (Consulta consulta : consultas) {
            if (consulta.query().isBlank()) continue;
            try {
                adicionarUnicos(coordenadas, buscarNoNominatim(consulta, endereco));
            } catch (GeocodificacaoIndisponivelException exception) {
                if (falhaProvedor == null) falhaProvedor = exception;
            }
        }
        if (!coordenadas.isEmpty()) return List.copyOf(coordenadas.values());
        if (falhaProvedor != null) throw falhaProvedor;
        throw new GeocodificacaoIndisponivelException();
    }

    private List<Coordenadas> buscarNoNominatim(Consulta consulta, EnderecoCep endereco) {
        String url = UriComponentsBuilder.fromUriString(properties.getNominatimUrl())
            .queryParam("q", consulta.query())
            .queryParam("format", "jsonv2")
            .queryParam("addressdetails", "1")
            .queryParam("limit", "5")
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
            return extrairResultados(respostas, endereco, consulta.nivel());
        } catch (RestClientException exception) {
            throw new GeocodificacaoIndisponivelException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeocodificacaoIndisponivelException(exception);
        }
    }

    private List<Coordenadas> buscarNoNominatimPorCep(String cep) {
        String url = UriComponentsBuilder.fromUriString(properties.getNominatimUrl())
            .queryParam("postalcode", cep)
            .queryParam("country", "Brazil")
            .queryParam("format", "jsonv2")
            .queryParam("addressdetails", "1")
            .queryParam("limit", "5")
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
            if (respostas == null || !respostas.isArray()) return List.of();

            List<Coordenadas> coordenadas = new ArrayList<>();
            for (JsonNode resposta : respostas) {
                JsonNode address = resposta.path("address");
                if (!cep.equals(normalizarCep(texto(address, "postcode")))
                    || !paisCompativel(address)
                    || !coordenadasDoBrasil(resposta)) {
                    continue;
                }
                adicionarCoordenada(coordenadas, resposta, "OpenStreetMap/CEP");
            }
            return List.copyOf(coordenadas);
        } catch (RestClientException exception) {
            throw new GeocodificacaoIndisponivelException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeocodificacaoIndisponivelException(exception);
        }
    }

    private List<Coordenadas> extrairResultados(JsonNode respostas, EnderecoCep endereco, NivelBusca nivel) {
        if (respostas == null || !respostas.isArray()) return List.of();
        List<Coordenadas> coordenadas = new ArrayList<>();
        for (JsonNode resposta : respostas) {
            JsonNode address = resposta.path("address");
            boolean compativel = switch (nivel) {
                case ENDERECO -> logradouroCompativel(endereco.logradouro(), address)
                    && localidadeCompativel(endereco, address);
                case BAIRRO -> bairroCompativel(endereco.bairro(), address);
                case LOCALIDADE -> cidadeCompativel(endereco.localidade(), address);
            };
            if (compativel) adicionarCoordenada(coordenadas, resposta, "OpenStreetMap");
        }
        return List.copyOf(coordenadas);
    }

    private void adicionarCoordenada(List<Coordenadas> destino, JsonNode resultado, String fonte) {
        double latitude = coordenada(resultado.path("lat"));
        double longitude = coordenada(resultado.path("lon"));
        if (coordenadasValidas(latitude, longitude)) {
            destino.add(new Coordenadas(latitude, longitude, fonte, true));
        }
    }

    private void adicionarUnicos(Map<String, Coordenadas> destino, List<Coordenadas> coordenadas) {
        for (Coordenadas coordenada : coordenadas) {
            destino.putIfAbsent(coordenada.latitude() + "," + coordenada.longitude(), coordenada);
        }
    }

    private boolean logradouroCompativel(String esperado, JsonNode address) {
        String ruaEsperada = normalizar(esperado);
        if (ruaEsperada.isBlank()) return false;
        for (String campo : List.of("road", "residential", "pedestrian", "street")) {
            String rua = normalizar(texto(address, campo));
            if (!rua.isBlank() && (rua.equals(ruaEsperada)
                || rua.contains(ruaEsperada) || ruaEsperada.contains(rua))) {
                return true;
            }
        }
        return false;
    }

    private boolean localidadeCompativel(EnderecoCep endereco, JsonNode address) {
        return bairroCompativel(endereco.bairro(), address)
            || cidadeCompativel(endereco.localidade(), address);
    }

    private boolean bairroCompativel(String esperado, JsonNode address) {
        return campoCompativel(esperado, address,
            List.of("neighbourhood", "suburb", "city_district", "quarter", "town", "city", "village", "municipality"));
    }

    private boolean cidadeCompativel(String esperado, JsonNode address) {
        return campoCompativel(esperado, address,
            List.of("city", "town", "village", "municipality", "county"));
    }

    private boolean campoCompativel(String esperado, JsonNode address, List<String> campos) {
        String valorEsperado = normalizar(esperado);
        if (valorEsperado.isBlank()) return false;
        if (!paisCompativel(address)) return false;
        for (String campo : campos) {
            String valor = normalizar(texto(address, campo));
            if (!valor.isBlank()
                && (valor.equals(valorEsperado) || valor.contains(valorEsperado) || valorEsperado.contains(valor))) {
                return true;
            }
        }
        return false;
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

    private double coordenada(JsonNode valor) {
        try {
            return valor.isNumber() ? valor.asDouble() : Double.parseDouble(valor.asString());
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
    }

    private boolean coordenadasValidas(double latitude, double longitude) {
        return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
            && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
    }

    private boolean coordenadasDoBrasil(JsonNode resultado) {
        double latitude = coordenada(resultado.path("lat"));
        double longitude = coordenada(resultado.path("lon"));
        return coordenadasValidas(latitude, longitude)
            && latitude >= -34 && latitude <= 6
            && longitude >= -74 && longitude <= -34;
    }

    private boolean paisCompativel(JsonNode address) {
        String pais = normalizar(texto(address, "country_code"));
        return pais.isBlank() || pais.equals("br");
    }

    private boolean coordenadaGenericaBrasilia(double latitude, double longitude) {
        return Math.abs(latitude - Double.parseDouble(BRASILIA_OPEN_CEP_LATITUDE)) < 0.0001
            && Math.abs(longitude - Double.parseDouble(BRASILIA_OPEN_CEP_LONGITUDE)) < 0.0001;
    }

    private String texto(JsonNode node, String campo) {
        return node.path(campo).asString("").trim();
    }

    private String normalizar(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private String normalizarCep(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
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

    private record Consulta(String query, NivelBusca nivel) {
    }

    private enum NivelBusca {
        ENDERECO,
        BAIRRO,
        LOCALIDADE
    }
}
