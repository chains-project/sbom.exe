# Terminator

A proof-of-concept to illustrate termination of Java virtual machine if a
prohibited method in invoked.

## Preparation

### Sample project

```shell
cd src/test/resources/sample-maven-project
mvn package
```

`i-am-affected-1.0-SNAPSHOT-jar-with-dependencies.jar` is created.

### Java Agent

```shell
mvn package
```

`terminator-1.0-SNAPSHOT.jar` is created and we use it to instrument the jar
of sample project.

## Demo

### Without instrumentation

#### Add

```text
Enter a number: 
1
Enter another number: 
1
What do you want to do with these numbers?
Add (1)
Subtract (2)
1
Result = 2
```

#### Subtract

```text
Enter a number: 
1
Enter another number: 
1
What do you want to do with these numbers?
Add (1)
Subtract (2)
2
Result = 0
```

### With instrumentation

#### Add

```text
Enter a number: 
1
Enter another number: 
1
What do you want to do with these numbers?
Add (1)
Subtract (2)
1
Result = 2
```

#### Subtract

```text
Enter a number: 
1
Enter another number: 
1
What do you want to do with these numbers?
Add (1)
Subtract (2)
2
You accidentally have executed `org/apache/commons/math3/analysis/function/Subtract` in the app
```

You can see that the app is terminated because a method in 
`org/apache/commons/math3/analysis/function/Subtract` is invoked.

> Note: The app is terminated because the method is invoked. It is not because
> the class is loaded.

## Concept
 
Suppose you have a function `add` in your code and it is implemented like this:
```java
int add(int x, int y) {
  return x + y;
} 
``` 
 
Its bytecode would like this:
```
 0: iload_1
 1: iload_2
 2: iadd
 3: return
``` 
 
Imagine the original source code now becomes
```java 
int add(int x, int y) {
   return maliciousCode(x,y);
}
```
 
Its bytecode will become:
```
0: aload_0
1: iload_1
2: iload_2
3: invokevirtual #7                  // Method maliciousCode:(II)I
6: ireturn 
``` 
 
Now, what I do in my proof of concept, I look for the malicious code invocation,
and I add `System.exit` call before `invokevirtual` instruction.
 
Hence the instrumented  bytecode  looks like this:
```
0: iconst_1
1: invokestatic  #7                  // Method java/lang/System.exit:(I)V
4: aload_0
5: iload_1
6: iload_2
7: invokevirtual #13                 // Method maliciousCode:(II)I
10: ireturn
```

Thus, before executing `maliciousCode`, `System.exit` is called and program exits.
