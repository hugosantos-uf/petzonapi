package br.com.petzon.petzonapi.repository;

import br.com.petzon.petzonapi.dto.ConversationSummaryDto;
import br.com.petzon.petzonapi.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Query(value = "SELECT " +
            "    m1.conversation_id as conversationId, " +
            "    p.nome as petNome, " +
            "    u.nome as usuarioNome, " +
            "    m1.content as ultimaMensagem, " +
            "    m1.timestamp as timestamp " +
            "FROM " +
            "    chat_messages m1 " +
            "JOIN " +
            "    usuarios u ON m1.sender_id = u.id_usuario " +
            "JOIN " +
            "    pets p ON p.id = CAST(SUBSTRING(m1.conversation_id FROM POSITION('-' IN m1.conversation_id) + 1) AS INTEGER) " +
            "WHERE " +
            "    m1.timestamp = (SELECT MAX(m2.timestamp) FROM chat_messages m2 WHERE m2.conversation_id = m1.conversation_id) " +
            "ORDER BY " +
            "    m1.timestamp DESC", nativeQuery = true)
    List<ConversationSummaryDto> findConversationSummaries();
}