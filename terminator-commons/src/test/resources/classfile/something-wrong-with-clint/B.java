import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class B {
    private static Method m3;
    private static Method m1;
    private static Method m2;

    public final boolean equal(Object var1) throws IllegalAccessException, InvocationTargetException {
        return (boolean) m3.invoke(var1);
    }

    public final String toStrings(Object var3) throws IllegalAccessException, InvocationTargetException {
        return (String) m2.invoke(var3);
    }

    public final int hashCode_fake(Object var2) throws IllegalAccessException, InvocationTargetException {
        return (int) m1.invoke(var2);
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("hashCode_fake", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString", Class.forName("java.lang.Object"));
            m3 = Class.forName("java.lang.Object").getMethod("equal", Class.forName("java.lang.Object"));
        } catch (ClassNotFoundException var2) {
            throw new NoClassDefFoundError(var2.getMessage());
        } catch (NoSuchMethodException var3) {
            throw new NoSuchMethodError(var3.getMessage());
        }
    }
}
