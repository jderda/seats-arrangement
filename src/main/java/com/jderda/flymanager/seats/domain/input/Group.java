package com.jderda.flymanager.seats.domain.input;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Group implements AbstractGroup, Comparable<Group> {

    private final List<Passenger> passengers;

    @Override
    public int compareTo(Group otherGroup) {
        return this.getOccupiedSeats().compareTo(otherGroup.getOccupiedSeats());
    }

    @Override
    public Integer getMaximumSatisfaction() {
        return passengers.size();
    }

    @Override
    public void print(Writer writer) throws IOException {
        for (int i=0; i<passengers.size(); i++) {
            if (i!=0) {
                writer.append(' ');
            }
            passengers.get(i).print(writer);
        }
    }

    @Override
    public Integer getOccupiedSeats() {
        return passengers.size();
    }
    
    public boolean hasWindowSeatPreferencePassengers() {
        return passengers.stream().anyMatch(Passenger::hasWindowSeatPreference);
    }

}
