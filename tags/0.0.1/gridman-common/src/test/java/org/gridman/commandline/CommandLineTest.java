package org.gridman.commandline;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jonathanknight
 */
public class CommandLineTest {
    enum TestEnum {
        one, two, three
    }

    private Random random = new Random(System.currentTimeMillis());

    public String stringField;
    public int intField;
    public Integer integerField;
    public boolean boolField;
    public Boolean booleanField;
    public TestEnum enumField;
    public Object badField;
    private String privateField;

    @Test
    public void testValidEnumValues() {
        CommandLine commandLine = new CommandLine();
        String result = commandLine.validEnumValues(TestEnum.class);
        assertThat("one, two, three", is(result));
    }

    @Test
    public void assertConstructorSetsTarget() {
        CommandLine commandLine = new CommandLine(this);
        assertSame(this, commandLine.getTarget());
    }

    @Test
    public void assertConstructorSetsOptions() throws Exception {
        MockChild child = new MockChild();

        Option opt;
        Field field;
        CommandLineArg arg;

        Map<String, Option> expected = new HashMap<String, Option>();

        field = MockParent.class.getDeclaredField("fieldA");
        arg = field.getAnnotation(CommandLineArg.class);
        expected.put("-a", new Option(arg, field));
        field = MockParent.class.getDeclaredField("fieldB");
        arg = field.getAnnotation(CommandLineArg.class);
        expected.put("-b", new Option(arg, field));
        field = MockChild.class.getDeclaredField("fieldC");
        arg = field.getAnnotation(CommandLineArg.class);
        expected.put("-c", new Option(arg, field));
        field = MockChild.class.getDeclaredField("fieldD");
        arg = field.getAnnotation(CommandLineArg.class);
        expected.put("-d", new Option(arg, field));

        CommandLine commandLine = new CommandLine(child);
        Map<String, Option> result = commandLine.getOptions();
        assertThat(expected, is(result));
    }

    @Test
    public void testConvertString() throws Exception {
        String text = UUID.randomUUID().toString();

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("stringField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(text, is(result));
    }

    @Test
    public void testConvertInt() throws Exception {
        int expected = random.nextInt(1000);
        String text = String.valueOf(expected);

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("intField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(expected, is(result));
    }

    @Test
    public void testConvertInteger() throws Exception {
        Integer expected = random.nextInt(1000);
        String text = String.valueOf(expected);

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("integerField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(expected, is(result));
    }

    @Test(expected = ArgumentShouldBeIntegerException.class)
    public void testConvertBadInteger() throws Exception {
        String text = "not a number";

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");

        Field field = CommandLineTest.class.getField("intField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.convert(option, text);
    }

    @Test
    public void testConvertBool() throws Exception {
        boolean expected = false;
        String text = String.valueOf(expected);

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("boolField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(expected, is(result));
    }

    @Test
    public void testConvertBoolean() throws Exception {
        Boolean expected = true;
        String text = String.valueOf(expected);

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("booleanField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(expected, is(result));
    }

    @Test(expected = ArgumentShouldBeBooleanException.class)
    public void testConvertBadBoolean() throws Exception {
        String text = "not a boolean";

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");

        Field field = CommandLineTest.class.getField("booleanField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.convert(option, text);
    }

    @Test
    public void testConvertEnum() throws Exception {
        TestEnum expected = TestEnum.three;
        String text = String.valueOf(expected);

        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("enumField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        Object result = commandLine.convert(option, text);
        assertThat(expected, is(result));
    }

    @Test(expected = ArgumentShouldBeException.class)
    public void testConvertBadEnum() throws Exception {
        String text = "bad_enum";

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");

        Field field = CommandLineTest.class.getField("enumField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.convert(option, text);
    }

    @Test(expected = UnhandledClassTypeException.class)
    public void testConvertBadType() throws Exception {
        String text = "bad_enum";

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");
        Field field = CommandLineTest.class.getField("badField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.convert(option, text);
    }

    @Test
    public void testSetObjectPublicField() throws Exception {
        String value = UUID.randomUUID().toString();

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");
        Field field = CommandLineTest.class.getField("stringField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setTarget(this);
        this.stringField = null;
        commandLine.setValue(option, value);
        assertThat(value, is(this.stringField));
    }

    @Test
    public void testSetObjectPrivateField() throws Exception {
        String value = UUID.randomUUID().toString();

        CommandLineArg arg = mock(CommandLineArg.class);
        when(arg.name()).thenReturn("-name");
        Field field = CommandLineTest.class.getDeclaredField("privateField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setTarget(this);
        this.privateField = null;
        commandLine.setValue(option, value);
        assertThat(value, is(this.privateField));
    }

    @Test(expected = UnrecognisedArgumentException.class)
    public void testParseArgumentDoesNotStartWithMinusSign() {
        CommandLine commandLine = new CommandLine();
        commandLine.parse("bad");
    }

    @Test
    public void testParseWithNoArgumentsAndNoOptions() {
        CommandLine commandLine = new CommandLine();
        commandLine.parse();
    }

    @Test(expected = UnrecognisedArgumentException.class)
    public void testParseArgumentIsNotMappedToField() throws Exception {
        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("stringField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setOptions(Collections.singletonMap("-a", option));
        commandLine.parse("-b", "b_value");
    }

    @Test(expected = CommandLineException.class)
    public void testParseWhenArgumentHasValueButValueNotInArgList() throws Exception {
        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("stringField");

        when(arg.hasValue()).thenReturn(true);

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setOptions(Collections.singletonMap("-a", option));
        commandLine.parse("-a");
    }

    @Test
    public void testParseWhenArgumentHasValueIsFalseAndNoValueNotInArgList() throws Exception {
        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("stringField");

        when(arg.hasValue()).thenReturn(false);
        when(arg.name()).thenReturn("-a");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setTarget(this);
        commandLine.setOptions(Collections.singletonMap("-a", option));
        this.stringField = null;
        commandLine.parse("-a");
        assertThat("true", is(this.stringField));
    }

    @Test(expected = UnrecognisedArgumentException.class)
    public void testParseEqualsArgumentIsNotMappedToField() throws Exception {
        CommandLineArg arg = mock(CommandLineArg.class);
        Field field = CommandLineTest.class.getField("stringField");

        Option option = new Option(arg, field);

        CommandLine commandLine = new CommandLine();
        commandLine.setOptions(Collections.singletonMap("-a", option));
        commandLine.parse("-b=b_value");
    }

    @Test(expected = RequiredArgumentMissingException.class)
    public void testParseWithMissingRequiredArgument() throws Exception {
        Field field = CommandLineTest.class.getField("stringField");

        CommandLineArg arg1 = mock(CommandLineArg.class);
        CommandLineArg arg2 = mock(CommandLineArg.class);
        when(arg1.name()).thenReturn("-a");
        when(arg1.required()).thenReturn(true);
        when(arg2.name()).thenReturn("-b");
        when(arg2.required()).thenReturn(true);
        when(arg2.hasValue()).thenReturn(true);

        Map<String, Option> opts = new HashMap<String, Option>();
        opts.put("-a", new Option(arg1, field));
        opts.put("-b", new Option(arg2, field));

        CommandLine commandLine = new CommandLine();
        commandLine.setOptions(opts);
        commandLine.parse("-b", "b_value");
    }

    @Test
    public void testParseSetsFields() throws Exception {
        String expectedString = UUID.randomUUID().toString();
        int expectedInt = random.nextInt(1000);
        boolean expectedBoolean = true;
        String[] keys = {"-a", "-b", "-c"};
        String[] args = {keys[0], String.valueOf(expectedString)
                , keys[1], String.valueOf(expectedInt)
                , keys[2], String.valueOf(expectedBoolean)};

        Field field1 = CommandLineTest.class.getField("stringField");
        Field field2 = CommandLineTest.class.getField("intField");
        Field field3 = CommandLineTest.class.getField("booleanField");

        CommandLineArg arg1 = mock(CommandLineArg.class);
        CommandLineArg arg2 = mock(CommandLineArg.class);
        CommandLineArg arg3 = mock(CommandLineArg.class);

        when(arg1.name()).thenReturn(keys[0]);
        when(arg1.required()).thenReturn(true);
        when(arg1.hasValue()).thenReturn(true);

        when(arg2.name()).thenReturn(keys[1]);
        when(arg2.required()).thenReturn(true);
        when(arg2.hasValue()).thenReturn(true);

        when(arg3.name()).thenReturn(keys[2]);
        when(arg3.required()).thenReturn(true);
        when(arg3.hasValue()).thenReturn(true);

        Map<String, Option> opts = new HashMap<String, Option>();
        opts.put(keys[0], new Option(arg1, field1));
        opts.put(keys[1], new Option(arg2, field2));
        opts.put(keys[2], new Option(arg3, field3));

        this.stringField = null;
        this.integerField = null;
        this.booleanField = null;

        CommandLine commandLine = new CommandLine();
        commandLine.setTarget(this);
        commandLine.setOptions(opts);
        commandLine.parse(args);

        assertThat(expectedString, is(this.stringField));
        assertThat(expectedInt, is(this.intField));
        assertThat(expectedBoolean, is(this.booleanField));
    }
}
