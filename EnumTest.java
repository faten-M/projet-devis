import com.openai.models.chat.completions.ChatCompletion;

public class EnumTest {
    public static void main(String[] args) {
        for (ChatCompletion.Choice.FinishReason r : ChatCompletion.Choice.FinishReason.values()) {
            System.out.println(r);
        }
    }
}
