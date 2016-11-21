package com.jderda.flymanager.seats.jobs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.jderda.flymanager.seats.domain.input.AbstractGroup;
import com.jderda.flymanager.seats.domain.input.Group;
import com.jderda.flymanager.seats.domain.input.InputData;
import com.jderda.flymanager.seats.domain.input.Passenger;
import com.jderda.flymanager.seats.domain.output.SeatsArrangement;
import com.jderda.flymanager.seats.domain.output.SeatsRow;

import lombok.Builder;
import lombok.NonNull;

/**
 * This is the simplest, naive implementation of seat assignment - similar to scheduling problem. 
 * In this case we're sorting the input groups by the number of passengers (descending) and try to fit 
 * them in any space that is left, if possible.
 *
 */
public class SimpleAssignSeatsJob implements Callable<SeatsArrangement> {
    
    @NonNull
    private final InputData inputData;
    
    List<SeatsRow> rows;
    List<Passenger> passengersNotFitting;

    @Override
    public SeatsArrangement call() throws Exception {
        assignGroupsToRows();
        
        assignRemainingPassengers();
        
        return SeatsArrangement.builder().rows(rows).build();
    }

    
    private void assignRemainingPassengers() {
        //as those passengers won't be satisfied anyway, the order or seat placement doesn't matter
        passengersNotFitting.forEach(this::tryAssignPassengerToAnyRow);
        
    }
    
    private void assignGroupsToRows() {
        List<Group> groups = inputData.getGroups();
        groups.sort(new Comparator<Group>() {

            @Override
            public int compare(Group group1, Group group2) {
                Double firstScore = getGroupScore(group1);
                Double secondScore = getGroupScore(group2);
                return -firstScore.compareTo(secondScore);
            }

            private Double getGroupScore(Group group) {
                Double score = group.getMaximumSatisfaction().doubleValue();
                Long numberOfWindowPreferences = group.getPassengers().stream()
                    .filter(passenger -> passenger.hasWindowSeatPreference())
                    .count();
                return score - 0.25 * numberOfWindowPreferences;
            }
        });
        groups.forEach(this::tryAssignGroupToAnyRow);
    }

    private void tryAssignPassengerToAnyRow(Passenger passenger) {
        for (SeatsRow row : rows) {
            if (!row.isFull()) {
                row.add(passenger);
                return;
            }
        }
    }
    
    private void tryAssignGroupToAnyRow(AbstractGroup group) {
        Optional<SeatsRow> maximalSatisfactionRow = Optional.empty();
        Optional<Integer> maximalSatisfaction = Optional.empty();
        for (SeatsRow row : rows) {
            Optional<Integer> potentialSatisfaction = row.getPotentialGroupSatisfaction(group);
            if (potentialSatisfaction.isPresent()) { 
                 if (!maximalSatisfactionRow.isPresent()
                         || potentialSatisfaction.get() > maximalSatisfaction.get()) {
                     maximalSatisfaction = potentialSatisfaction;
                     maximalSatisfactionRow = Optional.of(row);
                 }
            }
        }
        
        if (maximalSatisfactionRow.isPresent()) {
            maximalSatisfactionRow.get().add(group);
        } else {
            passengersNotFitting.addAll(group.getPassengers());
        }
    }

    private List<SeatsRow> createEmpyRows() {
        List<SeatsRow> rows = new ArrayList<>();
        for (int i=0; i<inputData.getNumberOfRows(); i++) {
            rows.add(new SeatsRow(inputData.getNumberOfSeatsInRow()));
        }
        return rows;
    }

    @Builder
    public SimpleAssignSeatsJob(InputData inputData) {
        super();
        this.inputData = inputData;
        rows = createEmpyRows();
        passengersNotFitting = new ArrayList<>();
    }
    
}
