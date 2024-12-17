import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class ParallelSumFixed {
    // Default number of threads if none provided
    private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int ARRAY_SIZE = 10_000_000; // Large array for meaningful parallelization
    private static final int TRIALS = 5; // Run multiple times for stable average results

    public static void main(String[] args) throws Exception {
        // Determine number of threads from command line argument if provided
        int numThreads = 2;
        if (args.length > 0) {
            try {
                numThreads = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of threads. Using default: " + DEFAULT_THREADS);
                numThreads = DEFAULT_THREADS;
            }
        }
        
        System.out.println("Using " + numThreads + " threads.");

        // Generate a large array of random integers
        int[] data = new int[ARRAY_SIZE];
        Random rand = new Random();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            data[i] = rand.nextInt(100); 
        }

        // Warm-up (optional) to allow JVM JIT optimizations
        for (int i = 0; i < 3; i++) {
            parallelSum(data, numThreads);
        }

        // Baseline single-threaded timing
        long singleThreadTimeSum = 0;
        long singleThreadSum = 0;
        for (int i = 0; i < TRIALS; i++) {
            long start = System.nanoTime();
            singleThreadSum = singleThreadSum(data);
            long end = System.nanoTime();
            singleThreadTimeSum += (end - start);
        }
        long avgSingleThreadTime = singleThreadTimeSum / TRIALS;
        
        // Parallel timing
        long parallelTimeSum = 0;
        long parallelSum = 0;
        for (int i = 0; i < TRIALS; i++) {
            long start = System.nanoTime();
            parallelSum = parallelSum(data, numThreads);
            long end = System.nanoTime();
            parallelTimeSum += (end - start);
        }
        long avgParallelTime = parallelTimeSum / TRIALS;

        // Print results
        System.out.println("Array size: " + ARRAY_SIZE);
        System.out.println("Single-thread sum: " + singleThreadSum + ", avg time: " + (avgSingleThreadTime / 1_000_000.0) + " ms");
        System.out.println("Parallel sum: " + parallelSum + ", avg time: " + (avgParallelTime / 1_000_000.0) + " ms");
    }

    // Simple single-threaded sum
    private static long singleThreadSum(int[] data) {
        long sum = 0;
        for (int val : data) {
            sum += val;
        }
        return sum;
    }

    // Parallel sum using ExecutorService
    private static long parallelSum(int[] data, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int chunkSize = (int) Math.ceil((double) data.length / numThreads);
        Future<Long>[] futures = new Future[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(start + chunkSize, data.length);

            futures[i] = executor.submit(() -> {
                long localSum = 0;
                for (int idx = start; idx < end; idx++) {
                    localSum += data[idx];
                }
                return localSum;
            });
        }

        long totalSum = 0;
        for (Future<Long> f : futures) {
            totalSum += f.get();
        }

        executor.shutdown();
        return totalSum;
    }
}
