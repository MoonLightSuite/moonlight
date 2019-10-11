/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018 
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.quanticol.moonlight.examples.example1;

import batch.CommandArguments;
import com.beust.jcommander.JCommander;
import diagnostics.DiagnosticsType;
import factory.StlFactory;
import jamtSignal.interpolation.InterpolationType;
import jamtType.number.JamtFloatType;
import logging.YJLog;
import measurement.Measurement;
import org.testng.Assert;
import vcdCompiler.InputTraceType;
import xStlCompiler.AssertionReportGenerator;
import xStlCompiler.DiagnosticsReportGenerator;
import xStlCompiler.XStlCompiler;
import xStlCompiler.XStlContext;
import xStlCompiler.dto.XStlAssertion;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class Main {

    public static void main(String[] argv) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        System.out.println("STABILIZATION");
        File stlProperty = new File(classLoader.getResource("bounded-stabilization/stabilization.stl").getFile());
        File alias = new File(classLoader.getResource("bounded-stabilization/stabilization.alias").getFile());
        File clock = new File(classLoader.getResource("bounded-stabilization/stabilization-extended.vcd").getFile());
        amt(stlProperty, alias, clock);

        System.out.println("CLOCK");
        stlProperty = new File(classLoader.getResource("clock-jitter/clock-jitter.stl").getFile());
        alias = new File(classLoader.getResource("clock-jitter/clock-jitter.alias").getFile());
        clock = new File(classLoader.getResource("clock-jitter/clock.vcd").getFile());
        amt(stlProperty, alias, clock);
    }

    private static void amt(File stlProperty, File alias, File clock) {
        StlFactory.init(JamtFloatType.REAL, InterpolationType.STEP);
        //XStlMeasurement comp = new XStlMeasurement();

        XStlCompiler comp = new XStlCompiler(true);
        String stlPropFile = stlProperty.getAbsolutePath();
        String aliasFile = alias.getAbsolutePath();
        String vcdFile = clock.getAbsolutePath();
        comp.setShowCompilerOutput(true);
        try {
            comp.compile(stlPropFile, vcdFile, InputTraceType.VCD, aliasFile);
            if (!comp.isErrorFound()) {
                comp.evaluate();
                comp.diagnose();
                for (XStlAssertion a : comp.getAssertions()) {
                    //YJLog.logg.info("Assertion {} verdict: {}", a.getName(), a.getVerdict());
                    System.out.println("Assertion: " + a.getName() + " Verdict: " + a.getVerdict());
                }
            } else {
                YJLog.logg.error("ERRORS FOUND: ");
                YJLog.logg.error(comp.getErrors().toString());
                System.out.println(comp.getErrors().toString());
                Assert.fail();
            }
            FileWriter writer = new FileWriter("ao");
            DiagnosticsReportGenerator aa = new DiagnosticsReportGenerator(writer);
            XStlContext visit = aa.visit(comp.getXstlSpec(), comp.getContext(), true);
            System.out.println("");


        } catch (Exception var7) {
            var7.printStackTrace();
            Assert.fail();
        }
    }

    public static void innerMain() {
        // java -jar ../../amt.jar -x clock-jitter.stl -a clock-jitter.alias -s clock.vcd -m clock-measurements.csv
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File stlProperty = new File(classLoader.getResource("clock-jitter/clock-jitter.stl").getFile());
        File alias = new File(classLoader.getResource("clock-jitter/clock-jitter.alias").getFile());
        File clock = new File(classLoader.getResource("clock-jitter/clock.vcd").getFile());

        String[] args = new String[6];
        args[0] = "-x";
        args[1] = stlProperty.getAbsolutePath();
        args[2] = "-a ";
        args[3] = alias.getAbsolutePath();
        args[4] = "-s";
        args[5] = clock.getAbsolutePath();

        CommandArguments arguments = new CommandArguments();
        JCommander commander = new JCommander(arguments, args);
        if (args.length == 0) {
            commander.usage();
        } else if (arguments.getStlFile().isEmpty()) {
            System.out.println("The input xSTL specification file is missing.");
        } else if (arguments.getSimFile().isEmpty()) {
            System.out.println("The input simulation file is missing.");
        } else {
            String simulationFilename = arguments.getSimFile();
            String xstlFilename = arguments.getStlFile();
            String aliasFilename = arguments.getAliasFile();
            InputTraceType simulationType = InputTraceType.CSV;
            if (!simulationFilename.endsWith(".csv") && !simulationFilename.endsWith(".vcd")) {
                System.out.println("The simulation file '" + simulationFilename + "' does not have a valid (.csv or .vcd) extension.");
            } else {
                if (simulationFilename.endsWith(".vcd")) {
                    simulationType = InputTraceType.VCD;
                }

                StlFactory.init(arguments.getFpType(), arguments.getInterpolationType());
                XStlCompiler comp = new XStlCompiler();
                comp.setShowCompilerOutput(true);

                try {
                    if (aliasFilename.isEmpty()) {
                        comp.compile(xstlFilename, simulationFilename, simulationType);
                    } else {
                        comp.compile(xstlFilename, simulationFilename, simulationType, aliasFilename);
                    }

                    if (!comp.isErrorFound()) {
                        comp.evaluate();
                    }

                    String verdictReportFilename = arguments.getVerdictReportFilename();
                    if (!verdictReportFilename.isEmpty()) {
                        FileWriter writer = new FileWriter(verdictReportFilename);
                        AssertionReportGenerator assertionReport = new AssertionReportGenerator(writer);
                        assertionReport.visit(comp.getXstlSpec(), comp.getContext(), true);
                        writer.flush();
                        writer.close();
                    }

                    String measurementsFilename = arguments.getMeasurementsReportFilename();
                    Iterator var14;
                    if (!measurementsFilename.isEmpty()) {
                        FileWriter writer = new FileWriter(measurementsFilename);
                        writer.write("Name");
                        writer.write(", ");
                        writer.write("Number of segments");
                        writer.write(", ");
                        writer.write("Minimum value");
                        writer.write(", ");
                        writer.write("Maximum value");
                        writer.write(", ");
                        writer.write("Average value");
                        writer.write("\n");
                        XStlContext context = comp.getContext();
                        List<Measurement> measurementList = context.getMeasurements();
                        var14 = measurementList.iterator();

                        while (var14.hasNext()) {
                            Measurement measurement = (Measurement) var14.next();
                            writer.write(measurement.getName());
                            writer.write(", ");
                            writer.write(Integer.toString(measurement.getSegmentList().size()));
                            writer.write(", ");
                            writer.write(measurement.getMin().toString());
                            writer.write(", ");
                            writer.write(measurement.getMax().toString());
                            writer.write(", ");
                            writer.write(measurement.getAverage().toString());
                            writer.write("\n");
                        }

                        writer.flush();
                        writer.close();
                    }

                    DiagnosticsType diagnosticsType = arguments.getDiagnosticsType();
                    String diagnosticsFilename = arguments.getDiagnosticsReportFilename();
                    boolean violation;
                    XStlAssertion a;
                    FileWriter writer;
                    DiagnosticsReportGenerator diagnosticsReport;
                    if (diagnosticsType == DiagnosticsType.SINGLE) {
                        violation = false;
                        var14 = comp.getAssertions().iterator();

                        while (var14.hasNext()) {
                            a = (XStlAssertion) var14.next();
//                            switch($SWITCH_TABLE$evaluation$Verdict()[a.getVerdict().ordinal()]) {
//                                case 1:
//                                    violation = true;
//                            }
                        }

                        if (violation) {
                            comp.diagnose();
                            if (!diagnosticsFilename.isEmpty()) {
                                writer = new FileWriter(diagnosticsFilename);
                                diagnosticsReport = new DiagnosticsReportGenerator(writer);
                                diagnosticsReport.visit(comp.getXstlSpec(), comp.getContext(), true);
                                writer.flush();
                                writer.close();
                            }
                        }
                    } else if (diagnosticsType == DiagnosticsType.EPOCH) {
                        violation = false;
                        var14 = comp.getAssertions().iterator();

                        while (var14.hasNext()) {
                            a = (XStlAssertion) var14.next();
//                            switch($SWITCH_TABLE$evaluation$Verdict()[a.getVerdict().ordinal()]) {
//                                case 1:
//                                    violation = true;
//                            }
                        }

                        if (violation) {
                            comp.epoch_diagnose();
                            if (!diagnosticsFilename.isEmpty()) {
                                writer = new FileWriter(diagnosticsFilename);
                                diagnosticsReport = new DiagnosticsReportGenerator(writer);
                                diagnosticsReport.visit(comp.getXstlSpec(), comp.getContext(), true);
                                writer.flush();
                                writer.close();
                            }
                        }
                    }
                } catch (Exception var15) {
                    var15.printStackTrace();
                }

                if (comp.isErrorFound()) {
                    System.err.println("ERRORS FOUND: ");
                    System.err.println(comp.getErrors());
                }

            }
        }
    }
}
