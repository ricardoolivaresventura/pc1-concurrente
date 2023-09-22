import java.util.Arrays;
import java.util.Random;

public class ParallelRadixSort {

    public static void parallelRadixSort(int[] arr, int h, int n) {
        int max = Arrays.stream(arr).max().orElse(0);

        for (int digit = 1; max / digit > 0; digit *= 10) {
            Thread[] threads = new Thread[h];

            for (int i = 0; i < h; i++) {
                final int threadId = i;
                final int start = threadId * (n / h);
                final int end = (threadId == h - 1) ? n : start + (n / h);
                final int currentDigit = digit;
                threads[i] = new Thread(() -> {
                    radixSort(arr, start, end, currentDigit);
                });
                threads[i].start();
            }

            try {
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void radixSort(int[] arr, int start, int end, int digit) {
        int[] output = new int[end - start];
        int[] count = new int[10];

        for (int i = start; i < end; i++) {
            count[(arr[i] / digit) % 10]++;
        }

        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        for (int i = end - 1; i >= start; i--) {
            output[count[(arr[i] / digit) % 10] - 1] = arr[i];
            count[(arr[i] / digit) % 10]--;
        }

        for (int i = start, j = 0; i < end; i++, j++) {
            arr[i] = output[j];
        }
    }

    public static void main(String[] args) {
        int h = 16; // Número de hilos
        int n = 1000000; // Tamaño del arreglo
        int[] arr = new int[n];

        // Llenar el arreglo con valores aleatorios
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextInt(n);
        }

        // Medir el tiempo de ejecución de Radix Sort en paralelo
        long startTime = System.currentTimeMillis();
        parallelRadixSort(arr, h, n);
        long endTime = System.currentTimeMillis();
        long parallelTime = endTime - startTime;

        // Medir el tiempo de ejecución de parallelSort
        int[] copyArr = Arrays.copyOf(arr, n);
        startTime = System.currentTimeMillis();
        Arrays.parallelSort(copyArr);
        endTime = System.currentTimeMillis();
        long javaParallelSortTime = endTime - startTime;

        System.out.println("Tiempo de ejecución de Radix Sort en paralelo: " + parallelTime + " ms");
        System.out.println("Tiempo de ejecución de parallelSort: " + javaParallelSortTime + " ms");
    }
}

