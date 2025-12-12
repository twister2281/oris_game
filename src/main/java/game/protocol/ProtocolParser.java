package game.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProtocolParser {
    
    public static void sendMessage(PrintWriter out, Message message) {
        if (out != null && message != null) {
            out.print(message.toProtocolString());
            out.flush();
        }
    }
    
    public static Message receiveMessage(BufferedReader in) throws IOException {
        if (in == null) {
            return null;
        }
        
        String line = in.readLine();
        if (line == null) {
            return null;
        }
        
        return Message.fromProtocolString(line);
    }
}


