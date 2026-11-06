import java.util.function.Supplier;

// Two methods, each with two lambdas. javac numbers the backing methods
// lambda$first$0, lambda$first$1, lambda$second$0, lambda$second$1 on recent
// JDKs but lambda$first$0..3 across the class on older ones. Used to check that
// HashComputer renumbers them to a canonical, build-JDK-independent form.
public class Lambdas {
    Runnable first() {
        Runnable a = () -> System.out.println("1");
        Runnable b = () -> System.out.println("2");
        a.run();
        return b;
    }

    Supplier<String> second() {
        Supplier<String> a = () -> "x";
        Supplier<String> b = () -> "y";
        a.get();
        return b;
    }
}
