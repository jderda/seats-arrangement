package com.jderda.flymanager.seats.domain.input;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InputData {

    private final List<Group> groups;
    private final Integer numberOfRows;
    private final Integer numberOfSeatsInRow;
    
    public static InputData of(BufferedReader reader) {
        try {
            String planeSizeDef = reader.readLine();
            Matcher matcher = Pattern.compile("([0-9]*) ([0-9]*)").matcher(planeSizeDef);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid plane size definition, expected two digits, got " + planeSizeDef);
            }
            Integer numberOfSeats = Integer.valueOf(matcher.group(1));
            Integer numberOfRows = Integer.valueOf(matcher.group(2));
            
            List<Group> groups = reader.lines()
                .map(InputData::parseGroupLine)
                .collect(Collectors.toList());
            
            return InputData.builder()
                    .groups(groups)
                    .numberOfSeatsInRow(numberOfSeats)
                    .numberOfRows(numberOfRows)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot read input data definition", e);
        }
    }
    
    private static Group parseGroupLine(String line) {
        String[] passengersArray = line.split(" ");
        List<Passenger> passengers = Arrays.stream(passengersArray)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(InputData::parsePassenger)
            .collect(Collectors.toList());
        return Group.builder()
                .passengers(passengers)
                .build();
    }
    
    private static Passenger parsePassenger(String passengerData) {
        Matcher matcher = Pattern.compile("([0-9]*)(W)?").matcher(passengerData);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid passenger details format, expected [0-9]*W?, got " + passengerData);
        }
        Integer id = Integer.valueOf(matcher.group(1));
        Boolean windowSeatPreference = (matcher.group(2) != null);
        return Passenger.builder()
                .id(id)
                .windowSeatPreference(windowSeatPreference)
                .build();
    }
    
    
}
