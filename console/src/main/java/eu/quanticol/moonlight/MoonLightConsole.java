package eu.quanticol.moonlight;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.io.TemporalSignalWriter;
import eu.quanticol.moonlight.signal.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class MoonLightConsole {

    private static final String LOADING_SCRIPT_MESSAGE = "Loading script from %s";
    private static final String IS_A_TEMPORAL_SCRIPT = "%s is a temporal script...";
    private static final String IS_A_SPATIAL_SCRIPT = "%s is a spatio temporal script...";
    private static final String LOADING_SIGNAL_MESSAGE = "Loading signal from %s";
    private static final String SELECTING_SCRIPT_COMPONENT = "Monitor \"%s\" has been selected.";
    private static final String MONITOR_INSTANTIATED = "Monitor \"%s\" instantiated with parameters %s.";
    private static final String MONITORING_STARTED = "Monitoring started...";
    private static final String SAVING_RESULTS = "Saving results in %s";
    private String inputFile;
    private String signalInput;
    private String inputFormat;
    private String monitor;
    private String domainName;
    private SignalDomain<?> domain;
    private String[] args;
    private File outputFile;
    private boolean isHelp;
    private final PrintStream out;
    private final PrintStream err;

    public MoonLightConsole() {
        this(System.out, System.err);
    }

    public MoonLightConsole(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
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
    }

    private void parseArgs(String[] args) {
        this.inputFile = args[0];
    }

    private void execute() throws IOException {
//        if (this.isHelp) {
//            this.help();
//        } else {
//            this.doMonitoring();
//        }
        showStartActivityMessage(String.format(LOADING_SCRIPT_MESSAGE,"./script.mls"));
        showActivityCompletedMessage();
        showMessage(String.format(IS_A_TEMPORAL_SCRIPT,"./script.mls"));
        showStartActivityMessage(String.format(SELECTING_SCRIPT_COMPONENT,"amonitor"));
        showActivityCompletedMessage();
        showMessage(String.format(MONITOR_INSTANTIATED,"amonitor","[0.1,2.5]"));
        showStartActivityMessage(String.format(LOADING_SCRIPT_MESSAGE,"./inputsignal.csv"));
        showActivityCompletedMessage();
        showStartActivityMessage(MONITORING_STARTED);
        showActivityCompletedMessage();
        showMessage(String.format("Monitoring time: %.3fs",0.453));
        showStartActivityMessage(String.format(SAVING_RESULTS,"./result.csv"));
        showActivityCompletedMessage();
    }

    private void doMonitoring() throws IOException {
//        showStartActivityMessage(LOADING_SCRIPT_MESSAGE,"script.mls");
        //        MoonLightScript script = loadScript();
//        if (script == null) {
//            showErrorMessage("Error while loading the script!");
//            return ;
//        }
//        if (script.isTemporal()) {
//            doTemporalMonitoring(script.temporal());
//        } else {
//            doSpatialTemporalMonitoring(script.spatialTemporal());
//        }
    }

    private void showErrorMessage(String message) {
        err.println(message);
    }

    private void doSpatialTemporalMonitoring(MoonLightSpatialTemporalScript spatialTemporal) {
        spatialTemporal.setMonitoringDomain(getDomain());
        //SpatialTemporalScriptComponent<?> component = spatialTemporal.selectSpatialTemporalComponent(this.monitor);
        showMessage(String.format("Monitoring domain: %s",domainName));
        Signal<MoonLightRecord> signal = loadTemporalSignal(null);//component.getSignalHandler());
        showStartActivityMessage("Monitoring started...");
        //component.monitorToFile(getTemporalSignalWriter(),outputFile,signal,args);
        showActivityCompletedMessage();
        showMessage(String.format("Monitored signal saved at %s",outputFile.getName()));
    }

    private void doTemporalMonitoring(MoonLightTemporalScript temporal) throws IOException {
        temporal.setMonitoringDomain(getDomain());
        TemporalScriptComponent<?> component = temporal.selectTemporalComponent(this.monitor);
        showMessage(String.format("Monitoring domain: %s",domainName));
        Signal<MoonLightRecord> signal = loadTemporalSignal(component.getSignalHandler());
        showStartActivityMessage("Monitoring started...");
        //component.monitorToFile(getTemporalSignalWriter(),outputFile,signal,args);
        showActivityCompletedMessage();
        showMessage(String.format("Monitored signal saved at %s",outputFile.getName()));
    }

    private TemporalSignalWriter getTemporalSignalWriter() {
        return null;
    }

    private void showMessage(String format) {
        out.println(format);
    }

    private SignalDomain<?> getDomain() {
        return domain;
    }

    private Signal<MoonLightRecord> loadTemporalSignal(RecordHandler temporal) {
        showStartActivityMessage(String.format("Loading signal %s",signalInput));
        Signal<MoonLightRecord> signal = null;
        showActivityCompletedMessage();
        return signal;
    }

    private MoonLightScript loadScript() {
        showStartActivityMessage(String.format(LOADING_SCRIPT_MESSAGE,inputFile));
        //Load script!
        showActivityCompletedMessage();
        return null;
    }

    private void showActivityCompletedMessage() {
        out.println(" DONE!");
    }

    private void showStartActivityMessage(String message) {
        out.print(message);
        out.flush();
    }


}