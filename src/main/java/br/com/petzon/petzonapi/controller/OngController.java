package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.ConversationSummaryDto;
import br.com.petzon.petzonapi.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ong") // Novo caminho base para funcionalidades da ONG
@RequiredArgsConstructor
public class OngController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/chat/conversations")
    @PreAuthorize("hasRole('ONG')") // Protegido para o papel de ONG
    public ResponseEntity<List<ConversationSummaryDto>> getConversations() {
        return ResponseEntity.ok(chatMessageService.getConversationSummaries());
    }
}