package ru.askar.common.cli.output;

/**
 * Класс для вывода ответов CLI в консоль.
 */
public class Stdout implements OutputWriter {
    @Override
    public void write(String message) {
        if (message.isEmpty()) return;
        System.out.println(message);
    }
}
