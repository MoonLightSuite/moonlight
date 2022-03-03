package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.core.base.AbstractInterval;
import eu.quanticol.moonlight.online.signal.TimeChain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

/**
 * <em>EXPERIMENTAL:</em> Utility class for plotting.
 * <p>
 * !! Requires python 3+ and Matplotlib in running environment
 * </p>
 */
public class Plotter {
    private final List<Thread> threads;
    private final boolean isAsync;

    private final double infinity;

    public Plotter(double maxValue) {
        this(true, maxValue);
    }

    public Plotter(boolean async, double maxValue) {
        threads = new ArrayList<>();
        isAsync = async;
        infinity = maxValue;
    }

    /**
     * Required for actually showing async plots
     */
    public void waitActivePlots(long seconds) {
        try {
            for (Thread t: threads) {
                t.join(seconds * 1000);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Plot coordinator died " +
                                            "unexpectedly");
        }
    }
    
    public void plot(List<Double> data, String name, String label) {
        if(isAsync)
            asyncShow(() -> plotSingle(data, name, label));
        else
            plotSingle(data, name, label);
    }

    public void plot(List<Double> dataDown, List<Double> dataUp, String name) {
        List<Double> down = replaceInfinite(dataDown);
        List<Double> up = replaceInfinite(dataUp);

        if(isAsync)
            asyncShow(() -> plotInterval(down, up, name));
        else
            plotInterval(down, up, name);
    }

    public void plot(TimeChain<Double, AbstractInterval<Double>> data,
                     String name)
    {
        List<Double> dataDown =
            replaceInfinite(data.stream()
                    .map(x -> x.getValue().getStart())
                    .collect(Collectors.toList()));
        List<Double> dataUp =
                replaceInfinite(data.stream()
                        .map(x -> x.getValue().getEnd())
                        .collect(Collectors.toList()));
//        List<Double> times = data.stream().map(SegmentInterface::getStart)
//                                 .collect(Collectors.toList());

        if(isAsync)
            asyncShow(() -> plotInterval(dataDown, dataUp, name));
        else
            plotInterval(dataDown, dataUp, name);
    }

    public void plotAll(TimeChain<Double, List<AbstractInterval<Double>>> data,
                        String name)
    {
        int locations = data.getFirst().getValue().size();
        IntStream.range(0, locations)
                 .forEach(location -> plotOne(data, name, location));

    }

    public void plotOne(TimeChain<Double, List<AbstractInterval<Double>>> data,
                        String name, int location)
    {
            List<Double> dataDown =
                    replaceInfinite(data.stream()
                            .map(x -> x.getValue().get(location).getStart())
                            .collect(Collectors.toList()));
            List<Double> dataUp =
                    replaceInfinite(data.stream()
                            .map(x -> x.getValue().get(location).getEnd())
                            .collect(Collectors.toList()));

            String locName = name + "@loc-" + location;
            if(isAsync)
                asyncShow(() -> plotInterval(dataDown, dataUp, locName));
            else
                plotInterval(dataDown, dataUp, locName);

    }

    public void plotOne(TimeChain<Double, List<Double>> data,
                        String name, int location, String label)
    {
        List<Double> values =
                replaceInfinite(data.stream()
                        .map(x -> x.getValue().get(location))
                        .collect(Collectors.toList()));

        String locName = name + "@loc-" + location;
        if(isAsync)
            asyncShow(() -> plotSingle(values, locName, label));
        else
            plotSingle(values, locName, label);

    }

    private Plot createPlot(String name) {
        Plot plt = Plot.create();
        plt.xlabel("times");
        //plt.ylabel("robustness");
        plt.title(name);
        plt.legend();
        return plt;
    }

    private void addData(Plot plt, List<Double> data, String label) {
        plt.plot().add(data).label(label);
    }

    private void showPlot(Plot plt) {
        try {
            plt.show();
        } catch (PythonExecutionException | IOException e) {
            System.err.println("unable to plot!");
            e.printStackTrace();
        }
    }

    private void plotInterval(List<Double> dataDown, List<Double> dataUp,
                              String name)
    {
        Plot plt = createPlot(name);
        addData(plt, dataDown, "rho_down");
        addData(plt, dataUp, "rho_up");
        //plt.xticks(times);    // Experimental feature, still unreliable
        showPlot(plt);
    }

    private void plotSingle(List<Double> data, String name, String label) {
        Plot plt = createPlot(name);
        data = replaceInfinite(data);
        addData(plt, data, label);
        //plt.xticks(times);    // Experimental feature, still unreliable
        showPlot(plt);
    }

    private void asyncShow(Runnable r) {
        Thread t = new Thread(r);
        threads.add(t);
        t.start();
    }

    private List<Double> replaceInfinite(List<Double> vs) {
        vs = vs.stream().map(v -> v.equals(Double.POSITIVE_INFINITY) ?  infinity : v)
                .map(v -> v.equals(Double.NEGATIVE_INFINITY) ? - infinity : v)
                .collect(Collectors.toList());
        return vs;
    }
}
