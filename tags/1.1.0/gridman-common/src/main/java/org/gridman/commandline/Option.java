package org.gridman.commandline;

import java.lang.reflect.Field;

/**
 * @author jonathanknight
 */
public class Option implements Comparable<Option> {
    private CommandLineArg arg;
    private Field field;

    public Option(CommandLineArg arg, Field field) {
        this.arg = arg;
        this.field = field;
    }

    public CommandLineArg getArg() {
        return arg;
    }

    public Field getField() {
        return field;
    }

    public int compareTo(Option o) {
        return this.arg.name().compareTo(o.arg.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Option option = (Option) o;

        return arg.equals(option.arg) && field.equals(option.field);
    }

    @Override
    public int hashCode() {
        int result = arg.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }
}
