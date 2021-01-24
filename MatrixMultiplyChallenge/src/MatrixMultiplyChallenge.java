/**
 * Challenge: Multiply two matrices
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/* sequential implementation of matrix multiplication */
class SequentialMatrixMultiplier {

    private int[][] A, B;
    private int numRowsA, numColsA, numRowsB, numColsB;

    public SequentialMatrixMultiplier(int[][] A, int[][] B) {
        this.A = A;
        this.B = B;
        this.numRowsA = A.length;
        this.numColsA = A[0].length;
        this.numRowsB = B.length;
        this.numColsB = B[0].length;
        if (numColsA != numRowsB)
            throw new Error(String.format("Invalid dimensions; Cannot multiply %dx%d*%dx%d\n", numRowsA, numRowsB, numColsA, numColsB));
    }

    /* returns matrix product C = AB */
    public int[][] computeProduct() {
        int[][] C = new int[numRowsA][numColsB];
        for (int i=0; i<numRowsA; i++) {
            for (int k=0; k<numColsB; k++) {
                int sum = 0;
                for (int j=0; j<numColsA; j++) {
                    sum += A[i][j] * B[j][k];
                }
                C[i][k] = sum;
            }
        }
        return C;
    }
}

class RowColumnProduct implements Runnable {

    private final int row;
    private final int[][] A;
    private final int[][] B;
    private int[][] C;

    public RowColumnProduct(int i, int[][] a, int[][] b, int[][] c) {
        this.row = i;
        this.A = a;
        this.B = b;
        this.C = c;
    }

    @Override
    public void run() {
        int length = this.A[this.row].length;
        for(int j = 0; j < length; j++) {
            int _sum = 0;
            for (int i = 0; i < length; i ++) {
                _sum += this.A[this.row][i] * this.B[i][j];
            }
            this.C[this.row][j] = _sum;
        }
    }
}

/* parallel implementation of matrix multiplication */
class ParallelMatrixMultiplier {

    private final int[][] A, B;
    private final int numRowsA, numColsA, numRowsB, numColsB;

    public ParallelMatrixMultiplier(int[][] A, int[][] B) {
        this.A = A;
        this.B = B;
        this.numRowsA = A.length;
        this.numColsA = A[0].length;
        this.numRowsB = B.length;
        this.numColsB = B[0].length;
        if (numColsA != numRowsB)
            throw new Error(String.format("Invalid dimensions; Cannot multiply %dx%d*%dx%d\n", numRowsA, numRowsB, numColsA, numColsB));
    }

    /* returns matrix product C = AB */
    public int[][] computeProduct() {
        int numWorkers = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(numWorkers);

        int[][] C = new int[numRowsA][numColsB];
        for(int i = 0; i < numRowsA; i++) {
            es.submit(new RowColumnProduct(i, A, B, C));
        }
        es.shutdown();
        try {
            es.awaitTermination(1L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return C;
    }
}

public class MatrixMultiplyChallenge {

    /* helper function to generate MxN matrix of random integers */
    public static int[][] generateRandomMatrix(int M, int N) {
        System.out.format("Generating random %d x %d matrix...\n", M, N);
        Random rand = new Random();
        int[][] output = new int[M][N];
        for (int i=0; i<M; i++)
            for (int j=0; j<N; j++)
                output[i][j] = rand.nextInt(100);
        return output;
    }

    /* evaluate performance of sequential and parallel implementations */
    public static void main(String args[]) {
        final int NUM_EVAL_RUNS = 5;
        int size = 1000;
        final int[][] A = generateRandomMatrix(size,size);
        final int[][] B = generateRandomMatrix(size,size);

        System.out.println("Evaluating Sequential Implementation...");
        SequentialMatrixMultiplier smm = new SequentialMatrixMultiplier(A,B);
        int[][] sequentialResult = smm.computeProduct();
        double sequentialTime = 0;

        for (int i=0; i<NUM_EVAL_RUNS; i++) {
            System.out.println("Eval " + i);
            long start = System.currentTimeMillis();
            smm.computeProduct();
            sequentialTime += System.currentTimeMillis() - start;
        }
        sequentialTime /= NUM_EVAL_RUNS;

        System.out.println("Evaluating Parallel Implementation...");
        ParallelMatrixMultiplier pmm = new ParallelMatrixMultiplier(A,B);
        int[][] parallelResult = pmm.computeProduct();
        double parallelTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            System.out.println("Eval " + i);
            long start = System.currentTimeMillis();
            pmm.computeProduct();
            parallelTime += System.currentTimeMillis() - start;
        }
        parallelTime /= NUM_EVAL_RUNS;

        // display sequential and parallel results for comparison
        if (!Arrays.deepEquals(sequentialResult, parallelResult))
            throw new Error("ERROR: sequentialResult and parallelResult do not match!");
        System.out.format("Average Sequential Time: %.1f ms\n", sequentialTime);
        System.out.format("Average Parallel Time: %.1f ms\n", parallelTime);
        System.out.format("Speedup: %.2f \n", sequentialTime/parallelTime);
        System.out.format("Efficiency: %.2f%%\n", 100*(sequentialTime/parallelTime)/Runtime.getRuntime().availableProcessors());
    }
}