package fyp;

import java.util.Arrays;

public class TestLoad {

    public static void main(String[] args) {

        int[] prices = DatasetLoader.loadPrices("data/pp-2023.csv", 10000);

        System.out.println("Loaded rows: " + prices.length);
        System.out.println("First 10 prices: " + Arrays.toString(Arrays.copyOf(prices, 10)));
    }
}
