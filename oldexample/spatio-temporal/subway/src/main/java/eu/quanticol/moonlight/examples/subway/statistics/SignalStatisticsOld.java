package eu.quanticol.moonlight.examples.subway.statistics;

import java.util.Arrays;

public class SignalStatisticsOld {

    private int size;
    private int steps;

    private int samplings;

    private double[] data;
    private double[][] dataTraj;

    private double[] square;
    private double[][] squareTraj;

    private double[] average;
    private double[][] averageTraj;

    private double[] variance;
    private double[][] varianceTraj;

    private double[] standardDeviation;
    private double[][] standardDeviationTraj;

    private double[] min;
    private double[][] minTraj;

    private double[] max;
    private double[][] maxTraj;

    public SignalStatisticsOld(int size) {
        this.size = size;
        this.data = new double[size];
        this.min = new double[size];
        Arrays.fill(this.min, Double.MAX_VALUE);
        this.max = new double[size];
        Arrays.fill(this.max, Double.MIN_VALUE);
        this.samplings = 0;
        this.square = new double[size];
    }

    public SignalStatisticsOld(int size, int steps) {
        this.size = size;
        this.steps= steps;
        this.dataTraj = new double[size][steps];
        this.minTraj = new double[size][steps];
        this.maxTraj = new double[size][steps];
        for(int i=0;i<size;i++){
            Arrays.fill(this.minTraj[i], Double.MAX_VALUE);
            Arrays.fill(this.maxTraj[i], Double.MIN_VALUE);
        }
        this.samplings = 0;
        this.squareTraj = new double[size][steps];
    }

    public synchronized void add( double[] data ) {
        if (data.length != size) {
            throw new IllegalArgumentException("Wrong array size: expected "+size+" is "+data.length+".");
        }
        this.data = doSum(this.data, data);
        this.square = doSum( this.square, square( data ) );
        doMin( data );
        doMax( data );
        this.average = null;
        this.variance = null;
        this.standardDeviation = null;
        this.samplings++;
    }

    public synchronized void add( double[][] dataTraj ) {
        if (dataTraj.length != size) {
            throw new IllegalArgumentException("Wrong array size: expected "+size+" is "+dataTraj.length+".");
        }
        if (dataTraj[0].length!=steps) {
            throw new IllegalArgumentException("Wrong steps size: expected "+steps+" is "+dataTraj[0].length+".");
        }
        this.dataTraj = doSum(this.dataTraj, dataTraj);
        this.squareTraj = doSum( this.squareTraj, square( dataTraj ) );
        doMin( dataTraj );
        doMax( dataTraj );
        this.averageTraj = null;
        this.varianceTraj = null;
        this.standardDeviationTraj = null;
        this.samplings++;
    }

    private void doMin( double[] data ) {
        for( int i=0 ; i<size; i++ ) {
            this.min[i] = Math.min(this.min[i], data[i]);
        }
    }

    private void doMin( double[][] dataTraj ) {
        for( int i=0 ; i<size; i++ ) {
            for( int j=0 ; j<steps; j++ ) {
                this.minTraj[i][j] = Math.min(this.minTraj[i][j], dataTraj[i][j]);
            }
        }
    }

    private void doMax( double[] data ) {
        for( int i=0 ; i<size; i++ ) {
            this.max[i] = Math.max(this.max[i], data[i]);
        }
    }

    private void doMax( double[][] dataTraj ) {
        for( int i=0 ; i<size; i++ ) {
            for( int j=0 ; j<steps; j++ ) {
                this.minTraj[i][j] = Math.max(this.maxTraj[i][j], dataTraj[i][j]);
            }
        }
    }

    private double[] square(double[] data) {
        double[] result = new double[size];
        for( int i=0 ; i<size; i++ ) {
            result[i] = Math.pow(data[i], 2);
        }
        return result;
    }

    private double[][] square(double[][] dataTraj) {
        double[][] result = new double[size][steps];
        for( int i=0 ; i<size; i++ ) {
            for (int j=0; j<steps;j++)
                result[i][j] = Math.pow(dataTraj[i][j], 2);
        }
        return result;
    }
    /**
     * Sum two arrays and save the result in the first one. Returns a reference to the first array.
     *
     * @param v1
     * @param v2
     * @return
     */
    private double[] doSum( double[] v1 , double[] v2 ) {
        for( int i=0 ; i<v1.length ; i++ ) {
            v1[i] = v1[i]+v2[i];
        }
        return v1;
    }

    private double[][] doSum( double[][] v1 , double[][] v2 ) {
        for( int i=0 ; i<v1.length ; i++ ) {
            for (int j=0; j< v1[0].length; j++)
                v1[i][j] = v1[i][j]+v2[i][j];
        }
        return v1;
    }

    public synchronized double[] getAverage( ) {
        if( this.average == null ) {
            computeAverage();
        }
        return this.average;
    }

    public synchronized double[][] getAverageTraj( ) {
        if( this.averageTraj == null ) {
            computeAverageTraj();
        }
        return this.averageTraj;
    }

    private void computeAverage() {
        if (this.samplings == 0) {
            return ;
        }
        this.average = new double[size];
        for( int i=0 ; i<size ; i++ ) {
            this.average[i] = this.data[i]/this.samplings;
        }
    }

    private void computeAverageTraj() {
        if (this.samplings == 0) {
            return ;
        }
        this.averageTraj = new double[size][steps];
        for( int i=0 ; i<size ; i++ ) {
            for(int j=0;j<steps; j++){
                this.averageTraj[i][j] = this.dataTraj[i][j]/this.samplings;
            }
        }
    }
    public synchronized double[] getVariance() {
        if (this.variance == null) {
            computeVariance();
        }
        return this.variance;
    }

    private void computeVariance() {
        double[] average = getAverage();
        if (average == null) {
            return ;
        }
        this.variance = new double[this.size];
        for( int i=0 ; i<size ; i++ ) {
            this.variance[i] = (this.square[i]/this.samplings)-Math.pow(this.average[i], 2);
        }
    }

    public synchronized double[] getStandardDeviation() {
        if (this.standardDeviation == null) {
            computeStandardDeviation();
        }
        return this.standardDeviation;
    }

    private void computeStandardDeviation() {
        double[] variance = getVariance();
        if (variance == null) {
            return ;
        }
        this.standardDeviation = new double[this.size];
        for( int i=0 ; i<size; i++ ) {
            this.standardDeviation[i] = Math.sqrt(this.variance[i]/this.samplings);
        }
    }

    public double[] getMin() {
        return Arrays.copyOf(this.min, size);
    }

    public double[] getMax() {
        return Arrays.copyOf(this.max, size);
    }

}
