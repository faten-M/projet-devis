import java.lang.reflect.Method;
import com.openai.models.chat.completions.ChatCompletionMessage;

public class Inspect {
    public static void main(String[] args) {
        for (Method m : ChatCompletionMessage.Builder.class.getMethods()) {
            if (m.getName().contains("role") || m.getName().contains("refusal") || m.getName().contains("content")) {
                System.out.println(m);
            }
        }
    }
}
