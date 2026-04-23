package fyp;

/**
 * BinarySearch
 *
 * Implements the Binary Search algorithm.
 * Repeatedly divides the search space in half by comparing the target
 * with the middle element, eliminating half the remaining elements each step.
 *
 * Time Complexity:
 *   Best case:    O(1)      - target is the middle element
 *   Average case: O(log n)  - target found after several halvings
 *   Worst case:   O(log n)  - target not present
 *
 * Space Complexity: O(1) - iterative implementation, no recursion stack
 *
 * Requirement: array MUST be sorted in ascending order
 */
public class BinarySearch {

    /**
     * Searches for a target value using binary search.
     *
     * @param data   the sorted array to search
     * @param target the value to find
     * @return index of target if found, -1 if not found
     */
    public static int search(int[] data, int target) {
        int low = 0;
        int high = data.length - 1;

        while (low <= high) {
            // Calculate midpoint (avoids integer overflow vs (low+high)/2)
            int mid = low + (high - low) / 2;

            if (data[mid] == target) {
                return mid; // Found at midpoint
            } else if (data[mid] < target) {
                low = mid + 1; // Target is in right half
            } else {
                high = mid - 1; // Target is in left half
            }
        }

        return -1; // Not found
    }
}
