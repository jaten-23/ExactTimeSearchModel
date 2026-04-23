package fyp;

/**
 * InterpolationSearch
 *
 * Implements the Interpolation Search algorithm.
 * Estimates the likely position of the target using a formula based on
 * the value distribution, similar to how a human searches a phone book.
 *
 * Time Complexity:
 *   Best case:    O(1)        - target found at estimated position immediately
 *   Average case: O(log log n) - for uniformly distributed data
 *   Worst case:   O(n)        - for highly skewed/non-uniform data
 *
 * Space Complexity: O(1) - no extra memory used
 *
 * Requirement: array MUST be sorted in ascending order
 * Best for:    uniformly distributed datasets (e.g. property prices)
 */
public class InterpolationSearch {

    /**
     * Searches for a target value using interpolation search.
     * Uses the interpolation formula to estimate position:
     * pos = low + ((target - data[low]) * (high - low)) / (data[high] - data[low])
     *
     * @param data   the sorted array to search
     * @param target the value to find
     * @return index of target if found, -1 if not found
     */
    public static int search(int[] data, int target) {
        int low = 0;
        int high = data.length - 1;

        while (low <= high && target >= data[low] && target <= data[high]) {

            // Avoid division by zero if all remaining elements are equal
            if (data[high] == data[low]) {
                if (data[low] == target) return low;
                return -1;
            }

            // Interpolation formula: estimate where target likely sits
            // Cast to long to prevent integer overflow on large datasets
            int pos = low + (int)(((long)(target - data[low]) * (high - low))
                          / (data[high] - data[low]));

            // Clamp pos within valid bounds (safety check)
            if (pos < low)  pos = low;
            if (pos > high) pos = high;

            if (data[pos] == target) {
                return pos; // Found at estimated position
            } else if (data[pos] < target) {
                low = pos + 1; // Target is in upper portion
            } else {
                high = pos - 1; // Target is in lower portion
            }
        }

        return -1; // Not found
    }
}
