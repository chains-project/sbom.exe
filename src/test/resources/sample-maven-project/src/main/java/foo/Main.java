package foo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a number: ");
        int number = scanner.nextInt();

        System.out.println("Enter another number: ");
        int anotherNumber = scanner.nextInt();

        System.out.println("What do you want to do with these numbers?");
        System.out.println("Add (1)");
        System.out.println("Subtract (2)");

        int choice = scanner.nextInt();

        if (choice == 1) {
            System.out.println("Result = " + add(number, anotherNumber));
        } else if (choice == 2) {
            System.out.println("Result = " + subtract(number, anotherNumber));
        }
    }

    private static int add(int a, int b) {
        return a + b;
    }

    private static int subtract(int a, int b) {
        return a - b;
    }
}