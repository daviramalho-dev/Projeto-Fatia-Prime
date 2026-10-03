package com.example.Fatia.Prime;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/frete")
public class FreteController {

    private final CepGeocoder geocoder;
    private final FreteProperties properties;

    public FreteController(CepGeocoder geocoder, FreteProperties properties) {
        this.geocoder = geocoder;
        this.properties = properties;
    }

    @GetMapping("/consulta")
    public FreteResponse consultar(@RequestParam(required = false) String cep) {
        return calcularFrete(cep, geocoder, properties, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/endereco")
    public CepGeocoder.Endereco buscarEndereco(@RequestParam(required = false) String cep) {
        String cepNormalizado = normalizarCep(cep);
        try {
            return geocoder.buscarEndereco(cepNormalizado);
        } catch (CepGeocoder.CepNaoEncontradoException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (CepGeocoder.GeocodificacaoIndisponivelException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        }
    }

    static FreteResponse calcularFrete(
        String cep,
        CepGeocoder geocoder,
        FreteProperties properties,
        HttpStatus statusForaDaArea
    ) {
        String cepNormalizado = normalizarCep(cep);
        if (!coordenadasLojaConfiguradas(properties)) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "As coordenadas da loja ainda não foram configuradas para calcular a entrega."
            );
        }

        List<CepGeocoder.Coordenadas> opcoes;
        try {
            opcoes = geocoder.geocodificarOpcoes(cepNormalizado);
        } catch (CepGeocoder.CepNaoEncontradoException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (CepGeocoder.GeocodificacaoIndisponivelException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        }
        if (opcoes == null || opcoes.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível calcular a entrega no momento. Tente novamente."
            );
        }

        List<CotacaoCoordenada> cotacoes = opcoes.stream()
            .filter(FreteController::coordenadasValidas)
            .map(coordenadas -> new CotacaoCoordenada(
                coordenadas,
                Haversine.distanciaKm(
                    properties.getLojaLat(), properties.getLojaLng(),
                    coordenadas.latitude(), coordenadas.longitude()
                )
            ))
            .sorted(Comparator.comparing(CotacaoCoordenada::aproximada))
            .toList();
        if (cotacoes.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível calcular a entrega no momento. Tente novamente."
            );
        }

        CotacaoCoordenada cotacao = cotacoes.stream()
            .filter(item -> !item.aproximada())
            .findFirst()
            .orElseGet(() -> cotacoes.stream()
                .filter(item -> item.distanciaKm() <= properties.getRaioKm())
                .findFirst()
                .orElse(cotacoes.get(0)));
        if (cotacao.distanciaKm() > properties.getRaioKm()) {
            throw new ResponseStatusException(
                statusForaDaArea,
                "Ainda não entregamos nessa região. Em breve abriremos novas unidades mais perto de você!"
            );
        }

        FreteProperties.Faixa faixa = properties.getFaixas().stream()
            .sorted(Comparator.comparingDouble(FreteProperties.Faixa::getAteKm))
            .filter(item -> cotacao.distanciaKm() <= item.getAteKm())
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "As faixas de frete não estão configuradas para essa distância."
            ));

        if (faixa.getValor() == null || faixa.getValor().signum() < 0) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "As faixas de frete não estão configuradas corretamente."
            );
        }

        return new FreteResponse(
            cepNormalizado,
            faixa.getNome(),
            faixa.getValor(),
            Math.round(cotacao.distanciaKm() * 10) / 10.0,
            cotacao.coordenadas().fonte(),
            cotacao.aproximada(),
            true
        );
    }

    private static boolean coordenadasValidas(CepGeocoder.Coordenadas coordenadas) {
        return coordenadas != null
            && Double.isFinite(coordenadas.latitude()) && coordenadas.latitude() >= -90 && coordenadas.latitude() <= 90
            && Double.isFinite(coordenadas.longitude()) && coordenadas.longitude() >= -180 && coordenadas.longitude() <= 180;
    }

    private record CotacaoCoordenada(CepGeocoder.Coordenadas coordenadas, double distanciaKm) {
        private boolean aproximada() {
            return coordenadas.aproximada();
        }
    }

    static String normalizarCep(String cep) {
        if (cep == null || !cep.trim().matches("[0-9]{5}-?[0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um CEP válido.");
        }
        return cep.replaceAll("\\D", "");
    }

    private static boolean coordenadasLojaConfiguradas(FreteProperties properties) {
        Double latitude = properties.getLojaLat();
        Double longitude = properties.getLojaLng();
        return latitude != null && longitude != null
            && Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
            && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180
            && Double.isFinite(properties.getRaioKm()) && properties.getRaioKm() > 0;
    }
}
