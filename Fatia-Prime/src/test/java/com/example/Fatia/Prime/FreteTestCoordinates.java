package com.example.Fatia.Prime;

final class FreteTestCoordinates {

    private FreteTestCoordinates() {
    }

    static CepGeocoder.Coordenadas paraCep(String cep) {
        double distancia = switch (cep) {
            case "99990000" -> 5.0;
            case "99990100" -> 15.0;
            case "99990200", "99990299" -> 25.0;
            case "99990300", "99990400" -> 31.0;
            default -> 5.0;
        };
        double longitude = Math.toDegrees(distancia / 6371.0088);
        return new CepGeocoder.Coordenadas(0.0, longitude, "fixture de teste");
    }
}