package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.ChatMessageDto;
import br.com.petzon.petzonapi.dto.ConversationSummaryDto;
import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.NotFoundException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.ChatMessageRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UsuarioRepository usuarioRepository;
    private final PetService petService;

    public ChatMessage save(ChatMessageDto chatMessageDto, int senderId, String conversationId) throws RegraDeNegocioException, NotFoundException {
        Usuario sender = usuarioRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Remetente não encontrado"));

        int petId = Integer.parseInt(conversationId);
        Pet petDaConversa = petService.buscarPorId(petId);

        Usuario recipient = petDaConversa.getResponsavel();

        if (recipient == null) {
            throw new RegraDeNegocioException("O pet desta conversa não tem um responsável definido.");
        }

        if (sender.getIdUsuario().equals(recipient.getIdUsuario())) {
            recipient = usuarioRepository.findById(chatMessageDto.getRecipientId())
                    .orElseThrow(() -> new NotFoundException("Destinatário da resposta não encontrado."));
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(chatMessageDto.getContent());
        message.setConversationId(conversationId);
        message.setTimestamp(Instant.now());

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public List<ConversationSummaryDto> getConversationSummaries() {
        return chatMessageRepository.findConversationSummaries();
    }
}