package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatMessageService.getChatHistory(conversationId));
    }
}