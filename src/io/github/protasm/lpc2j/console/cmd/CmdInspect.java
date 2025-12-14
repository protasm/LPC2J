package io.github.protasm.lpc2j.console.cmd;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdInspect extends Command {

    @Override
    public boolean execute(LPCConsole console, String... args) {
        if (args.length != 1) {
            System.out.println("Usage: inspect <object>");

            return true;
        }

        String objName = args[0];
        Object obj = console.objects().get(objName);

        if (obj == null) {
            System.out.println("Object not found: " + objName);

            return true;
        }

        Class<?> clazz = obj.getClass();

        System.out.println("Object: " + clazz.getName());
        System.out.println("\nFields:");

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // allows access to private fields

            try {
                Object value = field.get(obj);

                System.out.printf("  %s %s %s = %s%n", Modifier.toString(field.getModifiers()),
                        field.getType().getSimpleName(), field.getName(), String.valueOf(value));
            } catch (IllegalAccessException e) {
                System.out.printf("  %s %s %s = <inaccessible>%n", Modifier.toString(field.getModifiers()),
                        field.getType().getSimpleName(), field.getName());
            }
        }

        System.out.println("\nMethods:");

        for (Method method : clazz.getDeclaredMethods()) {
            String params = Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));

            System.out.printf("  %s %s %s(%s)%n", Modifier.toString(method.getModifiers()),
                    method.getReturnType().getSimpleName(), method.getName(), params);
        }

        return true;
    }

    @Override
    public String toString() {
        return "Inspect fields and methods of a named object";
    }
}
