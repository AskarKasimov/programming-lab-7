package ru.askar.common.cli;

import java.io.Serializable;
import ru.askar.common.CommandResponse;
import ru.askar.common.exception.ExitCLIException;

/** Абстрактный класс для команд */
public abstract class Command implements Serializable {
    protected final int argsCount;
    protected final String name;
    protected final String info;
    protected boolean scriptMode = false;

    /**
     * Заполнение имени и количества требуемых аргументов
     *
     * @param name
     * @param argsCount
     */
    public Command(String name, int argsCount, String info) {
        this.name = name;
        this.argsCount = argsCount;
        this.info = info;
    }

    public void setScriptMode(boolean scriptMode) {
        this.scriptMode = scriptMode;
    }

    /**
     * Выполнение логики команды
     *
     * @param args - аргументы
     */
    public abstract CommandResponse execute(String[] args) throws ExitCLIException;

    /** Выдать справку об использовании команды */
    public String getInfo() {
        return info;
    }

    public String getName() {
        return name;
    }

    public int getArgsCount() {
        return argsCount;
    }
}
