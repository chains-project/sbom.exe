import com.google.common.base.Joiner;

public class Main {
    public static String main(String[] args) {
        Joiner joiner = Joiner.on("; ").skipNulls();
        return joiner.join("Harry", null, "Ron", "Hermione");
    }
}
