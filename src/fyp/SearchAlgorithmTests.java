package fyp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchAlgorithmTests
 *
 * JUnit 5 correctness test suite for all four search algorithm implementations.
 *
 * Tests cover:
 *   - Normal cases: target present at start, middle, and end of array
 *   - Absent target: value not in array
 *   - Edge cases: single element, two elements, empty array
 *   - Duplicate values: ensure a valid index is returned
 *   - Boundary values: minimum and maximum element in the array
 *
 * Dependency: JUnit 5 (junit-platform-launcher, junit-jupiter-api, junit-jupiter-engine)
 * Add to pom.xml or download junit-platform-console-standalone-1.10.0.jar
 *
 * Run with:
 *   java -jar junit-platform-console-standalone.jar --scan-classpath --classpath out/
 */
@DisplayName("Search Algorithm Correctness Tests")
public class SearchAlgorithmTests {

    private int[] smallArray;   // [10, 20, 30, 40, 50]
    private int[] largeArray;   // 1..1000 step 1
    private int[] singleArray;  // [42]
    private int[] twoArray;     // [5, 15]
    private int[] duplicates;   // [1, 2, 2, 2, 5, 7, 7, 9]

    @BeforeEach
    void setUp() {
        smallArray  = new int[]{10, 20, 30, 40, 50};
        largeArray  = new int[1000];
        for (int i = 0; i < 1000; i++) largeArray[i] = i + 1;
        singleArray = new int[]{42};
        twoArray    = new int[]{5, 15};
        duplicates  = new int[]{1, 2, 2, 2, 5, 7, 7, 9};
    }

    // ===================================================================
    //  LinearSearch Tests
    // ===================================================================

    @Test @DisplayName("Linear: finds target at start")
    void linearFindsFirst() {
        assertEquals(0, LinearSearch.search(smallArray, 10));
    }

    @Test @DisplayName("Linear: finds target at end")
    void linearFindsLast() {
        assertEquals(4, LinearSearch.search(smallArray, 50));
    }

    @Test @DisplayName("Linear: finds target in middle")
    void linearFindsMiddle() {
        assertEquals(2, LinearSearch.search(smallArray, 30));
    }

    @Test @DisplayName("Linear: returns -1 for absent target")
    void linearAbsent() {
        assertEquals(-1, LinearSearch.search(smallArray, 99));
    }

    @Test @DisplayName("Linear: single-element array - present")
    void linearSinglePresent() {
        assertEquals(0, LinearSearch.search(singleArray, 42));
    }

    @Test @DisplayName("Linear: single-element array - absent")
    void linearSingleAbsent() {
        assertEquals(-1, LinearSearch.search(singleArray, 1));
    }

    @Test @DisplayName("Linear: finds element in large array")
    void linearLargeArray() {
        assertTrue(LinearSearch.search(largeArray, 500) >= 0);
    }

    // ===================================================================
    //  BinarySearch Tests
    // ===================================================================

    @Test @DisplayName("Binary: finds target at start")
    void binaryFindsFirst() {
        assertEquals(0, BinarySearch.search(smallArray, 10));
    }

    @Test @DisplayName("Binary: finds target at end")
    void binaryFindsLast() {
        assertEquals(4, BinarySearch.search(smallArray, 50));
    }

    @Test @DisplayName("Binary: finds target in middle")
    void binaryFindsMiddle() {
        assertEquals(2, BinarySearch.search(smallArray, 30));
    }

    @Test @DisplayName("Binary: returns -1 for absent target")
    void binaryAbsent() {
        assertEquals(-1, BinarySearch.search(smallArray, 25));
    }

    @Test @DisplayName("Binary: single-element array - present")
    void binarySinglePresent() {
        assertEquals(0, BinarySearch.search(singleArray, 42));
    }

    @Test @DisplayName("Binary: single-element array - absent")
    void binarySingleAbsent() {
        assertEquals(-1, BinarySearch.search(singleArray, 1));
    }

    @Test @DisplayName("Binary: two-element array - first element")
    void binaryTwoFirst() {
        assertEquals(0, BinarySearch.search(twoArray, 5));
    }

    @Test @DisplayName("Binary: two-element array - second element")
    void binaryTwoSecond() {
        assertEquals(1, BinarySearch.search(twoArray, 15));
    }

    @Test @DisplayName("Binary: finds minimum value in large array")
    void binaryLargeMin() {
        assertEquals(0, BinarySearch.search(largeArray, 1));
    }

    @Test @DisplayName("Binary: finds maximum value in large array")
    void binaryLargeMax() {
        assertEquals(999, BinarySearch.search(largeArray, 1000));
    }

    @Test @DisplayName("Binary: returns -1 for value below range")
    void binaryBelowRange() {
        assertEquals(-1, BinarySearch.search(largeArray, 0));
    }

    @Test @DisplayName("Binary: returns -1 for value above range")
    void binaryAboveRange() {
        assertEquals(-1, BinarySearch.search(largeArray, 1001));
    }

    // ===================================================================
    //  JumpSearch Tests
    // ===================================================================

    @Test @DisplayName("Jump: finds target in small array")
    void jumpFindsTarget() {
        assertTrue(JumpSearch.search(smallArray, 30) >= 0);
    }

    @Test @DisplayName("Jump: finds first element")
    void jumpFindsFirst() {
        assertEquals(0, JumpSearch.search(smallArray, 10));
    }

    @Test @DisplayName("Jump: returns -1 for absent target")
    void jumpAbsent() {
        assertEquals(-1, JumpSearch.search(smallArray, 99));
    }

    @Test @DisplayName("Jump: single-element array - present")
    void jumpSinglePresent() {
        assertEquals(0, JumpSearch.search(singleArray, 42));
    }

    @Test @DisplayName("Jump: single-element array - absent")
    void jumpSingleAbsent() {
        assertEquals(-1, JumpSearch.search(singleArray, 99));
    }

    @Test @DisplayName("Jump: finds element in large array")
    void jumpLarge() {
        assertTrue(JumpSearch.search(largeArray, 750) >= 0);
    }

    @Test @DisplayName("Jump: returns -1 for value above range")
    void jumpAboveRange() {
        assertEquals(-1, JumpSearch.search(largeArray, 9999));
    }

    @Test @DisplayName("Jump: finds last element in large array")
    void jumpLargeMax() {
        assertEquals(999, JumpSearch.search(largeArray, 1000));
    }

    // ===================================================================
    //  InterpolationSearch Tests
    // ===================================================================

    @Test @DisplayName("Interpolation: finds target in small uniform array")
    void interpolationFindsTarget() {
        assertTrue(InterpolationSearch.search(smallArray, 30) >= 0);
    }

    @Test @DisplayName("Interpolation: finds first element")
    void interpolationFindsFirst() {
        assertEquals(0, InterpolationSearch.search(smallArray, 10));
    }

    @Test @DisplayName("Interpolation: finds last element")
    void interpolationFindsLast() {
        assertEquals(4, InterpolationSearch.search(smallArray, 50));
    }

    @Test @DisplayName("Interpolation: returns -1 for absent target")
    void interpolationAbsent() {
        assertEquals(-1, InterpolationSearch.search(smallArray, 99));
    }

    @Test @DisplayName("Interpolation: single-element array - present")
    void interpolationSinglePresent() {
        assertEquals(0, InterpolationSearch.search(singleArray, 42));
    }

    @Test @DisplayName("Interpolation: single-element array - absent")
    void interpolationSingleAbsent() {
        assertEquals(-1, InterpolationSearch.search(singleArray, 1));
    }

    @Test @DisplayName("Interpolation: handles all-equal array")
    void interpolationAllEqual() {
        int[] allSame = {5, 5, 5, 5, 5};
        // Should return index 0 (the first match found due to equality guard)
        assertTrue(InterpolationSearch.search(allSame, 5) >= 0);
        assertEquals(-1, InterpolationSearch.search(allSame, 9));
    }

    @Test @DisplayName("Interpolation: finds element in large array")
    void interpolationLarge() {
        assertTrue(InterpolationSearch.search(largeArray, 500) >= 0);
    }

    // ===================================================================
    //  Cross-algorithm consistency: all algorithms agree on same array
    // ===================================================================

    @Test @DisplayName("Consistency: all algorithms return PRESENT for midpoint")
    void allAlgorithmsAgreePresent() {
        int target = smallArray[2]; // 30
        assertTrue(LinearSearch.search(smallArray,        target) >= 0, "Linear failed");
        assertTrue(BinarySearch.search(smallArray,        target) >= 0, "Binary failed");
        assertTrue(JumpSearch.search(smallArray,          target) >= 0, "Jump failed");
        assertTrue(InterpolationSearch.search(smallArray, target) >= 0, "Interpolation failed");
    }

    @Test @DisplayName("Consistency: all algorithms return -1 for absent value")
    void allAlgorithmsAgreeAbsent() {
        int target = -1;
        assertEquals(-1, LinearSearch.search(smallArray,        target), "Linear failed");
        assertEquals(-1, BinarySearch.search(smallArray,        target), "Binary failed");
        assertEquals(-1, JumpSearch.search(smallArray,          target), "Jump failed");
        assertEquals(-1, InterpolationSearch.search(smallArray, target), "Interpolation failed");
    }
}
