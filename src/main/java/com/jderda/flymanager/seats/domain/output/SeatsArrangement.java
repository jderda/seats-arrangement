package com.jderda.flymanager.seats.domain.output;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatsArrangement {

    private final List<SeatsRow> rows;
    
    public void print(Writer writer) throws IOException {
        for (SeatsRow row : rows) {
            row.print(writer);
        }
        writer.append(String.format("%.0f%%\n", getPercentageOfSatisfiedPassengers()));
    }

    public double getPercentageOfSatisfiedPassengers() {
        Integer totalNumberOfPassengers = getNumberOfPassengersOnboard();
        Integer totalNumberOfSatisfiedPassengers = getNumberOfSatisfiedPassengers();
        return 100.0*totalNumberOfSatisfiedPassengers/totalNumberOfPassengers;
    }

    public int getNumberOfSatisfiedPassengers() {
        return rows.stream().mapToInt(SeatsRow::getNumberOfSatisfiedPassengers).sum();
    }

    public int getNumberOfPassengersOnboard() {
        return rows.stream().mapToInt(SeatsRow::getOccupiedSeats).sum();
    }
    
}
