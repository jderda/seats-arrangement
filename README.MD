Flight passengers manager
=========================

Assumptions made
----------------
I've made several assumptions during implementation:
1. I was not sure about what this sentence meant: 'If the plane is over subscribed the program should aim to maximize customer satisfaction of those customers waiting for the flight' - I assumed that out of all passengers waiting for flight, we're aiming to select a group for which the satisfaction will be maximized and that we're calculating satisfactions only for passengers that got onboard (eg. those that did not fit are not taken into account)
2. that all rows are of the same width and each row contains exactly two window on opposite sides.
3. one made to simplify the seat arrangements algorithm - the assumption is that in any group there is at most one passenger who would like to sit by the window.
4. that we don't want the seats to be empty (otherwise we could onboard only groups that will be satisfied and leave the rest of seats unoccupied, achieving 100% score)  

Running application
-------------------
There are two algorithms implemented (see below for description). To run simplified one (safe for larger inputs), simply execute `java -jar solution.jar /path/to/input/data.txt`  (or, if run in IDE, execute `com.jderda.flymanager.seats.ConsoleApplication` class with single arguments `/path/to/input/data.txt`).
This algorithm however fail to provide optimal solution in some cases (see below and test data for details). To run the algorithm that always return proper result (but has enormous complexity, thus is not suitable for larger inputs) execute `java -jar solution.jar /path/to/input/data.txt -all`  (or, if run in IDE, execute `com.jderda.flymanager.seats.ConsoleApplication` class with single arguments `/path/to/input/data.txt` and `-all`)

Proposed solution
-----------------
As we have clearly defined 'satisfaction' criteria, they simplify the problem - the main condition to customer satisfaction is that the group remains non divided between different rows. Secondly, the satisfaction for given customer is binary (customer is with entire group and (optionally) has a window seat). Therefore we are creating new groups by combining existing ones to achieve the maximum potential satisfaction factor in any given group, and then assign those groups to rows in order (sorted by satisfaction of entire row, descending order). As a last step we fill the remaining places with other passengers.

Implementation details
----------------------
I implemented two algorithms - one that is simplified and runs on O(n) time giving proper solution most of the times and one that checks entire solutions domain that runs in O(m^n) time. 
The first algorithm uses approach similar to solution of scheduling problem, treating rows as resources and number of people in each group as the 'length' of job (with additional constraints around window seats), however it fails to provide the proper answer for some edge cases (see the 'edgeCase' test scenario).
The second algorithm will always return proper answer, however the implementation is very sup-optimal performance-wise, but easily yields to parallelization (also on distributed environments) and the actual run time can be greatly reduced by failing fast in some cases (namely: situations where single row is already filled for example).

I put some time into creating proper domain classes - both algorithms could be implemented using arrays, primitive structures etc, but approach that I've chosen allows easier implementation of other algorithms and reuse of some portions of code. 

Things to improve
-----------------
There are several key things that could be improved, namely:
* Tests - currently tests are created only to show that it works, proper tests should validate outputs and unit-test individual components/classes
* Error handling - currently input is not validated, and application will fail unexpectedly with invalid input
* Logging - to be able to debug and trace the application decisions, a proper logging in methods should be implemented
* Thread safety - current algorithm implementations are stateful and not reusable in multi-threaded environment; it's possible to make them stateless and thread-safe, which might be necessary for usage in production systems
* Optimizations - algorithms are sub-optimal (both in terms of performance and results), and can be optimized given more time
* Approximate algorithms - in real life, most likely we'll be aiming at 'good enough' solution, where we can use simulated annealing or genetic algorithms to find a solution that suits us best in reasonable time (this was however against the task description)

Original task
-------------
You run an Airline that has several planes that fly to different destinations around the world. You pride yourself on having a high customer satisfaction with those that fly with you. This you achieve by ensuring that:
 * Groups of travellers are seated together on the same row.
 * Providing travellers with a window seat if request.

To determine the best sitting arrangements on the flight create a program that takes an input file as a command line argument and prints the results to standard out. An example input file is:

```
4 4
1W 2 3 
4 5 6 7
8
9 10 11W
12W
13 14
15 16
```

The first line specifies the dimensions of the plane. The first digit is the number of seats in a row and the second digit is the number of rows on the plane.

Each subsequent line describes a group of travellers. For example, the first line of travelers describes a group of three where the first traveller has a preference for a window seat. Each number uniquely identifies the traveller on the flight.
The output for the above file should be:

```
1 2 3 8
4 5 6 7
11 9 10 12
13 14 15 16
100%
```

The program should aim to maximize customer satisfaction. The last line in the above output indicates the percentage of customers that have had their preferences satisfied. If the plane is over subscribed the program should aim to maximize customer satisfaction of those customers waiting for the flight.
When you are submitting your program please provide a brief description of the approach in a README.txt file and zipped folder that includes a buildable project with the source code and appropriate tests.
