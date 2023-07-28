package foo.bar;

public class Triple<A,B,C> {
    private A x;
    private B y;
    private C z;

    public Triple(A x, B y, C z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public A getX() {
        return x;
    }

    public B getY() {
        return y;
    }

    public C getZ() {
        return z;
    }
}
