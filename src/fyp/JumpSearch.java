package fyp;

/**
 * JumpSearch
 *
 * Implements the Jump Search algorithm.
 * Skips ahead by a fixed block size (sqrt(n)) to find a block where
 * the target might exist, then performs a linear scan within that block.
 *
 * Time Complexity:
 *   Best case:    O(1)        - target is the first element
 *   Average case: O(sqrt n)   - target found after block jumps + linear scan
 *   Worst case:   O(sqrt n)   - last block searched
 *
 * Space Complexity: O(1) - no extra memory used
 *
 * Requirement: array MUST be sorted in ascending order
 */
public class JumpSearch {

    /**
     * Searches for a target value using jump search.
     *
     * @param data   the sorted array to search
     * @param target the value to find
     * @return index of target if found, -1 if not found
     */
    public static int search(int[] data, int target) {
        int n = data.length;

        // Optimal block size is square root of array length
        int step = (int) Math.sqrt(n);
        int prev = 0;

        // Jump ahead by 'step' until we overshoot the target or reach end
        while (data[Math.min(step, n) - 1] < target) {
            prev = step;
            step += (int) Math.sqrt(n);
            if (prev >= n) {
                return -1; // Target is beyond the array
            }
        }

        // Linear scan backwards from current block to find exact position
        while (data[prev] < target) {
            prev++;
            if (prev == Math.min(step, n)) {
                return -1; // Not found in this block
            }
        }

        // Check if the current element is the target
        if (data[prev] == target) {
            return prev;
        }

        return -1; // Not found
    }
}
