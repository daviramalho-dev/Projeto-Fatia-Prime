package com.example.Fatia.Prime;

public interface CepGeocoder {

    Coordenadas geocodificar(String cep);

    record Coordenadas(double latitude, double longitude, String fonte) {
    }

    class CepNaoEncontradoException extends RuntimeException {
        public CepNaoEncontradoException() {
            super("CEP não encontrado. Verifique o número informado.");
        }
    }

    class GeocodificacaoIndisponivelException extends RuntimeException {
        public GeocodificacaoIndisponivelException() {
            super("Não foi possível calcular a entrega no momento. Tente novamente.");
        }

        public GeocodificacaoIndisponivelException(Throwable cause) {
            super("Não foi possível calcular a entrega no momento. Tente novamente.", cause);
        }
    }
}