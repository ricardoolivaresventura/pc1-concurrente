package com.mycompany.transformadarapidadefourier;

import java.text.DecimalFormat;
import java.util.concurrent.*;
import java.util.Arrays;

public class ParallelFFT {

    private Complex[] data;
    private Complex[] resultCooleyTukey;
    private Complex[] resultRadix2;
    private int n;
    private int h;

    public ParallelFFT(Complex[] data, int h) {
        this.data = data;
        this.n = data.length;
        this.resultCooleyTukey = new Complex[n];
        this.resultRadix2 = new Complex[n];
        this.h = h;
    }

    public void computeCooleyTukeyFFT() {
        ExecutorService executor = Executors.newFixedThreadPool(h);
        for (int i = 0; i < h; i++) {
            int start = i * (n / h);
            int end = (i == h - 1) ? n : (i + 1) * (n / h);
            executor.submit(new FFTTask(start, end, resultCooleyTukey));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void computeRadix2FFT() {
        ExecutorService executor = Executors.newFixedThreadPool(h);
        for (int i = 0; i < h; i++) {
            int start = i * (n / h);
            int end = (i == h - 1) ? n : (i + 1) * (n / h);
            executor.submit(new Radix2FFTTask(start, end, resultRadix2));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class FFTTask implements Runnable {

        private int start;
        private int end;
        private Complex[] result;

        public FFTTask(int start, int end, Complex[] result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }

        @Override
        public void run() {
            // Realiza la FFT en la porción de datos [start, end)
            computeCooleyTukeyFFT(start, end, result);
        }
    }

    private class Radix2FFTTask implements Runnable {

        private int start;
        private int end;
        private Complex[] result;

        public Radix2FFTTask(int start, int end, Complex[] result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }

        @Override
        public void run() {
            // Realiza la FFT en la porción de datos [start, end)
            computeRadix2FFT(start, end, result);
        }
    }

    private void computeCooleyTukeyFFT(int start, int end, Complex[] result) {
        if (end - start <= 1) {
            result[start] = data[start];
            return;
        }

        int length = end - start;
        int halfLength = length / 2;
        Complex[] even = new Complex[halfLength];
        Complex[] odd = new Complex[halfLength];

        for (int i = 0; i < halfLength; i++) {
            even[i] = data[start + 2 * i];
            odd[i] = data[start + 2 * i + 1];
        }

        computeCooleyTukeyFFT(start, start + halfLength, even);
        computeCooleyTukeyFFT(start + halfLength, end, odd);

        for (int i = 0; i < halfLength; i++) {
            Complex t = Complex.polar(1, -2 * Math.PI * i / length).times(odd[i]);
            result[start + i] = even[i].plus(t);
            result[start + i + halfLength] = even[i].minus(t);
        }
    }

    private void computeRadix2FFT(int start, int end, Complex[] result) {
        int N = end - start;

        // Caso base: si N es 1, la FFT es el propio valor
        if (N == 1) {
            result[start] = data[start];
            return;
        }

        // Divide la secuencia en pares e impares
        Complex[] even = new Complex[N / 2];
        Complex[] odd = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = data[start + 2 * k];
            odd[k] = data[start + 2 * k + 1];
        }

        // Calcula la FFT de los pares e impares de forma recursiva
        computeRadix2FFT(start, start + N / 2, even);
        computeRadix2FFT(start + N / 2, end, odd);

        // Combina los resultados de forma eficiente
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            Complex t = wk.times(odd[k]);
            result[start + k] = even[k].plus(t);
            result[start + k + N / 2] = even[k].minus(t);
        }
    }

        public static void main(String[] args) {
        int n = 121184; 
        int h = 32;    

        Complex[] data = new Complex[n];

        for (int i = 0; i < n; i++) {
            data[i] = new Complex(Math.random(), Math.random());
        }

        ParallelFFT parallelFFT = new ParallelFFT(data, h);

        long startTime = System.currentTimeMillis();
        parallelFFT.computeCooleyTukeyFFT();
        long endTime = System.currentTimeMillis();
        double cooleyTukeyTime = (endTime - startTime) + 0.0; 

        for (int i = 0; i < n; i++) {
            data[i] = new Complex(Math.random(), Math.random());
        }

        startTime = System.currentTimeMillis();
        parallelFFT.computeRadix2FFT();
        endTime = System.currentTimeMillis();
        double radix2Time = (endTime - startTime) + 0.0;

        DecimalFormat df = new DecimalFormat("#0.000");
        String cooleyTukeyTimeFormatted = df.format(cooleyTukeyTime);
        String radix2TimeFormatted = df.format(radix2Time);

        System.out.println("Tiempo Cooley-Tukey: " + cooleyTukeyTimeFormatted + " milisegundos");
        System.out.println("Tiempo Radix-2: " + radix2TimeFormatted + " milisegundos");
    }

}

class Complex {

    double real, imag;

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public Complex plus(Complex other) {
        return new Complex(this.real + other.real, this.imag + other.imag);
    }

    public Complex minus(Complex other) {
        return new Complex(this.real - other.real, this.imag - other.imag);
    }

    public Complex times(Complex other) {
        return new Complex(
                this.real * other.real - this.imag * other.imag,
                this.real * other.imag + this.imag * other.real
        );
    }

    public static Complex polar(double r, double theta) {
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }
}
