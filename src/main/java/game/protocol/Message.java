package game.protocol;

public class Message {
    private MessageType type;
    private String[] data;
    
    public Message(MessageType type, String[] data) {
        this.type = type;
        this.data = data;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String[] getData() {
        return data;
    }
    
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
        
        if (data != null) {
            for (String item : data) {
                sb.append("|").append(item);
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    public static Message fromProtocolString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = line.trim().split("\\|");
        if (parts.length == 0) {
            return null;
        }
        
        try {
            MessageType type = MessageType.valueOf(parts[0]);
            String[] data = new String[parts.length - 1];
            System.arraycopy(parts, 1, data, 0, data.length);
            return new Message(type, data);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}







