package fyp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * DatasetLoader
 *
 * Loads and prepares the HM Land Registry Price Paid dataset (pp-2023.csv).
 * CSV column layout (0-indexed): 0=Transaction ID, 1=Price, 2=Date, 3=Postcode ...
 * All returned arrays are sorted ascending (required by Binary/Jump/Interpolation Search).
 */
public class DatasetLoader {

    private static final String CSV_PATH = "D:\\ExactTimeSearchModel\\data\\pp-2023.csv";

    /**
     * Loads the full real dataset. Parses the price column, sorts, and returns it.
     * @return sorted int[] of property sale prices (858,018 records for pp-2023.csv)
     */
    public static int[] loadRealDataset() {
        List<Integer> prices = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 2) continue;
                try {
                    int price = Integer.parseInt(columns[1].replaceAll("\"", "").trim());
                    prices.add(price);
                } catch (NumberFormatException e) {
                    // Skip header or malformed lines
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read dataset: " + e.getMessage());
            System.err.println("Check path: " + CSV_PATH);
        }
        int[] data = prices.stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(data);
        return data;
    }

    /**
     * Returns a sorted subset of the full dataset of the given size.
     */
    public static int[] getSubset(int[] fullData, int size) {
        return Arrays.copyOf(fullData, size);
    }

    /**
     * Returns a value guaranteed to exist in the dataset (midpoint element).
     */
    public static int getPresentTarget(int[] data) {
        return data[data.length / 2];
    }

    /**
     * Returns a value guaranteed NOT to exist in the dataset.
     * -1 cannot appear as a property sale price.
     */
    public static int getAbsentTarget(int[] data) {
        return -1;
    }

    /**
     * Generates a synthetic sorted dataset for sizes beyond the real dataset.
     * Fixed seed 42L ensures reproducibility across runs.
     */
    public static int[] generateSyntheticDataset(int size, int maxValue) {
        Random rng = new Random(42L);
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = rng.nextInt(maxValue);
        }
        Arrays.sort(data);
        return data;
    }
}
