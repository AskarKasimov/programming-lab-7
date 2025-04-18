package ru.askar.common.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.askar.common.cli.CommandResponseCode;
import ru.askar.common.cli.input.InputReader;
import ru.askar.common.cli.output.OutputWriter;
import ru.askar.common.exception.UserRejectedToFillFieldsException;

import java.io.Serializable;
import java.util.Objects;

public class Event implements Comparable<Event>, Serializable {
    private Integer id;
    private String name;
    private String description;
    private EventType eventType;

    @JsonCreator
    public Event(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("eventType") EventType eventType) {
        setId(id);
        setName(name);
        setDescription(description);
        setEventType(eventType);
    }

    private Event(Integer id) {
        setId(id);
    }

    /**
     * Создание экземпляра с пользовательским вводом.
     *
     * @param outputWriter - способ печати ответа
     * @param inputReader  - способ считывания входных данных
     */
    public static Event createEvent(
            OutputWriter outputWriter, InputReader inputReader, Integer id, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        Event event = new Event(id);
        outputWriter.write(CommandResponseCode.INFO.getColoredMessage("Ввод события"));
        event.requestName(outputWriter, inputReader, scriptMode);
        event.requestDescription(outputWriter, inputReader, scriptMode);
        event.requestEventType(outputWriter, inputReader, scriptMode);
        return event;
    }

    private void requestName(OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        String name;
        do {
            outputWriter.write("Введите название события: ");
            try {
                name = inputReader.getInputString();
                this.setName(name);
            } catch (IllegalArgumentException e) {
                name = null;
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
        } while (name == null);
    }

    private void requestDescription(
            OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        String description;
        do {
            outputWriter.write("Введите описание события: ");
            try {
                description = inputReader.getInputString();
                this.setDescription(description);
            } catch (IllegalArgumentException e) {
                description = null;
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
        } while (description == null);
    }

    private void requestEventType(
            OutputWriter outputWriter, InputReader inputReader, boolean scriptMode)
            throws UserRejectedToFillFieldsException {
        outputWriter.write(
                CommandResponseCode.WARNING.getColoredMessage(
                        "Хотите ввести тип события? (y/n): "));
        String answer = inputReader.getInputString();
        if (answer != null && answer.equalsIgnoreCase("y")) {
            setEventType(EventType.createEventType(outputWriter, inputReader, scriptMode));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id)
                && Objects.equals(name, event.name)
                && Objects.equals(description, event.description)
                && eventType == event.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, eventType);
    }

    @Override
    public int compareTo(Event other) {
        // Сначала сравниваем по типу события
        if (this.eventType != null && other.eventType != null) {
            int eventTypeComparison = this.eventType.compareTo(other.eventType);
            if (eventTypeComparison != 0) {
                return eventTypeComparison;
            }
        }

        // Если типы равны или null, сравниваем по имени
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return "Событие"
                + ": id="
                + id
                + ", название='"
                + name
                + "'"
                + ", описание='"
                + description
                + "'"
                + ", тип="
                + eventType
                + ";";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
