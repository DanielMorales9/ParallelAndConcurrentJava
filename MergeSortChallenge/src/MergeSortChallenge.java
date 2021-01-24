/**
 * Challenge: Sort an array of random integers with merge sort
 */

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/* sequential implementation of merge sort */
class SequentialMergeSorter {

    private final int[] array;

    public SequentialMergeSorter(int[] array) {
        this.array = array;
    }

    /* returns sorted array */
    public int[] sort() {
        sort(0, array.length-1);
        return array;
    }

    /* helper method that gets called recursively */
    private void sort(int left, int right) {
        if (left < right) {
            int mid = (left+right)/2; // find the middle point
            sort(left, mid); // sort the left half
            sort(mid+1, right); // sort the right half
            MergeHelper.merge(array, left, mid, right); // merge the two sorted halves
        }
    }

}

/* parallel implementation of merge sort */
class ParallelMergeSorter {

    private final int[] array;

    public ParallelMergeSorter(int[] array) {
        this.array = array;
    }

    /* returns sorted array */
    public int[] sort() {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new RecursiveMergeSort(0, array.length - 1));
        pool.shutdown();
        return array;
    }

    class RecursiveMergeSort extends RecursiveAction {

        private final int left;
        private final int right;

        public RecursiveMergeSort(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (left < right) {
                int mid = (left + right) / 2; // find the middle point
                invokeAll(new RecursiveMergeSort(left, mid), new RecursiveMergeSort(mid + 1, right));
                MergeHelper.merge(array, left, mid, right); // merge the two sorted halves
            }
        }
    }
}

public class MergeSortChallenge {

    /* helper function to generate array of random integers */
    public static int[] generateRandomArray(int length) {
        System.out.format("Generating random array int[%d]...\n", length);
        Random rand = new Random();
        int[] output = new int[length];
        for (int i=0; i<length; i++)
            output[i] = rand.nextInt();
        return output;
    }

    /* evaluate performance of sequential and parallel implementations */
    public static void main(String[] args) {
        final int NUM_EVAL_RUNS = 5;
        final int[] input = generateRandomArray(1_000_000);

        System.out.println("Evaluating Sequential Implementation...");
        SequentialMergeSorter sms = new SequentialMergeSorter(Arrays.copyOf(input, input.length));
        int[] sequentialResult = sms.sort();
        double sequentialTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            sms = new SequentialMergeSorter(Arrays.copyOf(input, input.length));
            long start = System.currentTimeMillis();
            sms.sort();
            sequentialTime += System.currentTimeMillis() - start;
        }
        sequentialTime /= NUM_EVAL_RUNS;

        System.out.println("Evaluating Parallel Implementation...");
        ParallelMergeSorter pms = new ParallelMergeSorter(Arrays.copyOf(input, input.length));
        int[] parallelResult = pms.sort();
        double parallelTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            pms = new ParallelMergeSorter(Arrays.copyOf(input, input.length));
            long start = System.currentTimeMillis();
            pms.sort();
            parallelTime += System.currentTimeMillis() - start;
        }
        parallelTime /= NUM_EVAL_RUNS;

        // display sequential and parallel results for comparison
        if (!Arrays.equals(sequentialResult, parallelResult))
            throw new Error("ERROR: sequentialResult and parallelResult do not match!");
        System.out.format("Average Sequential Time: %.1f ms\n", sequentialTime);
        System.out.format("Average Parallel Time: %.1f ms\n", parallelTime);
        System.out.format("Speedup: %.2f \n", sequentialTime/parallelTime);
        System.out.format("Efficiency: %.2f%%\n", 100*(sequentialTime/parallelTime)/Runtime.getRuntime().availableProcessors());
    }
}