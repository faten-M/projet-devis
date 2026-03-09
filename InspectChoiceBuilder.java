import com.openai.models.chat.completions.ChatCompletion;
import java.lang.reflect.Method;

public class InspectChoiceBuilder {
    public static void main(String[] args) {
        Class<?> builderClass = ChatCompletion.Choice.Builder.class;
        System.out.println("Builder class: " + builderClass);
        for (Method m : builderClass.getDeclaredMethods()) {
            if (m.getName().contains("logprobs") || m.getName().contains("finishReason") || m.getName().contains("index")) {
                System.out.println(m);
            }
        }
    }
}
