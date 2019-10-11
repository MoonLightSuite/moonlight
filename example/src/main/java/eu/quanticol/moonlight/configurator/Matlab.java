package eu.quanticol.moonlight.configurator;

import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;

public class Matlab {


    public static MatlabProxy startMatlab() {
        MatlabProxyFactoryOptions.Builder options = new MatlabProxyFactoryOptions.Builder();
        options.setUsePreviouslyControlledSession(true);
        try {
            MatlabProxy proxy = new MatlabProxyFactory(options.build()).getProxy();
            proxy.eval(String.format("addpath(genpath('%s'))", Configurator.STALIRO_PATH));
            proxy.eval(String.format("addpath(genpath('%s'))", Configurator.BREACH_PATH));
            proxy.eval(String.format("addpath(genpath('%s'))", Configurator.UTILITY_PATH));
            return proxy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
