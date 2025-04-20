package ru.askar.common.object;

import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.UserRejectedToFillFieldsException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Coordinates implements Serializable {
    private Float x;
    private Float y;

    public Coordinates(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    private Coordinates() {
    }

    /**
     * Создание экземпляра с пользовательским вводом.
     *
     * @param outputWriter - способ печати ответа
     * @param inputReader  - способ считывания входных данных
     * @return - созданный Coordinates
     */
    public static Coordinates createCoordinates(
            OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        Coordinates coordinates = new Coordinates();
        outputWriter.write("Ввод координат: ");
        coordinates.requestX(outputWriter, inputReader, scriptMode);
        coordinates.requestY(outputWriter, inputReader, scriptMode);
        return coordinates;
    }

    private void requestX(OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        BigDecimal x;
        do {
            outputWriter.write("Введите x: ");
            try {
                x = inputReader.getInputBigDecimal();
                this.x = x.floatValue();
            } catch (IllegalArgumentException e) {
                x = null;
                if (scriptMode) {
                    throw new UserRejectedToFillFieldsException();
                }
                outputWriter.write(CommandResponseCode.ERROR.getColoredMessage(e.getMessage()));
                outputWriter.write(
                        CommandResponseCode.WARNING.getColoredMessage(
                                "Хотите попробовать еще раз? (y/n): "));
                String answer = inputReader.getInputString();
                if (answer != null && !answer.equalsIgnoreCase("y")) {
                    throw new UserRejectedToFillFieldsException();
                }
            }
        } while (x == null);
    }

    private void requestY(OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        BigDecimal y;
        do {
            outputWriter.write("Введите y: ");
            try {
                y = inputReader.getInputBigDecimal();
                this.y = y.floatValue();
            } catch (IllegalArgumentException e) {
                y = null;
                if (scriptMode) {
                    throw new UserRejectedToFillFieldsException();
                }
                outputWriter.write(CommandResponseCode.ERROR.getColoredMessage(e.getMessage()));
                outputWriter.write(
                        CommandResponseCode.WARNING.getColoredMessage(
                                "Хотите попробовать еще раз? (y/n): "));
                String answer = inputReader.getInputString();
                if (answer != null && !answer.equalsIgnoreCase("y")) {
                    throw new UserRejectedToFillFieldsException();
                }
            }
        } while (y == null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Координаты" + ": x=" + x + ", y=" + y;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }
}
