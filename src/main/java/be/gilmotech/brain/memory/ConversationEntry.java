package be.gilmotech.brain.memory;

public record ConversationEntry(String role, String content, String toolCallId) {
    public ConversationEntry(String role, String content) {
        this(role, content, null);
    }
}
