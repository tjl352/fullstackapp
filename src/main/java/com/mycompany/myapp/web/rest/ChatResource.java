package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.ChatMessage;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.ChatMessageRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.ChatMessageDTO;
import com.mycompany.myapp.web.rest.vm.ChatRequestVM;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the AI chatbot.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChatResource.class);

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatModel chatModel;

    public ChatResource(ChatMessageRepository chatMessageRepository, UserRepository userRepository, ChatModel chatModel) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.chatModel = chatModel;
    }

    /**
     * {@code GET /api/chat/history} : get the current user's chat history.
     *
     * @return the list of chat messages ordered chronologically.
     */
    @GetMapping("/history")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChatMessageDTO>> getHistory() {
        User user = getCurrentUser();
        List<ChatMessageDTO> history = chatMessageRepository
            .findAllByUserIdOrderByTimestampAsc(user.getId())
            .stream()
            .map(ChatMessageDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(history);
    }

    /**
     * {@code POST /api/chat/send} : send a message and receive an AI response.
     *
     * @param chatRequest the incoming user prompt.
     * @return the AI assistant response text.
     */
    @PostMapping("/send")
    @Transactional
    public ResponseEntity<String> sendMessage(@Valid @RequestBody ChatRequestVM chatRequest) {
        User user = getCurrentUser();
        String prompt = chatRequest.getMessage();

        List<ChatMessage> history = chatMessageRepository.findAllByUserIdOrderByTimestampAsc(user.getId());
        List<Message> messages = new ArrayList<>();

        for (ChatMessage chatMessage : history) {
            messages.add(toSpringAiMessage(chatMessage));
        }

        ChatMessage userChatMessage = new ChatMessage();
        userChatMessage.setRole(ROLE_USER);
        userChatMessage.setContent(prompt);
        userChatMessage.setTimestamp(Instant.now());
        userChatMessage.setUser(user);
        chatMessageRepository.save(userChatMessage);

        messages.add(new UserMessage(prompt));

        LOG.debug("Sending chat prompt for user {} with {} prior messages", user.getLogin(), history.size());
        String aiResponse = chatModel.call(new Prompt(messages)).getResult().getOutput().getText();

        ChatMessage assistantChatMessage = new ChatMessage();
        assistantChatMessage.setRole(ROLE_ASSISTANT);
        assistantChatMessage.setContent(aiResponse);
        assistantChatMessage.setTimestamp(Instant.now());
        assistantChatMessage.setUser(user);
        chatMessageRepository.save(assistantChatMessage);

        return ResponseEntity.ok(aiResponse);
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
    }

    private Message toSpringAiMessage(ChatMessage chatMessage) {
        if (ROLE_ASSISTANT.equalsIgnoreCase(chatMessage.getRole())) {
            return new AssistantMessage(chatMessage.getContent());
        }
        return new UserMessage(chatMessage.getContent());
    }

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }
}
