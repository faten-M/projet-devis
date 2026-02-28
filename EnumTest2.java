import com.openai.models.chat.completions.ChatCompletion;
import java.lang.reflect.Field;

public class EnumTest2 {
    public static void main(String[] args) throws Exception {
        System.out.println("FinishReason class: " + ChatCompletion.Choice.FinishReason.class);
        System.out.println("Known inner class: " + ChatCompletion.Choice.FinishReason.Known.class);
        System.out.println("Fields in Known:");
        for (Field f : ChatCompletion.Choice.FinishReason.Known.class.getFields()) {
            System.out.println("  " + f.getName() + " -> " + f.get(null));
        }
        // Attempt to see static methods
        for (var m : ChatCompletion.Choice.FinishReason.class.getMethods()) {
            if (m.getName().contains("of") || m.getName().contains("from") || m.getName().contains("known")) {
                System.out.println("method: " + m);
            }
        }
    }
}