package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.ChatMessageDto;
import br.com.petzon.petzonapi.dto.ConversationSummaryDto;
import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UsuarioService usuarioService;
    private final PetService petService;

    public ChatMessage save(ChatMessageDto chatMessageDto, int senderId, String conversationId) throws RegraDeNegocioException, PetNaoEncontradoException {
        Usuario sender = usuarioService.findById(senderId)
                .orElseThrow(() -> new RegraDeNegocioException("Remetente não encontrado"));

        int petId = Integer.parseInt(conversationId);
        Pet petDaConversa = petService.buscarPorId(petId);

        Usuario recipient = petDaConversa.getResponsavel();

        if (recipient == null) {
            throw new RegraDeNegocioException("O pet desta conversa não tem um responsável definido.");
        }

        if (sender.getIdUsuario().equals(recipient.getIdUsuario())) {
            recipient = usuarioService.findById(chatMessageDto.getRecipientId())
                    .orElseThrow(() -> new RegraDeNegocioException("Destinatário da resposta não encontrado."));
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