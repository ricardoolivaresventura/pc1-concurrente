import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

public class BucketSortParallel {
    public static void parallelBucketSort(int[] arr, int numThreads) {
        int n = arr.length;
        ArrayList<Integer>[] buckets = new ArrayList[n];

        // Inicializar los buckets
        for (int i = 0; i < n; i++) {
            buckets[i] = new ArrayList<Integer>();
        }

        // Colocar elementos en los buckets
        for (int i = 0; i < n; i++) {
            int bucketIndex = arr[i] / (n / numThreads);
            buckets[bucketIndex].add(arr[i]);
        }

        // Crear y lanzar hilos para ordenar los buckets en paralelo
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int start = i * (n / numThreads);
            final int end = (i == numThreads - 1) ? n : (i + 1) * (n / numThreads);
            threads[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    Collections.sort(buckets[j]);
                }
            });
            threads[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Combinar los buckets ordenados en el arreglo original
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < buckets[i].size(); j++) {
                arr[index++] = buckets[i].get(j);
            }
        }
    }

    public static void main(String[] args) {
        int numThreads = 16; // Número de hilos
        int n = 1000000; // Tamaño del arreglo
        int[] arr = new int[n];
        int[] originalArr = new int[n]; // Copia del arreglo original para la comparación

        // Llenar el arreglo con valores aleatorios
        for (int i = 0; i < n; i++) {
            arr[i] = (int) (Math.random() * 1000);
            originalArr[i] = arr[i]; // Copiar el arreglo original
        }

        // Iniciar el contador de tiempo
        long startTime = System.currentTimeMillis();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryBefore = memoryBean.getHeapMemoryUsage();

        long totalMemory = Runtime.getRuntime().totalMemory();

        parallelBucketSort(arr, numThreads);

        MemoryUsage memoryAfter = memoryBean.getHeapMemoryUsage();

        // Detener el contador de tiempo
        long endTime = System.currentTimeMillis();

        // Calcular el tiempo transcurrido en segundos
        long parallelTime = endTime - startTime;
        // Calcular la diferencia de memoria
        long memoryUsed = memoryAfter.getUsed() - memoryBefore.getUsed();
        double memoryUsagePercentage = (double) memoryAfter.getUsed() / totalMemory * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedPercentage = df.format(memoryUsagePercentage);

        // Iniciar el contador de tiempo para Arrays.parallelSort
        startTime = System.currentTimeMillis();

        // Usar Arrays.parallelSort para ordenar el arreglo original
        Arrays.parallelSort(originalArr);

        // Detener el contador de tiempo para Arrays.parallelSort
        endTime = System.currentTimeMillis();

        // Calcular el tiempo transcurrido en segundos para Arrays.parallelSort
        double parallelSortTimeInSeconds = endTime - startTime;

        // Verificar si los arreglos son iguales
        boolean isSorted = Arrays.equals(arr, originalArr);
        System.out.println("\nEl arreglo ha sido ordenado correctamente: " + isSorted);
        System.out.println("Tiempo transcurrido (tu algoritmo): " + parallelTime + " ms");
        System.out.println("Tiempo transcurrido (parallelSort): " + parallelSortTimeInSeconds + " ms");
        System.out.println("Uso de memoria: " + memoryUsed + " bytes");
        System.out.println("Uso de memoria: " + formattedPercentage  + "%");

    }
}