import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelSum {

    // Change this number to test different parallelism levels
    private static final int NUM_THREADS = 4; // try 1, 2, 4, 8, etc.

    // Size of the array
    private static final int ARRAY_SIZE = 100_000;

    public static void main(String[] args) throws Exception {
        // Generate a large array of random integers
        int[] data = new int[ARRAY_SIZE];
        Random rand = new Random();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            data[i] = rand.nextInt(100); // values between 0 and 99
        }

        // Warm up the JVM (optional)
        // This can help get more stable timing measurements if running multiple times.
        for (int i = 0; i < 3; i++) {
            parallelSum(data, NUM_THREADS); 
        }

        // Now do an official timed run
        long startTime = System.nanoTime();
        long sum = parallelSum(data, NUM_THREADS);
        long endTime = System.nanoTime();

        double durationMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("Number of threads: " + NUM_THREADS);
        System.out.println("Sum of array: " + sum);
        System.out.println("Time taken: " + durationMs + " ms");
    }

    /**
     * Sums an array in parallel using the given number of threads.
     * @param data the array to sum
     * @param numThreads the number of threads to use
     * @return the sum of all elements in data
     * @throws Exception if any thread operation fails
     */
    public static long parallelSum(int[] data, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Determine chunk sizes
        int chunkSize = (int) Math.ceil((double)data.length / numThreads);

        Future<Long>[] futures = new Future[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(start + chunkSize, data.length);

            Callable<Long> task = () -> {
                long localSum = 0;
                for (int idx = start; idx < end; idx++) {
                    localSum += data[idx];
                }
                return localSum;
            };

            futures[i] = executor.submit(task);
        }

        long totalSum = 0;
        for (Future<Long> f : futures) {
            totalSum += f.get();
        }

        executor.shutdown();
        return totalSum;
    }
}
