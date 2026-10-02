package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.frete")
public class FreteProperties {

    private Double lojaLat;
    private Double lojaLng;
    private double raioKm = 30.0;
    private List<Faixa> faixas = new ArrayList<>();
    private String brasilApiUrl = "https://brasilapi.com.br/api/cep/v2";
    private String viaCepUrl = "https://viacep.com.br/ws";
    private String nominatimUrl = "https://nominatim.openstreetmap.org/search";
    private String nominatimUserAgent = "FatiaPrime/1.0 (+https://github.com/daviramalho-dev/Projeto-Fatia-Prime)";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(5);

    public Double getLojaLat() {
        return lojaLat;
    }

    public void setLojaLat(Double lojaLat) {
        this.lojaLat = lojaLat;
    }

    public Double getLojaLng() {
        return lojaLng;
    }

    public void setLojaLng(Double lojaLng) {
        this.lojaLng = lojaLng;
    }

    public double getRaioKm() {
        return raioKm;
    }

    public void setRaioKm(double raioKm) {
        this.raioKm = raioKm;
    }

    public List<Faixa> getFaixas() {
        return faixas;
    }

    public void setFaixas(List<Faixa> faixas) {
        this.faixas = faixas == null ? new ArrayList<>() : new ArrayList<>(faixas);
    }

    public String getBrasilApiUrl() {
        return brasilApiUrl;
    }

    public void setBrasilApiUrl(String brasilApiUrl) {
        this.brasilApiUrl = brasilApiUrl;
    }

    public String getViaCepUrl() {
        return viaCepUrl;
    }

    public void setViaCepUrl(String viaCepUrl) {
        this.viaCepUrl = viaCepUrl;
    }

    public String getNominatimUrl() {
        return nominatimUrl;
    }

    public void setNominatimUrl(String nominatimUrl) {
        this.nominatimUrl = nominatimUrl;
    }

    public String getNominatimUserAgent() {
        return nominatimUserAgent;
    }

    public void setNominatimUserAgent(String nominatimUserAgent) {
        this.nominatimUserAgent = nominatimUserAgent;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public static class Faixa {
        private double ateKm;
        private BigDecimal valor;
        private String nome;

        public double getAteKm() {
            return ateKm;
        }

        public void setAteKm(double ateKm) {
            this.ateKm = ateKm;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }
}