package eu.quanticol.moonlight.amt;

import factory.StlFactory;
import jamtSignal.interpolation.InterpolationType;
import jamtType.number.JamtFloatType;
import logging.YJLog;
import org.testng.Assert;
import vcdCompiler.InputTraceType;
import xStlCompiler.DiagnosticsReportGenerator;
import xStlCompiler.XStlCompiler;
import xStlCompiler.XStlContext;
import xStlCompiler.dto.XStlAssertion;

import java.io.File;
import java.io.FileWriter;

public class Howto {
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
}
