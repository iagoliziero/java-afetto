package br.com.fiap.java_afetto.dto.calendario;

import java.time.LocalDate;
import java.util.UUID;

public record EventoCalendario(

        TipoEventoCalendario tipo,

        // Ex.: "V10", "Amoxicilina", "Check-up anual"
        String titulo,

        // Ex.: "Próxima dose", "1 comprimido · A cada 12 horas", nome da clínica
        String detalhe,

        LocalDate data,

        // "HH:mm" quando o evento tem horário (consultas); senão null
        String hora,

        // Só nas consultas: AGENDADO ou CONCLUIDO
        String status,

        // Id da vacina, do remédio ou da consulta, para o app abrir o detalhe
        UUID idReferencia,

        UUID idPet,
        String nomePet

) {
}
