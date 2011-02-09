package org.gridman.commandline;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author jonathanknight
 */
public class CommandLine {
    private static final int PAD_LIMIT = 8192;

    private Map<String, Option> options = new HashMap<String, Option>();
    private Object target;

    CommandLine() {
    }

    public CommandLine(Object target) {
        this.target = target;
        for (Class c = target.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                CommandLineArg o = f.getAnnotation(CommandLineArg.class);
                if (o != null) {
                    options.put(o.name(), new Option(o, f));
                }
            }
        }

    }

    Map<String, Option> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    void setOptions(Map<String, Option> options) {
        this.options.clear();
        this.options.putAll(options);
    }

    Object getTarget() {
        return target;
    }

    void setTarget(Object target) {
        this.target = target;
    }

    public void parse(String... args) throws CommandLineException {
        Map<String, String> values = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                String argName;
                String argText;
                int index;
                if ((index = arg.indexOf('=')) < 0) {
                    argName = arg;
                    Option option = options.get(argName);
                    if (option == null) {
                        throw new UnrecognisedArgumentException(arg);
                    }
                    if (option.getArg().hasValue()) {
                        i++;
                        if (i >= args.length) {
                            throw new CommandLineException("Invalid command line at argument " + argName);
                        }
                        argText = args[i];
                    } else {
                        argText = "true";
                    }
                } else {
                    argName = arg.substring(0, index);
                    argText = arg.substring(index + 1);
                }

                if (!options.containsKey(argName)) {
                    throw new UnrecognisedArgumentException(arg);
                }

                values.put(argName, argText);
            } else {
                throw new UnrecognisedArgumentException(arg);
            }
        }

        List<Option> list = new ArrayList<Option>(options.values());
        Collections.sort(list);

        for (Option option : list) {
            String text = values.get(option.getArg().name());
            if (text == null && option.getArg().required()) {
                throw new RequiredArgumentMissingException(option.getArg().name());
            } else if (text != null) {
                Object value = convert(option, text);
                setValue(option, value);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    Object convert(Option option, String text) throws CommandLineException {
        Object value;
        Field f = option.getField();
        Class type = f.getType();
        if (type.equals(String.class)) {
            value = text;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            try {
                value = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new ArgumentShouldBeIntegerException(option.getArg().name());
            }
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            if ("true".equalsIgnoreCase(text)) {
                value = true;
            } else if ("false".equalsIgnoreCase(text)) {
                value = false;
            } else {
                throw new ArgumentShouldBeBooleanException(option.getArg().name());
            }
        } else if (Enum.class.isAssignableFrom(type)) {
            try {
                value = Enum.valueOf(type, text);
            } catch (IllegalArgumentException e) {
                throw new ArgumentShouldBeException(option.getArg().name(), validEnumValues(type));
            }
        } else {
            throw new UnhandledClassTypeException(option.getArg().name(), type);
        }
        return value;
    }

    void setValue(Option option, Object value) {
        Field f = option.getField();
        try {
            f.set(target, value);
        } catch (IllegalAccessException ignore) {
            // try again
            f.setAccessible(true);
            try {
                f.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
    }

    String validEnumValues(Class<? extends Enum> type) {
        StringBuilder msg = new StringBuilder();
        Enum[] values = type.getEnumConstants();
        for (Enum value : values) {
            msg.append(value.toString()).append(", ");
        }
        return msg.toString().substring(0, msg.length() - 2);
    }

    public String usage() {
        StringBuilder msg = new StringBuilder("Usage:\n");
        msg.append(target.getClass().getCanonicalName());

        List<Option> list = new ArrayList<Option>(options.values());
        Collections.sort(list);

        int length = 0;
        for (Option option : list) {
            CommandLineArg arg = option.getArg();
            if (arg.required()) {
                int test = arg.name().length();
                length = Math.max(length, test);
                msg.append(" ");
                msg.append(arg.name())
                        .append(" ")
                        .append(arg.usageVar());
            }
        }
        for (Option option : list) {
            if (!option.getArg().required()) {
                length = Math.max(length, option.getArg().name().length());
                msg.append(" [")
                        .append(option.getArg().name())
                        .append(" ")
                        .append(option.getArg().usageVar())
                        .append("]");
            }
        }

        msg.append("\n");

        for (Option option : list) {
            String name = option.getArg().name();
            boolean required = option.getArg().required();
            name = rightPad(name, length);
            msg.append(name)
                    .append(" - ")
                    .append(required ? "(required) - " : "(optional) - ");
            String[] usages = option.getArg().usage();
            if (usages != null && usages.length > 0) {
                msg.append(usages[0])
                        .append("\n");
                for (int i = 1; i < usages.length; i++) {
                    msg.append(leftPad(" ", length + 16))
                            .append(usages[i])
                            .append("\n");
                }
            }
        }
        return msg.toString().trim();
    }

    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(padding(pads, padChar));
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (padStr == null || padStr.length() == 0) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return padding(pads, padChar).concat(str);
    }

    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (padStr == null || padStr.length() == 0) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }
}
