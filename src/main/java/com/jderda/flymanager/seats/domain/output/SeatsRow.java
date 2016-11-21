package com.jderda.flymanager.seats.domain.output;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jderda.flymanager.seats.domain.input.AbstractGroup;
import com.jderda.flymanager.seats.domain.input.Passenger;

import lombok.Builder;
import lombok.Data;

@Data
public class SeatsRow implements AbstractGroup {

    private final Integer capacity;
    
    private final List<AbstractGroup> groups;

    public Optional<Integer> getPotentialGroupSatisfaction(AbstractGroup group) {
        Integer currentCapacityUsed = getOccupiedSeats();
        if (group.getOccupiedSeats() > capacity-currentCapacityUsed) {
            return Optional.empty();
        }
        
        Integer potentialSatisfaction = group.getMaximumSatisfaction();
        
        //check for the case that there are too many passengers wanting window seat in that row
        Integer numberOfAlreadBookedWindowSeats = (int) groups.stream()
                .flatMap(grp -> grp.getPassengers().stream())
                .filter(passenger -> passenger.hasWindowSeatPreference())
                .count();
        Integer numberOfWantedWindowSeats = (int) group.getPassengers().stream()
                .filter(passenger -> passenger.hasWindowSeatPreference())
                .count();
        Integer numberOfWindowSeatsLeft = Math.max(0, 2-numberOfAlreadBookedWindowSeats);
        Integer numberOfDissatisfiedPassengers = Math.min(0, numberOfWantedWindowSeats-numberOfWindowSeatsLeft);
        potentialSatisfaction = potentialSatisfaction-numberOfDissatisfiedPassengers;
        return Optional.of(potentialSatisfaction);
    }

    @Override
    public Integer getOccupiedSeats() {
        return groups.stream().mapToInt(grp -> grp.getOccupiedSeats()).sum();
    }

    public void add(AbstractGroup group) {
        groups.add(group);
    }

    public boolean isFull() {
        return getOccupiedSeats() == this.capacity;
    }
    
    public Integer getNumberOfSatisfiedPassengers() {
        int totalSatisfaction = 0;
        for (AbstractGroup group : groups) {
            if (!(group instanceof Passenger)) {
                totalSatisfaction += group.getMaximumSatisfaction();
            }
        }
        int windowPreferringPassengers = (int)groups.stream()
            .flatMap(group -> group.getPassengers().stream())
            .filter(Passenger::hasWindowSeatPreference)
            .count();
        return totalSatisfaction-Math.max(0, windowPreferringPassengers-2);
    }

    @Override
    public List<Passenger> getPassengers() {
        List<Passenger> orderedList = new ArrayList<>();
        List<AbstractGroup> windowGroups = this.groups.stream()
                .filter(AbstractGroup::hasWindowSeatPreferencePassengers)
                .collect(Collectors.toList());

        List<AbstractGroup> nonWindowGroups = this.groups.stream()
                .filter(group -> !group.hasWindowSeatPreferencePassengers())
                .collect(Collectors.toList());
        if (!windowGroups.isEmpty()) {
            Optional<Passenger> firstWindowPassenger = windowGroups.get(0).getPassengers().stream()
                    .filter(Passenger::hasWindowSeatPreference)
                    .findAny();
            if (firstWindowPassenger.isPresent()) {
                orderedList.add(firstWindowPassenger.get());
            }
            windowGroups.remove(0).getPassengers().stream()
                .filter(passenger -> (!firstWindowPassenger.isPresent()) || (passenger != firstWindowPassenger.get()))
                .forEach(orderedList::add);
        }
        nonWindowGroups.stream()
            .forEach(group -> group.getPassengers().stream().forEach(orderedList::add));
        if (!windowGroups.isEmpty()) {
            Optional<Passenger> lastWindowPassenger = windowGroups.get(windowGroups.size()-1).getPassengers().stream()
                    .filter(Passenger::hasWindowSeatPreference)
                    .findAny();
            windowGroups.stream()
                .forEach(
                        group -> group.getPassengers().stream()
                            .filter(passenger -> (!lastWindowPassenger.isPresent()) || (passenger != lastWindowPassenger.get()))
                            .forEach(orderedList::add)
                        );
            if (lastWindowPassenger.isPresent()) {
                orderedList.add(lastWindowPassenger.get());
            }
        }
        return orderedList;
    }

    public void print(Writer writer) throws IOException {
        List<Passenger> passengers = this.getPassengers();
        for (int i=0; i<passengers.size(); i++) {
            if (i != 0) {
                writer.append(' ');
            }
            passengers.get(i).print(writer);
        }
        writer.append('\n');
    }

    @Override
    public boolean hasWindowSeatPreferencePassengers() {
        return groups.stream().anyMatch(group -> group.hasWindowSeatPreferencePassengers());
    }

    @Builder
    public SeatsRow(Integer capacity, List<AbstractGroup> groups) {
        super();
        this.capacity = capacity;
        this.groups = groups;
    }

    public SeatsRow(Integer capacity) {
        this(capacity, new ArrayList<>());
    }

}
