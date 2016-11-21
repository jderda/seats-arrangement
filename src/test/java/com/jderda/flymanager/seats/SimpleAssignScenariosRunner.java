package com.jderda.flymanager.seats;

import com.jderda.flymanager.seats.jobs.SimpleAssignSeatsJob;

public class SimpleAssignScenariosRunner extends AbstractInputScenariosRunner {

    public SimpleAssignScenariosRunner(String testName, String testData) {
        super(testName, testData);
    }

    @Override
    public void init() {
        job = SimpleAssignSeatsJob.builder()
                .inputData(inputData)
                .build();
    }

}
