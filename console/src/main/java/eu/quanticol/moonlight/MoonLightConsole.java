package eu.quanticol.moonlight;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import eu.quanticol.moonlight.script.ScriptLoader;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.space.LocationServiceList;
import eu.quanticol.moonlight.io.MoonLightRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoonLightConsole {

    private static final String LOADING_SCRIPT_MESSAGE = "Loading script from %s";
    private static final String IS_A_TEMPORAL_SCRIPT = "%s is a temporal script...";
    private static final String IS_A_SPATIAL_SCRIPT = "%s is a spatio temporal script...";
    private static final String LOADING_SIGNAL_MESSAGE = "Loading signal from %s";
    private static final String LOADING_SPATIAL_SIGNAL_MESSAGE = "Loading spatial signal from %s";
    private static final String LOADING_SPACE_MESSAGE = "Loading location service from %s";
    private static final String SELECTING_SCRIPT_COMPONENT = "Monitor \"%s\" has been selected.";
    private static final String SELECTING_DEFAULT_SCRIPT_COMPONENT = "Default monitor has been selected.";
    private static final String MONITOR_INSTANTIATED = "Monitor \"%s\" instantiated with parameters %s.";
    private static final String MONITORING_STARTED = "Monitoring started...";
    private static final String SAVING_RESULTS = "Saving results in %s";
    private static final String UNKNOWN_DOMAIN = "Domain %s is unknown!";
    private static final String SELECTING_SIGNAL_DOMAIN = "Selecting signal domain %s.";
    private static final String NO_PARAMETERS_MESSAGE =  "No parameter is passed to MoonLight Console!";
    private static final String OPTION_LONG_HELP = "--help";
    private static final String OPTION_SHORT_HELP = "-h";
    private static final String OPTION_LONG_MONITOR = "--monitor";
    private static final String OPTION_SHORT_MONITOR = "-m";
    private static final String OPTION_LONG_SPACE = "--space";
    private static final String OPTION_SHORT_SPACE = "-s";
    private static final String OPTION_LONG_INPUT = "--input";
    private static final String OPTION_SHORT_INPUT = "-i";
    private static final String OPTION_LONG_OUTPUT = "--output";
    private static final String OPTION_SHORT_OUTPUT = "-o";
    private static final String OPTION_LONG_DOMAIN = "--domain";
    private static final String OPTION_SHORT_DOMAIN = "-d";
    private static final String OPTION_LONG_ARGS = "--args";
    private static final String OPTION_SHORT_ARGS = "-a";
    private static final String OPTION_LONG_QUIET = "--quiet";
    private static final String OPTION_SHORT_QUIET = "-q";
    private static final String INPUT = "input";
    private static final String SCRIPT = "script";
    private static final String OUTPUT = "output";
    private static final String DOMAIN = "domain";
    private static final String SPACE = "space";
    private static final String ARGS = "args";
    private static final String QUIET = "quiet";

    private static final String MONITOR = "monitor";


    private Map<String,String> parameters;
    private String[] scriptArgs;
    private boolean isHelp;
    private final PrintStream out;
    private final PrintStream err;

    public MoonLightConsole() {
        this(System.out, System.err);
    }

    public MoonLightConsole(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
        this.parameters = new HashMap<>();
    }


    public static void main(String[] args) {
        MoonLightConsole console = new MoonLightConsole();
        try {
            console.parseArgs(args);
            console.execute();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Illegal arguments!");
            console.help();
        } catch(Throwable e) {
            System.err.println(e.getMessage());
        }
    }

    private void help() {
        out.println("MoonLight Console: Synopsis");
        out.println("\n");
        out.println("Show this help:");
        out.println("   mlconsole --help");
        out.println("   mlconsole -h");
        out.println("\n");
        out.println("Perform monitoring:");
        out.println("   mlconsole <scriptfile> optionlists (--args|-a arg1 arg2...)");
        out.println("  --input|-i <signalfile>: signal file name, if omitted 'input.csv' is used");
        out.println("  --output|-o <outputfile>: output file name, if omitted 'output.csv' is used");
        out.println("  --monitor|-m <monitorname>: property name to monitor");
        out.println("  --space|-s <spacefile>: space file name, if omitted 'space.csv' is used");
        out.println("  --domain|-d minmax|boolean: domain to use in the monitoring procedure");
    }

    private void parseArgs(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(NO_PARAMETERS_MESSAGE);
        }
        if ((args.length == 1)&&(isHelpOption(args[0]))) {
            this.isHelp = true;
            return ;
        }
        parameters.put(SCRIPT,args[0]);
        int counter = 1;
        while (counter<args.length) {
            counter = parseNextArg(counter, args);
        }
    }

    private int parseNextArg(int counter, String[] args) {
        if (checkOption(OPTION_LONG_MONITOR, OPTION_SHORT_MONITOR,args, counter)) {
            return setKey(MONITOR,args[counter],counter+1,args);
        }
        if (checkOption(OPTION_LONG_INPUT, OPTION_SHORT_INPUT, args, counter)) {
            return setKey(INPUT,args[counter],counter+1,args);
        }
        if (checkOption(OPTION_LONG_OUTPUT, OPTION_SHORT_OUTPUT, args, counter)) {
            return setKey(OUTPUT,args[counter],counter+1,args);
        }
        if (checkOption(OPTION_LONG_DOMAIN, OPTION_SHORT_DOMAIN, args, counter)) {
            return setKey(DOMAIN,args[counter],counter+1,args);
        }
        if (checkOption(OPTION_LONG_SPACE, OPTION_SHORT_SPACE, args, counter)) {
            return setKey(SPACE,args[counter],counter+1,args);
        }
        if (checkOption(OPTION_LONG_ARGS, OPTION_SHORT_ARGS, args, counter)) {
            this.scriptArgs = Arrays.copyOfRange(args,counter+1,args.length);
            return args.length;
        }
        throw new IllegalArgumentException(String.format("Unknown option %s at %d", args[counter], counter));
    }

    private int setKey(String option, String key, int i, String[] args) {
        if (this.parameters.containsKey(key)) {
            throw new IllegalArgumentException(replicatedOption(option));
        }
        if (i<args.length) {
            isNotAnOption(option,i,args);
            this.parameters.put(key, args[i]);
            return i+1;
        } else {
            throw new IllegalArgumentException(noValueAfter(option));
        }
    }

    public boolean checkOption(String longKeywork, String shortKeyword, String[] args, int idx) {
        return longKeywork.equals(args[idx])||shortKeyword.equals(args[idx]);
    }

    private String replicatedOption(String option) {
        return String.format("Duplicated option %s!",option);
    }

    private String noValueAfter(String option) {
        return String.format("A value is expected after %s!",option);
    }

    private String noOptionMessage(String option, int i, String arg) {
        return String.format("Illegal parameter %d: after %s a name is expected while %s is provided!",i,option,arg);
    }

    private void isNotAnOption(String option, int i, String[] args) {
        if (args[i].startsWith("-")) {
            throw new IllegalArgumentException(noOptionMessage(option,i,args[i]));
        }
    }

    private boolean isHelpOption(String arg) {
        return OPTION_LONG_HELP.equals(arg)||OPTION_SHORT_HELP.equals(arg);
    }


    private void execute() throws IOException, MoonLightScriptLoaderException {
        if (this.isHelp) {
            this.help();
        } else {
            this.doMonitoring();
        }
//        showActivityCompletedMessage();
//        showMessage(String.format(IS_A_TEMPORAL_SCRIPT,"./script.mls"));
//        showStartActivityMessage(String.format(SELECTING_SCRIPT_COMPONENT,"amonitor"));
//        showActivityCompletedMessage();
//        showMessage(String.format(MONITOR_INSTANTIATED,"amonitor","[0.1,2.5]"));
//        showStartActivityMessage(String.format(LOADING_SCRIPT_MESSAGE,"./inputsignal.csv"));
//        showActivityCompletedMessage();
//        showStartActivityMessage(MONITORING_STARTED);
//        showActivityCompletedMessage();
//        showMessage(String.format("Monitoring time: %.3fs",0.453));
//        showStartActivityMessage(String.format(SAVING_RESULTS,"./result.csv"));
//        showActivityCompletedMessage();
    }

    private void doMonitoring() throws IOException, MoonLightScriptLoaderException {
            MoonLightScript script = loadScript(parameters.get(SCRIPT));
        if (script.isTemporal()) {
            showMessage(String.format(IS_A_TEMPORAL_SCRIPT,parameters.get(SCRIPT)));
            doTemporalMonitoring(script.temporal());
        } else {
            showMessage(String.format(IS_A_SPATIAL_SCRIPT,parameters.get(SCRIPT)));
            doSpatialTemporalMonitoring(script.spatialTemporal());
        }
    }

    private void showErrorMessage(String message) {
        err.println(message);
    }

    private void doSpatialTemporalMonitoring(MoonLightSpatialTemporalScript spatialTemporal) throws IOException {
        if (parameters.containsKey(DOMAIN)) {
            showStartActivityMessage(String.format(SELECTING_SIGNAL_DOMAIN,parameters.get(DOMAIN)));
            spatialTemporal.setMonitoringDomain(getDomain(parameters.get(DOMAIN)));
            showActivityCompletedMessage();
        }
        SpatialTemporalScriptComponent<?> component;
        if (parameters.containsKey(MONITOR)) {
            component = spatialTemporal.selectSpatialTemporalComponent(parameters.get(MONITOR));
            showStartActivityMessage(String.format(SELECTING_SCRIPT_COMPONENT,parameters.get(MONITOR)));
        } else {
            component = spatialTemporal.selectDefaultSpatialTemporalComponent();
            showStartActivityMessage(SELECTING_DEFAULT_SCRIPT_COMPONENT);
        }
        SpatialTemporalSignal<MoonLightRecord> signal = loadSpatialTemporalSignal(component.getSignalHandler(),parameters.getOrDefault(INPUT,"input.csv"));
        LocationService<Double, MoonLightRecord> space = loadLocationService(signal.size(),component.getEdgeHandler(),parameters.getOrDefault(SPACE, "space.csv"));
        showStartActivityMessage(MONITORING_STARTED);
        long startingTime = System.currentTimeMillis();
        double[][][] result = component.monitorToArrayFromString(space,signal,scriptArgs);
        long elapsed = System.currentTimeMillis()-startingTime;
        showActivityCompletedMessage();
        showMessage(String.format("Monitoring time: %.3fs",elapsed/1000.0));
        writeSpatialTemporalResultsToFile(result,parameters.getOrDefault(OUTPUT, "output.csv"));
    }


    private SpatialTemporalSignal<MoonLightRecord> loadSpatialTemporalSignal(RecordHandler signalHandler, String fileName) throws IOException {
        showStartActivityMessage(String.format(LOADING_SPATIAL_SIGNAL_MESSAGE, fileName));
        List<String[]> rows = Files.readAllLines(Paths.get(fileName)).stream().map(l -> l.split(";")).collect(Collectors.toList());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        int length = rows.get(0).length;
        if (((length-1)%signalHandler.size()) != 0) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        if (rows.stream().anyMatch(r -> r.length != length)) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        int size = (length-1)/(signalHandler.size());
        SpatialTemporalSignal<MoonLightRecord> signal = new SpatialTemporalSignal<>(size);
        for (String[] row: rows) {
            double[] values = Arrays.stream(row).mapToDouble(Double::parseDouble).toArray();
            signal.add(values[0],i -> signalHandler.fromDoubleArray(values,1+i*size,1+(i+1)*size));
        }
        showActivityCompletedMessage();
        return signal;
    }

    private LocationService<Double, MoonLightRecord> loadLocationService(int size, RecordHandler edgeHandler, String fileName) throws IOException {
        showStartActivityMessage(String.format(LOADING_SPACE_MESSAGE, fileName));
        List<String[]> rows = Files.readAllLines(Paths.get(fileName)).stream().map(l -> l.split(";")).collect(Collectors.toList());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        if (rows.stream().anyMatch(r -> (r.length != 2 + size)||(r.length == 1))) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        LocationServiceList<MoonLightRecord> locationService = new LocationServiceList<>();
        GraphModel<MoonLightRecord> current = null;
        double time = Double.NaN;
        for (String[] row: rows) {
            double nextTime = getNextTime(time,row);
            current = saveModel(locationService, time, nextTime, current);
            current.add(Integer.parseInt(row[0]), edgeHandler.fromDoubleArray(Stream.of(row).skip(2).mapToDouble(Double::parseDouble).toArray()),Integer.parseInt(row[1]));
        }
        showActivityCompletedMessage();
        return locationService;
    }

    private GraphModel<MoonLightRecord> saveModel(LocationServiceList<MoonLightRecord> locationService, double time, double nextTime, GraphModel<MoonLightRecord> current) {
        if ((current != null)&&(time != nextTime)) {
            locationService.add(time, current);
            return new GraphModel<>(current.size());
        }
        return current;
    }

    private double getNextTime(double time, String[] row) {
        if (Double.isNaN(time)) {
            if (row.length != 1) {
                throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
            }
        }
        if (row.length == 1) {
            return Double.parseDouble(row[0]);
        }
        return time;
    }

    private void doTemporalMonitoring(MoonLightTemporalScript temporal) throws IOException {
        if (parameters.containsKey(DOMAIN)) {
            showStartActivityMessage(String.format(SELECTING_SIGNAL_DOMAIN,parameters.get(DOMAIN)));
            temporal.setMonitoringDomain(getDomain(parameters.get(DOMAIN)));
            showActivityCompletedMessage();
        }
        TemporalScriptComponent<?> component;
        if (parameters.containsKey(MONITOR)) {
            component = temporal.selectTemporalComponent(parameters.get(MONITOR));
            showStartActivityMessage(String.format(SELECTING_SCRIPT_COMPONENT,parameters.get(MONITOR)));
        } else {
            component = temporal.selectDefaultTemporalComponent();
            showStartActivityMessage(SELECTING_DEFAULT_SCRIPT_COMPONENT);
        }
        Signal<MoonLightRecord> signal = loadTemporalSignal(component.getSignalHandler(),parameters.getOrDefault(INPUT,"input.csv"));
        showStartActivityMessage(MONITORING_STARTED);
        long startingTime = System.currentTimeMillis();
        double[][] result = component.monitorToArray(signal,scriptArgs);
        long elapsed = System.currentTimeMillis()-startingTime;
        showActivityCompletedMessage();
        showMessage(String.format("Monitoring time: %.3fs",elapsed/1000.0));
        writeTemporalResultsToFile(result,parameters.getOrDefault(OUTPUT, "output.csv"));
    }

    private void writeTemporalResultsToFile(double[][] result, String output) throws FileNotFoundException {
        showStartActivityMessage(String.format(SAVING_RESULTS,output));
        PrintWriter writer = new PrintWriter(output);
        for (double[] row : result) {
            writer.println(String.format("%f;%f", row[0], row[1]));
        }
        writer.close();
        showActivityCompletedMessage();
    }

    private void writeSpatialTemporalResultsToFile(double[][][] result, String output) throws FileNotFoundException {
        showStartActivityMessage(String.format(SAVING_RESULTS,output));
        PrintWriter writer = new PrintWriter(output);
        int counter = 0;
        boolean flag = true;
        while (flag) {
            flag = false;
            for(int i=0;i<result.length;i++) {
                String beginning = (i==0?"":";");
                if (counter<result[i].length) {
                    writer.println(String.format("%s%f;%f",beginning,result[i][counter][0],result[i][counter][1]));
                } else {
                    writer.println(String.format("%s;",beginning));
                }
                flag = flag | (counter+1<result[i].length);
            }
            counter++;
        }
        writer.close();
        showActivityCompletedMessage();
    }

    private void showMessage(String format) {
        out.println(format);
    }

    private SignalDomain<?> getDomain(String domainName) {
        if ("boolean".equals(domainName)) {
            return new BooleanDomain();
        }
        if ("minmax".equals(domainName)) {
            return new DoubleDomain();
        }
        throw new IllegalArgumentException(String.format(UNKNOWN_DOMAIN, domainName));
    }

    private Signal<MoonLightRecord> loadTemporalSignal(RecordHandler handler, String fileName) throws IOException {
        showStartActivityMessage(String.format(LOADING_SIGNAL_MESSAGE, fileName));
        List<String[]> rows = Files.readAllLines(Paths.get(fileName)).stream().map(l -> l.split(";")).collect(Collectors.toList());
        if (rows.stream().anyMatch(r -> r.length != handler.size()+1)) {
            throw new IllegalArgumentException("Signal CSV file is malformed (wrong number of elements in a row)!");
        }
        Signal<MoonLightRecord> signal = new Signal<>();
        for (String[] row: rows) {
            double[] values = Arrays.stream(row).mapToDouble(Double::parseDouble).toArray();
            signal.add(values[0],handler.fromDoubleArray(1,values.length));
        }
        showActivityCompletedMessage();
        return signal;
    }

    private MoonLightScript loadScript(String fileName) throws IOException, MoonLightScriptLoaderException {
        showStartActivityMessage(String.format(LOADING_SCRIPT_MESSAGE, fileName));
        MoonLightScript script = ScriptLoader.loadFromFile(fileName);
        showActivityCompletedMessage();
        showActivityCompletedMessage();
        return script;
    }

    private void showActivityCompletedMessage() {
        out.println(" DONE!");
    }

    private void showStartActivityMessage(String message) {
        out.print(message);
        out.flush();
    }


}