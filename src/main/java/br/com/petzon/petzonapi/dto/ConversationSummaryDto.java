package br.com.petzon.petzonapi.dto;

import java.time.Instant;

public interface ConversationSummaryDto {
    String getConversationId();

    String getPetNome();

    String getUsuarioNome();

    String getUltimaMensagem();

    Instant getTimestamp();
}