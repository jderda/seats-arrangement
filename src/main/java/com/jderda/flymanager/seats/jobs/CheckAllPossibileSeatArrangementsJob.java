package com.jderda.flymanager.seats.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.jderda.flymanager.seats.domain.input.AbstractGroup;
import com.jderda.flymanager.seats.domain.input.InputData;
import com.jderda.flymanager.seats.domain.input.Passenger;
import com.jderda.flymanager.seats.domain.output.SeatsArrangement;
import com.jderda.flymanager.seats.domain.output.SeatsRow;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * This implementation checks all possible permutations of groups assignment, covers all corner cases.
 * It's very computationally-intensive (with computational complexity of O(m^n) ), but is easy to parallelize,
 * both on local computing unit and on distributed clusters (like EMR). Optimizations are possible (like skipping 
 * groups of possibilities that for sure won't have a solution), but due to time constraints not implemented here. 
 * 
 * In worst-case real-life scenario (800 individual passengers, 80 rows), this requires ~10^1600 individual checks,
 * making it unrealistic.
 */
@Builder
@RequiredArgsConstructor
public class CheckAllPossibileSeatArrangementsJob implements Callable<SeatsArrangement> {

    private final InputData inputData;

    @Override
    public SeatsArrangement call() throws Exception {
        Optional<Combination> bestCombination = Optional.empty(); 
        int bestScore = -1;
        Optional<Combination> nextCombination = Optional.of(new Combination(inputData));
        while (nextCombination.isPresent()) {
            Combination combination = nextCombination.get();
            Integer combinationScore = combination.getScore();
            if (combinationScore > bestScore) {
                bestCombination = nextCombination;
                bestScore = combinationScore;
            }
            nextCombination = combination.getNext();
        }
        return bestCombination.get().getSeatsArrangement();
    }

}

class Combination {
    
    public final static int INVALID_CASE_SCORE = -1;
    
    private final InputData input;
    private final Integer[] arrangement;
    
    @Builder
    public Combination(InputData input) {
        this(input, new Integer[input.getGroups().size()]);
    }
    
    private Combination(InputData input, Integer[] arrangement) {
        super();
        this.input = input;
        this.arrangement = arrangement;
    }

    public Optional<Combination> getNext() {
        Integer[] newArrangement = Arrays.copyOf(arrangement, arrangement.length);
        for (int i = newArrangement.length-1; i>=0; i--) {
            if (newArrangement[i] == null) {
                newArrangement[i] = 0;
                return Optional.of(new Combination(input, newArrangement));
            } else {
                newArrangement[i] = newArrangement[i]+1;
                if (newArrangement[i] < input.getNumberOfRows()) {
                    return Optional.of(new Combination(input, newArrangement));
                } else {
                    newArrangement[i] = null;
                }
            }
        }
        return Optional.empty();
    }
    
    public Integer getScore() {
        int numberOfSatisfiedPassengers = 0;
        for (int rowIndex=0; rowIndex<input.getNumberOfRows(); rowIndex++) {
            List<AbstractGroup> rowGroups = getRowGroups(rowIndex);
            int numberOfPassengers = rowGroups.stream().mapToInt(grp -> grp.getOccupiedSeats()).sum();
            int numberOfWindowPassengers = (int) rowGroups.stream()
                    .flatMap(grp -> grp.getPassengers().stream())
                    .filter(psg -> psg.hasWindowSeatPreference())
                    .count();
            if (numberOfPassengers<=input.getNumberOfSeatsInRow()) {
                numberOfSatisfiedPassengers += numberOfPassengers - Math.max(0, numberOfWindowPassengers-2);
            } else {
                return INVALID_CASE_SCORE;
            }
        }
        return numberOfSatisfiedPassengers;
    }
    
    private List<AbstractGroup> getRowGroups(int rowIndex) {
        List<AbstractGroup> result = new ArrayList<>();
        for (int i=0; i<arrangement.length; i++) {
            if (arrangement[i] != null && arrangement[i] == rowIndex) {
                result.add(input.getGroups().get(i));
            }
        }
        return result;
    }
    
    private List<Passenger> getUnassignedPassengers() {
        List<Passenger> result = new ArrayList<>();
        for (int i=0; i<arrangement.length; i++) {
            if (arrangement[i] == null) {
                result.addAll(input.getGroups().get(i).getPassengers());
            }
        }
        return result;
    }

    public SeatsArrangement getSeatsArrangement() {
        List<SeatsRow> rows = new ArrayList<>();
        LinkedList<Passenger> unassignedPassengers = new LinkedList<>(getUnassignedPassengers());
        for (int rowIndex=0; rowIndex<input.getNumberOfRows(); rowIndex++) {
            List<AbstractGroup> rowGroups = getRowGroups(rowIndex);
            SeatsRow seatsRow = SeatsRow.builder()
                    .capacity(input.getNumberOfSeatsInRow())
                    .groups(rowGroups)
                    .build();
            while (!(seatsRow.isFull() || unassignedPassengers.isEmpty())) {
                seatsRow.add(unassignedPassengers.poll());
            }
            rows.add(seatsRow);
        }
        return SeatsArrangement.builder()
                .rows(rows)
                .build();
    }
    
}