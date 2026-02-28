import com.openai.models.chat.completions.ChatCompletion;
import java.lang.reflect.Method;

public class InspectLogprobsBuilder {
    public static void main(String[] args) {
        Class<?> builderClass = ChatCompletion.Choice.Logprobs.Builder.class;
        System.out.println("Logprobs builder: " + builderClass);
        for (Method m : builderClass.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
