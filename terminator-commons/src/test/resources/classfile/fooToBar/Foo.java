public class Foo {
    private static int x = 1;
    private static int y = 2;

    public int sum() {
        return Foo.x + Foo.y;
    }

    public int subtract() {
        return Foo.x - Foo.y;
    }
}
