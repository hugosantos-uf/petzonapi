package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.ChatMessageDto;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.service.ChatMessageService;
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
    public void sendMessage(@DestinationVariable String conversationId, @Payload ChatMessageDto chatMessageDto, Authentication authentication) throws RegraDeNegocioException {
        Integer senderId = Integer.parseInt(authentication.getName());

        ChatMessage savedMessage = chatMessageService.save(chatMessageDto, senderId, conversationId);

        String recipientUsername = savedMessage.getRecipient().getEmail();
        String senderUsername = savedMessage.getSender().getEmail();

        String privateQueueDestination = "/queue/messages";

        messagingTemplate.convertAndSendToUser(recipientUsername, privateQueueDestination, savedMessage);

        messagingTemplate.convertAndSendToUser(senderUsername, privateQueueDestination, savedMessage);
    }
}