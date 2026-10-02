package com.example.Fatia.Prime;

import java.util.Comparator;
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

        CepGeocoder.Coordenadas coordenadas;
        try {
            coordenadas = geocoder.geocodificar(cepNormalizado);
        } catch (CepGeocoder.CepNaoEncontradoException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (CepGeocoder.GeocodificacaoIndisponivelException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        }
        if (coordenadas == null) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível calcular a entrega no momento. Tente novamente."
            );
        }

        double distanciaKm = Haversine.distanciaKm(
            properties.getLojaLat(), properties.getLojaLng(),
            coordenadas.latitude(), coordenadas.longitude()
        );
        if (distanciaKm > properties.getRaioKm()) {
            throw new ResponseStatusException(statusForaDaArea, "Ainda não entregamos nessa região.");
        }

        FreteProperties.Faixa faixa = properties.getFaixas().stream()
            .sorted(Comparator.comparingDouble(FreteProperties.Faixa::getAteKm))
            .filter(item -> distanciaKm <= item.getAteKm())
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
            Math.round(distanciaKm * 10) / 10.0,
            coordenadas.fonte()
        );
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
