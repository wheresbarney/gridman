package org.gridman.commandline;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jonathanknight
 */
public class OptionTest {

    @CommandLineArg(name = "field1")
    public String testOne;

    @CommandLineArg(name = "field2")
    public String testTwo;

    @CommandLineArg(name = "field2")
    public String testTwoToo;

    @CommandLineArg(name = "field3")
    public String testThree;

    @Test
    public void testOptionConstructorSetsFields() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        Field field = clazz.getField("testOne");
        CommandLineArg arg = field.getAnnotation(CommandLineArg.class);

        Option option = new Option(arg, field);
        assertThat(field, is(option.getField()));
        assertThat(arg, is(option.getArg()));
    }

    @Test
    public void testComparable() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        field = clazz.getField("testTwo");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionTwo = new Option(arg, field);

        field = clazz.getField("testTwoToo");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionTwoToo = new Option(arg, field);

        field = clazz.getField("testThree");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionThree = new Option(arg, field);

        List<Option> expected = new ArrayList<Option>();
        expected.add(optionOne);
        expected.add(optionTwo);
        expected.add(optionTwoToo);
        expected.add(optionThree);

        List<Option> test = new ArrayList<Option>();
        test.add(optionThree);
        test.add(optionTwo);
        test.add(optionOne);
        test.add(optionTwoToo);

        Collections.sort(test);
        assertThat(expected, is(test));
    }

    @Test
    public void testEqualsWhenSameObject() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        assertThat(optionOne.equals(optionOne), is(true));
    }

    @Test
    public void testEqualsWhenObjectsEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.equals(optionTwo), is(true));
    }

    @Test
    public void testEqualsWhenWrongClass() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        assertThat(optionOne.equals(new Object()), is(false));
    }

    @Test
    public void testEqualsWhenArgsNotEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        field = clazz.getField("testTwo");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.equals(optionTwo), is(false));
    }

    @Test
    public void testEqualsWhenFieldsNotEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        field = clazz.getField("testTwo");
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.equals(optionTwo), is(false));
    }

    @Test
    public void testHashCodeWhenObjectsEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.hashCode() == optionTwo.hashCode(), is(true));
    }

    @Test
    public void testHashCodeWhenArgsNotEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        field = clazz.getField("testTwo");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.hashCode() == optionTwo.hashCode(), is(false));
    }

    @Test
    public void testHashCodeWhenFieldsNotEqual() throws Exception {
        Class<OptionTest> clazz = OptionTest.class;
        CommandLineArg arg;
        Field field;

        field = clazz.getField("testOne");
        arg = field.getAnnotation(CommandLineArg.class);
        Option optionOne = new Option(arg, field);

        field = clazz.getField("testTwo");
        Option optionTwo = new Option(arg, field);

        assertThat(optionOne.hashCode() == optionTwo.hashCode(), is(false));
    }
}

