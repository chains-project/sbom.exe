import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelloWorldTest {
    @Test
    public void testHelloWorld() {
        assertEquals("Hello, world!", new HelloWorld().sayHello());
    }
}

class HelloWorld {
    public String sayHello() {
        return "Hello, world!";
    }
}
