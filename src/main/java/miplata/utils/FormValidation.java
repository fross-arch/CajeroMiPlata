package miplata.utils;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Utilidades de validación de entrada por consola.
 */
public class FormValidation {

    private static final Scanner sc = new Scanner(System.in);

    public static int validateInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                int value = sc.nextInt();
                sc.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("  Error: ingresa un número entero válido.");
                sc.nextLine();
            }
        }
    }

    // ERROR #4 / #7 / #10: valida que la opción esté dentro de un rango [min, max]
    public static int validateIntRange(String prompt, int min, int max) {
        while (true) {
            int value = validateInt(prompt);
            if (value >= min && value <= max) return value;
            System.out.println("  Error: opción no válida. Elige entre " + min + " y " + max + ".");
        }
    }

    public static double validateDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("  Error: el campo no puede estar vacío.");
                continue;
            }
            try {
                return Double.parseDouble(input.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("  Error: ingresa un número válido.");
            }
        }
    }

    public static String validateString(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("  Error: el campo no puede estar vacío.");
        }
    }

    // ERROR #1: contraseña con mínimo 4 caracteres
    public static String validatePassword(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String value = sc.nextLine().trim();
            if (value.isEmpty()) {
                System.out.println("  Error: el campo no puede estar vacío.");
            } else if (value.length() < 4) {
                System.out.println("  Error: la contraseña debe tener mínimo 4 caracteres.");
            } else {
                return value;
            }
        }
    }

    public static long validateLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                long value = sc.nextLong();
                sc.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("  Error: ingresa un número válido.");
                sc.nextLine();
            }
        }
    }

    public static String validateNumerico(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String value = sc.nextLine().trim();
            if (value.isEmpty()) {
                System.out.println("  Error: el campo no puede estar vacío.");
            } else if (!value.matches("\\d+")) {
                System.out.println("  Error: solo se permiten números.");
            } else {
                return value;
            }
        }
    }

    public static void pausar() {
        System.out.println("\nPresiona 1 para volver al menú.");
        while (validateInt("Opción") != 1) {
            System.out.println("  Presiona 1 para volver al menú.");
        }
    }
}