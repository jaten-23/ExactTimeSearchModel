package fyp;

/**
 * LinearSearch
 *
 * Implements the Linear Search algorithm.
 * Sequentially scans each element from index 0 until the target is found
 * or the end of the array is reached.
 *
 * Time Complexity:
 *   Best case:    O(1)  - target is the first element
 *   Average case: O(n)  - target is in the middle
 *   Worst case:   O(n)  - target is last or not present
 *
 * Space Complexity: O(1) - no extra memory used
 *
 * Works on: sorted OR unsorted arrays
 */
public class LinearSearch {

    /**
     * Searches for a target value in an integer array using linear search.
     *
     * @param data   the array to search (sorted or unsorted)
     * @param target the value to find
     * @return index of target if found, -1 if not found
     */
    public static int search(int[] data, int target) {
        // Scan every element from the beginning
        for (int i = 0; i < data.length; i++) {
            if (data[i] == target) {
                return i; // Found — return position
            }
        }
        return -1; // Not found
    }
}
