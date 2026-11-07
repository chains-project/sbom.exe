import java.util.function.Supplier;

// Two methods, each with two lambdas. javac 24+ numbers the backing methods
// per enclosing method (lambda$first$0, lambda$first$1, lambda$second$0,
// lambda$second$1) while javac 23 and older number them across the class
// (lambda$second$2, lambda$second$3). jdk23/ and jdk24/ hold the output of
// `javac --release 17 Lambdas.java` from each, used to check that HashComputer
// gives both the same hash.
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
