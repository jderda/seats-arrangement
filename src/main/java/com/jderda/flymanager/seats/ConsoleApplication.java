package com.jderda.flymanager.seats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;

import com.jderda.flymanager.seats.domain.input.InputData;
import com.jderda.flymanager.seats.domain.output.SeatsArrangement;
import com.jderda.flymanager.seats.jobs.CheckAllPossibileSeatArrangementsJob;
import com.jderda.flymanager.seats.jobs.SimpleAssignSeatsJob;

public class ConsoleApplication {
    
    private static final String ERROR_CANNOT_READ_FILE = "Cannot read specified file";
    private static final String ERROR_INVALID_ARGUMENTS = "You have to specify exactly one argument";
    private static final String ERROR_INVALID_PATH = "Specified path does not exist or is not a file";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(ERROR_INVALID_ARGUMENTS);
            System.exit(0);
        }
        String path = args[0];
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            System.out.println(ERROR_INVALID_PATH);
            System.exit(0);
        }
        
        OutputStreamWriter outputWriter = new OutputStreamWriter(System.out);
        try (FileReader reader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            InputData data = InputData.of(bufferedReader);
            
            Callable<SeatsArrangement> job;
            if (args.length > 1 && args[1].equalsIgnoreCase("-all")) {
                job = CheckAllPossibileSeatArrangementsJob.builder()
                        .inputData(data)
                        .build();
            } else {
                job = SimpleAssignSeatsJob.builder()
                        .inputData(data)
                        .build();
            }
            job.call().print(outputWriter);
        } catch (Exception e) {
            System.out.println(ERROR_CANNOT_READ_FILE);
            e.printStackTrace();
        } finally {
            try {
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
