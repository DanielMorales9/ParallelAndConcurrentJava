import java.util.Arrays;

public class MergeHelper {
    /* helper method to merge two sorted subarrays array[l..m] and array[m+1..r] into array */
    public static void merge(int[] array, int left, int mid, int right) {
        // copy data to temp subarrays to be merged
        int leftTempArray[] = Arrays.copyOfRange(array, left, mid + 1);
        int rightTempArray[] = Arrays.copyOfRange(array, mid + 1, right + 1);

        // initial indexes for left, right, and merged subarrays
        int leftTempIndex = 0, rightTempIndex = 0, mergeIndex = left;

        // merge temp arrays into original
        while (leftTempIndex < mid - left + 1 || rightTempIndex < right - mid) {
            if (leftTempIndex < mid - left + 1 && rightTempIndex < right - mid) {
                if (leftTempArray[leftTempIndex] <= rightTempArray[rightTempIndex]) {
                    array[mergeIndex] = leftTempArray[leftTempIndex];
                    leftTempIndex++;
                } else {
                    array[mergeIndex] = rightTempArray[rightTempIndex];
                    rightTempIndex++;
                }
            } else if (leftTempIndex < mid - left + 1) { // copy any remaining on left side
                array[mergeIndex] = leftTempArray[leftTempIndex];
                leftTempIndex++;
            } else if (rightTempIndex < right - mid) { // copy any remaining on right side
                array[mergeIndex] = rightTempArray[rightTempIndex];
                rightTempIndex++;
            }
            mergeIndex++;
        }
    }
}
