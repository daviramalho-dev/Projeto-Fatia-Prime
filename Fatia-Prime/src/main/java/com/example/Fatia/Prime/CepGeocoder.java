package com.example.Fatia.Prime;

import java.util.List;

public interface CepGeocoder {

    Coordenadas geocodificar(String cep);

    default List<Coordenadas> geocodificarOpcoes(String cep) {
        Coordenadas coordenada = geocodificar(cep);
        return coordenada == null ? List.of() : List.of(coordenada);
    }

    default Endereco buscarEndereco(String cep) {
        throw new GeocodificacaoIndisponivelException();
    }

    record Endereco(String cep, String logradouro, String bairro, String localidade, String uf) {
    }

    record Coordenadas(double latitude, double longitude, String fonte, boolean aproximada) {
        public Coordenadas(double latitude, double longitude, String fonte) {
            this(latitude, longitude, fonte, false);
        }
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