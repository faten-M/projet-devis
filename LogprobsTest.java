import com.openai.models.chat.completions.ChatCompletion;

public class LogprobsTest {
    public static void main(String[] args) {
        ChatCompletion.Choice.Logprobs lp = ChatCompletion.Choice.Logprobs.builder().build();
        System.out.println("Created logprobs: " + lp);
    }
}
