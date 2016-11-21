package com.jderda.flymanager.seats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import com.jderda.flymanager.seats.domain.input.InputData;
import com.jderda.flymanager.seats.domain.output.SeatsArrangement;

@RunWith(Parameterized.class)
public abstract class AbstractInputScenariosRunner {
    
    protected InputData inputData;
    protected Callable<SeatsArrangement> job;
    
    @Before
    public abstract void init();
    
    public AbstractInputScenariosRunner(String testName, String testData) {
        inputData = InputData.of(new BufferedReader(new StringReader(testData)));
    }

    @Test
    public void testNumberOfRowsGreaterThanZero() {
        Assert.assertTrue(inputData.getNumberOfRows() > 0);
    }

    @Test
    public void testAlgorithmReturnsResult() throws Exception {
        SeatsArrangement result = job.call();
        Assert.assertNotNull(result);
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        result.print(writer);
        writer.flush();
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        Set<String> testCasesLocations = new Reflections("testCases", new ResourcesScanner())
                .getResources(Pattern.compile(".*\\.txt"));
        
        return testCasesLocations.stream()
            .map(AbstractInputScenariosRunner::readTestCase)
            .filter(tc -> tc!=null)
            .collect(Collectors.toList());
        
    }
    
    private static Object[] readTestCase(String location) {
        String content;
        try {
            content = IOUtils.toString(AbstractInputScenariosRunner.class.getClassLoader().getResourceAsStream(location), "UTF-8");
            return new Object[] {location, content};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
}
