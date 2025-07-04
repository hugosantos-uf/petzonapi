package br.com.petzon.petzonapi.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ChatMessageResponseDto {
    private Long id;
    private String conversationId;
    private ResponsavelDto sender;
    private ResponsavelDto recipient;
    private String content;
    private Instant timestamp;
}