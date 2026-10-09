package br.com.fiap.java_afetto.util;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// As 27 UFs, para validar o "estado" do endereço e guardar o nome completo
public final class EstadosBrasil {

    private static final Map<String, String> NOMES = Map.ofEntries(
            Map.entry("AC", "Acre"),
            Map.entry("AL", "Alagoas"),
            Map.entry("AP", "Amapá"),
            Map.entry("AM", "Amazonas"),
            Map.entry("BA", "Bahia"),
            Map.entry("CE", "Ceará"),
            Map.entry("DF", "Distrito Federal"),
            Map.entry("ES", "Espírito Santo"),
            Map.entry("GO", "Goiás"),
            Map.entry("MA", "Maranhão"),
            Map.entry("MT", "Mato Grosso"),
            Map.entry("MS", "Mato Grosso do Sul"),
            Map.entry("MG", "Minas Gerais"),
            Map.entry("PA", "Pará"),
            Map.entry("PB", "Paraíba"),
            Map.entry("PR", "Paraná"),
            Map.entry("PE", "Pernambuco"),
            Map.entry("PI", "Piauí"),
            Map.entry("RJ", "Rio de Janeiro"),
            Map.entry("RN", "Rio Grande do Norte"),
            Map.entry("RS", "Rio Grande do Sul"),
            Map.entry("RO", "Rondônia"),
            Map.entry("RR", "Roraima"),
            Map.entry("SC", "Santa Catarina"),
            Map.entry("SP", "São Paulo"),
            Map.entry("SE", "Sergipe"),
            Map.entry("TO", "Tocantins")
    );

    private EstadosBrasil() {
    }

    // "sp" -> "São Paulo"; UF que não existe -> vazio
    public static Optional<String> nome(String sigla) {
        return sigla == null
                ? Optional.empty()
                : Optional.ofNullable(NOMES.get(sigla.trim().toUpperCase(Locale.ROOT)));
    }
}
