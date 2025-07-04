package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.ChatMessageRequest;
import br.com.petzon.petzonapi.dto.ChatMessageResponseDto;
import br.com.petzon.petzonapi.dto.ConversationSummaryDto;
import br.com.petzon.petzonapi.dto.ResponsavelDto;
import br.com.petzon.petzonapi.entity.ChatMessage;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.ChatMessageRepository;
import br.com.petzon.petzonapi.repository.PetRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;

    @Transactional
    public ChatMessageResponseDto saveAndMapToDto(ChatMessageRequest chatMessageDto, int senderId, String conversationId){
        Usuario sender = usuarioRepository.findById(senderId)
                .orElseThrow(() -> new RegraDeNegocioException("Remetente não encontrado"));
        String[] ids = conversationId.split("-");
        if (ids.length < 2) {
            throw new RegraDeNegocioException("ID de conversa inválido: " + conversationId);
        }
        Integer petId = Integer.parseInt(ids[1]);
        Pet petDaConversa = petRepository.findById(petId)
                .orElseThrow(() -> new RegraDeNegocioException("Pet não encontrado"));
        Usuario responsavelPeloPet = petDaConversa.getResponsavel();
        Usuario recipient;
        if (sender.getIdUsuario().equals(responsavelPeloPet.getIdUsuario())) {
            recipient = usuarioRepository.findById(chatMessageDto.getRecipientId())
                    .orElseThrow(() -> new RegraDeNegocioException("Destinatário da resposta não encontrado."));
        } else {
            recipient = responsavelPeloPet;
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(chatMessageDto.getContent());
        message.setConversationId(conversationId);
        message.setTimestamp(Instant.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageResponseDto responseDto = new ChatMessageResponseDto();
        responseDto.setId(savedMessage.getId());
        responseDto.setConversationId(savedMessage.getConversationId());
        responseDto.setContent(savedMessage.getContent());
        responseDto.setTimestamp(savedMessage.getTimestamp());

        ResponsavelDto senderDto = new ResponsavelDto();
        senderDto.setIdUsuario(savedMessage.getSender().getIdUsuario());
        senderDto.setNome(savedMessage.getSender().getNome());
        senderDto.setEmail(savedMessage.getSender().getEmail()); // <-- ADICIONE ESTA LINHA
        responseDto.setSender(senderDto);

        // Mapeia o recipient, agora incluindo o email
        ResponsavelDto recipientDto = new ResponsavelDto();
        recipientDto.setIdUsuario(savedMessage.getRecipient().getIdUsuario());
        recipientDto.setNome(savedMessage.getRecipient().getNome());
        recipientDto.setEmail(savedMessage.getRecipient().getEmail()); // <-- ADICIONE ESTA LINHA
        responseDto.setRecipient(recipientDto);

        return responseDto;
    }


    public List<ChatMessage> getChatHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public List<ConversationSummaryDto> getConversationSummaries() {
        return chatMessageRepository.findConversationSummaries();
    }
}