package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.ChatMessageRequest;
import br.com.petzon.petzonapi.dto.ChatMessageResponseDto;
import br.com.petzon.petzonapi.dto.ResponsavelDto;
import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/{conversationId}/sendMessage")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Payload ChatMessageRequest chatMessageDto,
            Authentication authentication) {

        int senderId = Integer.parseInt(authentication.getName());

        ChatMessageResponseDto messageResponse = chatMessageService.saveAndMapToDto(chatMessageDto, senderId, conversationId);

        String recipientUsername = messageResponse.getRecipient().getEmail(); // Supondo que o DTO tenha o email
        String senderUsername = messageResponse.getSender().getEmail(); // Supondo que o DTO tenha o email

        String privateQueueDestination = "/queue/messages";

        messagingTemplate.convertAndSendToUser(recipientUsername, privateQueueDestination, messageResponse);
        messagingTemplate.convertAndSendToUser(senderUsername, privateQueueDestination, messageResponse);
    }
}