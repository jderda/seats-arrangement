package com.jderda.flymanager.seats.domain.input;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class Passenger implements AbstractGroup {
    
    private final Integer id;
    
    @Getter(AccessLevel.NONE)
    private final Boolean windowSeatPreference;
    
    @Override
    public Integer getMaximumSatisfaction() {
        return 1;
    }
    
    @Override
    public void print(Writer writer) throws IOException {
        writer.append(id.toString());
    }

    @Override
    public Integer getOccupiedSeats() {
        return 1;
    }

    @Override
    public List<Passenger> getPassengers() {
        return Collections.singletonList(this);
    }
    
    public Boolean hasWindowSeatPreference() {
        return windowSeatPreference;
    }

    @Override
    public boolean hasWindowSeatPreferencePassengers() {
        return hasWindowSeatPreference();
    }
}
