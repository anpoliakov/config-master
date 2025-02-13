package by.anpoliakov;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RunnerTest {

    @Test(expected = IllegalArgumentException.class)
    public void start_parametersAreEmpty_shouldThrowException() {
        //given
        Runner runner = new Runner();
        List<String> args = new ArrayList<String>();

        //when
        runner.validateParameters(args);
    }
}