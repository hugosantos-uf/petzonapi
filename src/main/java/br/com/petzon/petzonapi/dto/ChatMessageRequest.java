package br.com.petzon.petzonapi.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChatMessageRequest {
    @NotNull
    private Integer recipientId;
    @NotBlank
    private String content;
}