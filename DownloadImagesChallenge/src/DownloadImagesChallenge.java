/**
 * Challenge: Download a collection of images
 */

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* sequential implementation of multiple image downloader */
class SequentialImageDownloader {

    private int[] imageNumbers;

    public SequentialImageDownloader(int[] imageNumbers) {
        this.imageNumbers = imageNumbers;
    }

    /* returns total bytes from downloading all images in imageNumbers array */
    public int downloadAll() {
        int totalBytes = 0;
        for (int num : imageNumbers)
            totalBytes += HelperDowload.downloadImage(num);
        return totalBytes;
    }

}

/* parallel implementation of multiple image downloader */
class ParallelImageDownloader {

    private final int[] imageNumbers;

    public ParallelImageDownloader(int[] imageNumbers) {
        this.imageNumbers = imageNumbers;
    }

    /* returns total bytes from downloading all images in imageNumbers array */
    public int downloadAll() {
        int numWorkers = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(numWorkers);
        return pool.invoke(new ParallelWorker(0,imageNumbers.length-1));
    }

    private class ParallelWorker extends RecursiveTask<Integer> {

        private final int left;
        private final int right;

        public ParallelWorker(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        protected Integer compute() {
            if (left < right) {
                int mid = (left + right) / 2; // find the middle point
                ParallelWorker leftWorker = new ParallelWorker(left, mid);
                ParallelWorker rightWorker = new ParallelWorker(mid+1, right);
                leftWorker.fork();
                return rightWorker.compute() + leftWorker.join();
            }
            return HelperDowload.downloadImage(imageNumbers[left]);
        }
    }
}

public class DownloadImagesChallenge {

    /* evaluate performance of sequential and parallel implementations */
    public static void main(String[] args) {
        final int NUM_EVAL_RUNS = 3;
        final int[] IMAGE_NUMS = IntStream.rangeClosed(1,50).toArray(); // images to download

        System.out.println("Evaluating Sequential Implementation...");
        SequentialImageDownloader sid = new SequentialImageDownloader(IMAGE_NUMS);
        int sequentialResult = sid.downloadAll();
        double sequentialTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            long start = System.currentTimeMillis();
            sid.downloadAll();
            sequentialTime += System.currentTimeMillis() - start;
        }
        sequentialTime /= NUM_EVAL_RUNS;

        System.out.println("Evaluating Parallel Implementation...");
        ParallelImageDownloader pid = new ParallelImageDownloader(IMAGE_NUMS);
        int parallelResult = pid.downloadAll();
        double parallelTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            long start = System.currentTimeMillis();
            pid.downloadAll();
            parallelTime += System.currentTimeMillis() - start;
        }
        parallelTime /= NUM_EVAL_RUNS;

        // display sequential and parallel results for comparison
        if (sequentialResult != parallelResult)
            throw new Error("ERROR: sequentialResult and parallelResult do not match!");
        System.out.format("Downloaded %d images totaling %.1f MB\n", IMAGE_NUMS.length, sequentialResult/1e6);
        System.out.format("Average Sequential Time: %.1f ms\n", sequentialTime);
        System.out.format("Average Parallel Time: %.1f ms\n", parallelTime);
        System.out.format("Speedup: %.2f \n", sequentialTime/parallelTime);
        System.out.format("Efficiency: %.2f%%\n", 100*(sequentialTime/parallelTime)/Runtime.getRuntime().availableProcessors());
    }
}