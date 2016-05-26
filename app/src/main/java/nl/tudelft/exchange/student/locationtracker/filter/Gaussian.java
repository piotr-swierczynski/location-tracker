package nl.tudelft.exchange.student.locationtracker.filter;

/**
 * Created by Piotr on 2016-05-25.
 */
public final class Gaussian {

    // return pdf(x) = standard Gaussian pdf
    public static double pdf(double x) {
        return Math.exp(-(x*x) / 2) / Math.sqrt(2 * Math.PI);
    }

    // return pdf(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    public static double pdf(double x, double mu, double sigma) {
        return pdf((x - mu) / sigma) / sigma;
    }
}
