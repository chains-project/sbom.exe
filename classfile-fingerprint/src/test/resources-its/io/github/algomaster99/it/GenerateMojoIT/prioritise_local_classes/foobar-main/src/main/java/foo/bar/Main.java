package foo.bar;

public class Main {
    public static void main(String[] args) {
        Triple<String, String, String> triple = new Triple<>("Hello", "World", "!");
        System.out.println(triple.getX() + " " + triple.getY() + triple.getZ());
    }
}
