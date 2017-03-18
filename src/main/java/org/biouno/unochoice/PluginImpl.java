package org.biouno.unochoice;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import hudson.Plugin;

public class PluginImpl extends Plugin {

    public static final MetricRegistry REGISTRY = new MetricRegistry();
    public  static Timer TIMERMETRIC = null;

    @Override
    public void start() throws Exception {
        TIMERMETRIC = PluginImpl.REGISTRY.timer("eval");
        final JmxReporter reporter = JmxReporter.forRegistry(REGISTRY).build();
        reporter.start();
    }
    
}
