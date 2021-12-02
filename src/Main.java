import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.*;
import java.io.*;

public class Main {
    private static final int NUMBER_OF_SIMULATIONS_TO_RUN = 1;
    private static final int MIN_TTL_IN_DAYS = 1;
    private static final int MAX_TTL_IN_DAYS = 30;
    private static final int HOURS_IN_DAY = 24;
    private static final int MIN_ACCEPTABLE_CACHE_HIT_PERCENTAGE = 45;
    private static final float MIN_ACCEPTABLE_COST_IMPROVEMENT_PERCENTAGE = 0.05f;
    private static final int NUMBER_OF_DAYS_IN_WEEK = 7;
    private static ArrayList<Integer> ttlQueue = new ArrayList<>();
    private static int bestTTL = 0;
    private static double bestCost = Double.MAX_VALUE; //1.7*10^308 - max value a double can represent
    private static ArrayList<Integer> usedTTLs = new ArrayList<>();
    private static HashMap<Integer, Double> ttlCosts = new HashMap<>();
    private static HashMap<Integer, Float> ttlCacheHitRatios = new HashMap<>();
    private static int nameCounter = 0;
    public static void main (String[] args) throws Exception {
        
        bruteForce();
        //qLearning();
        //popularRelease();
    }

    private static void bruteForce() throws Exception {
        long timeSum = 0;
        String csvName = "bruteforce.csv";

        for (int i = 1; i <= MAX_TTL_IN_DAYS; i++)
            for (int j = 0; j < NUMBER_OF_SIMULATIONS_TO_RUN; j++)
            {
                long startTime = System.nanoTime();

                Simulation simulation = new Simulation(i * HOURS_IN_DAY);

                // Simulation will run 30 times
                simulation.run();

                double dblCost = simulation.getCostPer100000Requests();
                float fltCHR = simulation.getCacheHitRatio();

                ttlCosts.put(i, dblCost);
                ttlCacheHitRatios.put(i, fltCHR);
                
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000000;  //divide by 1000000 to get milliseconds.
                timeSum += duration;

                String runInfo = i + "," + dblCost + "," + fltCHR + "," + duration;
                
                System.out.println("runInfo: " + runInfo);
                System.out.println("Start time: " + startTime);
                System.out.println("End time: " + endTime);
                System.out.println("Execution time: " + duration);
                
                CSVWriter(runInfo, csvName);
            }

        addString(csvName);
        
        System.out.println("TTL Costs per 100000 request: $" + ttlCosts);
        System.out.println("TTL Cache Hit Ratios: $" + ttlCacheHitRatios);
        System.out.println("Execution time: " + timeSum);
        System.out.println("End of Brute Force");
        System.out.println("------------------------------------");
    }

    private static void popularRelease() throws Exception {
        int initialTTL = 5;
        double leastCost = bestCost;
        long timeSum = 0;
        long startTime = 0;
        long endTime = 0;
        long duration = 0;
        bestTTL = initialTTL;
        ttlQueue.add(initialTTL);
        double leastCHR = MIN_ACCEPTABLE_CACHE_HIT_PERCENTAGE;
        int dsr = 0;

        String csvName = "popularRelease.csv";

        while (!ttlQueue.isEmpty())
        {
            int  token = 0;
            int ttl = ttlQueue.remove(0);
            
            if (!usedTTLs.contains(ttl)) {
                usedTTLs.add(ttl);
            }

            startTime = System.nanoTime();
            Simulation simulation = new Simulation(ttl * HOURS_IN_DAY);
            simulation.run();

            double dblCost = simulation.getCostPer100000Requests();
            float fltCHR = simulation.getCacheHitRatio();

            // ttl = 5 for first simulation
            ttlCosts.put(ttl, dblCost);
            ttlCacheHitRatios.put(ttl, fltCHR);
            
            dsr = simulation.getRandomDaysSinceRelease();

            if (dblCost <= leastCost) { 
                leastCost = dblCost;
                bestTTL = ttl;
                token += 1;
            }

            if (fltCHR >= leastCHR) {
                leastCHR = fltCHR;
                token += 1;
            }

            if (token > 0) {
                exploreNeighbours(ttl, 1, 1);
                exploreNeighbours(ttl, 8, 8);
                if ((dsr <= ttl+5) && (dsr >= ttl-5)) {
                    exploreNeighbours(ttl, (int)(dsr), (int)(dsr));
                }
            }

            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000000;  //divide by 1000000 to get milliseconds.
            timeSum += duration;
            String runInfo = ttl + "," + dblCost + "," + fltCHR + "," + duration;
            CSVWriter(runInfo, csvName);
        }

        addString(csvName);

        System.out.println("TTL Costs per 100000 request: $" + ttlCosts);
        System.out.println("TTL Cache Hit Ratios: $" + ttlCacheHitRatios);
        System.out.println("---- reached least cost ----");
        System.out.println("Execution time: " + timeSum);
        System.out.println("Least Cost: " + leastCost);
        System.out.println("Best TTL: " + bestTTL);
        System.out.println("------------------------------------");
    }

    private static void exploreNeighbours(int ttl, int ttlUp, int ttlDown) throws Exception {
        if (!usedTTLs.contains(ttl+ttlUp) && ttl+ttlUp <= MAX_TTL_IN_DAYS) {
            ttlQueue.add(ttl+ttlUp);
            usedTTLs.add(ttl+ttlUp);
        }

        if (!usedTTLs.contains(ttl-ttlDown) && ttl-ttlDown >= MIN_TTL_IN_DAYS) {
            ttlQueue.add(ttl-ttlDown);
            usedTTLs.add(ttl-ttlDown);
        }
    }

    private static void qLearning() throws Exception {
        int initialTTL = getRandomIntegerInRange(MIN_TTL_IN_DAYS, MAX_TTL_IN_DAYS);
        long startTime = 0;
        long endTime = 0;
        long duration = 0;
        long timeSum = 0;

        bestTTL = initialTTL;
        ttlQueue.add(initialTTL);

        String csvName = "qLearning.csv";

        while (!ttlQueue.isEmpty()) {
            System.out.println("Curr" + ttlQueue);

            int ttl = ttlQueue.remove(0);

            if (!usedTTLs.contains(ttl)) {
                usedTTLs.add(ttl);
            }

            System.out.println("Used" + usedTTLs);

            startTime = System.nanoTime();

            Simulation simulation = new Simulation(ttl * HOURS_IN_DAY);
            simulation.run();

            double reward;
            double cost = simulation.getCostPer100000Requests();
            float fltCHR = simulation.getCacheHitRatio();

            ttlCosts.put(ttl, cost); // add ttl and cost into hash map
            ttlCacheHitRatios.put(ttl, fltCHR); // add ttl and CHR to hash map

            if (bestCost == Double.MAX_VALUE) {
                reward = 1;
                bestCost = cost;
                bestTTL = ttl;
            }
            //MIN_ACCEPTABLE_CACHE_HIT_PERCENTAGE = 45;
            else if (fltCHR < MIN_ACCEPTABLE_CACHE_HIT_PERCENTAGE)
                // if CHR < 45%
                reward = -1000;
            else if (cost > bestCost)
                // if cost > largest double value
                reward = -10;
                //MIN_ACCEPTABLE_COST_IMPROVEMENT_PERCENTAGE = 0.05f;
            else if (cost > (1-MIN_ACCEPTABLE_COST_IMPROVEMENT_PERCENTAGE) * bestCost)
                reward = 0;
            else {
                reward = bestCost - cost;
                // bestCost becomes the lowest cost
                bestCost = cost;
                bestTTL = ttl;
            }

            if (reward > 0) {
                //cost improvement is less than 0.05%
                explore(ttl, 1, 1);
                
                // explore(size up, size down)
                explore(ttl, NUMBER_OF_DAYS_IN_WEEK, NUMBER_OF_DAYS_IN_WEEK);

                explore(ttl, (int) Math.floor((ttl + MAX_TTL_IN_DAYS) / 2.0), (int) Math.floor((ttl + MIN_TTL_IN_DAYS) / 2.0));
            }

            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000000;
            timeSum += duration;

            String runInfo = ttl + "," + simulation.getCostPer100000Requests() + "," + simulation.getCacheHitRatio() + "," + duration;

            CSVWriter(runInfo, csvName);
        }

        addString(csvName);

        System.out.println("Best TTL in days: " + bestTTL);
        System.out.println("Lowest Cost per 100000 request: $" + String.format("%.2f", bestCost));
        System.out.print("Execution time: " + timeSum);
        System.out.print("End of Q Learning");
        System.out.print("------------------------------------");
    }

    private static void explore(int ttl, int stepSizeUp, int StepSizeDown) {
        if (!usedTTLs.contains(ttl+stepSizeUp) && ttl+stepSizeUp <= MAX_TTL_IN_DAYS) {
            ttlQueue.add(ttl+stepSizeUp);
            usedTTLs.add(ttl+stepSizeUp);
        }

        if (!usedTTLs.contains(ttl-StepSizeDown) && ttl-StepSizeDown >= MIN_TTL_IN_DAYS) {
            ttlQueue.add(ttl-StepSizeDown);
            usedTTLs.add(ttl-StepSizeDown);
        }
    }

    private static int getRandomIntegerInRange(int min, int max) {
        return new Random().nextInt(max + 1 - min) + min;
    }

    private static void CSVWriter(String csvData, String csvName) {
        try {
            FileWriter csvWriter = new FileWriter(csvName, true);
            
            csvWriter.append(csvData);
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addString(String csvName) {
        String csvData = "--------,--------,--------,-------";
                         
        try {
            FileWriter csvWriter = new FileWriter(csvName, true);
            
            csvWriter.append(csvData);
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
