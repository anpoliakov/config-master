package by.anpoliakov;

import org.junit.Test;

import java.util.ArrayList;

public class RunnerTest {

    @Test(expected = IllegalArgumentException.class)
    public void start_parametersAreEmpty_shouldThrowException() {
        Runner runner = new Runner();
        runner.validateParameters(new ArrayList<String>());
    }
}