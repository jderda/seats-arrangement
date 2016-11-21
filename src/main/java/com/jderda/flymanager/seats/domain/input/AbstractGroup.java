package com.jderda.flymanager.seats.domain.input;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public interface AbstractGroup {
    
    public default Integer getMaximumSatisfaction() {
        return getOccupiedSeats();
    }
    
    public abstract void print(Writer writer) throws IOException;
    public abstract Integer getOccupiedSeats();
    public abstract List<Passenger> getPassengers();
    public abstract boolean hasWindowSeatPreferencePassengers();

}
