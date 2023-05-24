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
