package fftlib;


public class FFT {

    // compute the FFT of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k + 1];
        }
        Complex[] r = fft(even);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    public static double[] fft(double[] x, int sc) {
        int len = x.length;
        if (len == 1) {
            return x;
        }
        Complex[] cs = new Complex[len];
        double[] ds = new double[len / 2];
        for (int i = 0; i < len; i++) {
            cs[i] = new Complex(x[i], 0);
        }
        Complex[] ffts = fft(cs);

        for (int i = 0; i < ds.length; i++) {
            ds[i] = Math.sqrt(Math.pow(ffts[i].re(), 2) + Math.pow(ffts[i].im(), 2)) / x.length;
        }
        return ds;
    }

    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }

        return y;

    }

    // compute the circular convolution of x and y
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new IllegalArgumentException("Dimensions don't agree");
        }

        int n = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[n];
        for (int i = 0; i < n; i++) {
            c[i] = a[i].times(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }


    // compute the linear convolution of x and y
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2 * x.length];
        for (int i = 0; i < x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2 * x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2 * y.length];
        for (int i = 0; i < y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2 * y.length; i++) b[i] = ZERO;

        return cconvolve(a, b);
    }

    // display an array of Complex numbers to standard output
    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < SIZE; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }

    private static final int SIZE = 16384 / 4;

    public static double fun(int x) {
        return Math.sin(15f * x);//f= 3.142
    }

    public static double getY(double[] d) {
        double y = 0;
        int x = 0;
        for (int i = 0; i < d.length; i++) {
            if (d[i] > y) {
                y = d[i];
                x = i;
            }
        }
        x++;
        log(String.format("x： %s ， y: %s", x, y));
        log(String.format("频率： %sHz", (float) x / SIZE));
        log(String.format("频率2： %sHz", (float) (SIZE - x) / SIZE));
        log(String.format("振幅： %s", y));
        return y;
    }

    public static void log(String s) {
        System.out.println(s);
    }

}