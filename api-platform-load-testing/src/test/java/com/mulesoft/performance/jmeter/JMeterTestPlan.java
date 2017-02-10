package com.mulesoft.performance.jmeter;

/**
 * Created by axelsirota on 1/26/17.
 */

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class JMeterTestPlan {

    private HashMap<String, String> HTTPParameters;
    private HashMap<String, String> logParameters;
    private HashMap<String, Integer> ThreadParameters;
    final private Queue<Header> headerQueue;
    private Integer port;
    private File jmeterProperties;
    private String jMeterHome;
    private StandardJMeterEngine engine;
    private  HTTPSamplerProxy proxyToHit;
    private LoopController loopController;
    private ThreadGroup threadGroup;
    private TestPlan testPlan;
    private HeaderManager headerManager;

    public JMeterTestPlan(String jMeterHome) {
        this.jMeterHome = jMeterHome;
        this.jmeterProperties = new File(this.jMeterHome+ "/bin/jmeter.properties");
        this.initializeEngine();
        this.headerQueue = new LinkedList<Header>();
    }

    private void initializeEngine() {
        if (!this.jmeterProperties.exists()) {
            System.exit(1);
        }
        this.engine = new StandardJMeterEngine();
        JMeterUtils.setJMeterHome(this.jMeterHome);
        JMeterUtils.loadJMeterProperties(this.jmeterProperties.getPath());
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();
    }

    public void setHTTPParameters(Integer port, String hostIP, String method, String path) {
        this.HTTPParameters = new HashMap<String, String>();
        this.HTTPParameters.put("Server", hostIP);
        this.HTTPParameters.put("UrlPath", path);
        this.HTTPParameters.put("Method", method);
        this.port = port;
    }

    public void setThreadGroupParameters (Integer threadCount, Integer rampUpTime, Integer durationTime) {
        this.ThreadParameters = new HashMap<String, Integer>();
        this.ThreadParameters.put("Threads", threadCount);
        this.ThreadParameters.put("RampUp", rampUpTime);
        this.ThreadParameters.put("Duration",durationTime);
    }

    public void setLoggingParameters (String logName, String jmxName) {
        this.logParameters = new HashMap<String, String>();
        this.logParameters.put("LogName", logName);
        this.logParameters.put("JMXName", jmxName);
    }

    private void setHTTPProxy() {
        this.proxyToHit = new HTTPSamplerProxy();
        this.proxyToHit.setName("HTTP Request");
        this.proxyToHit.setEnabled(true);
        this.proxyToHit.setPostBodyRaw(true);
        this.proxyToHit.setDomain(this.HTTPParameters.get("Server"));
        this.proxyToHit.setPort(this.port);
        this.proxyToHit.setPath(this.HTTPParameters.get("UrlPath"));
        this.proxyToHit.setMethod(this.HTTPParameters.get("Method"));
        this.proxyToHit.setFollowRedirects(true);
        this.proxyToHit.setAutoRedirects(false);
        this.proxyToHit.setUseKeepAlive(true);
        this.proxyToHit.setDoMultipartPost(false);
        this.proxyToHit.setImplementation("HttpClient4");
        this.proxyToHit.setMonitor(false);
        this.proxyToHit.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        this.proxyToHit.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
    }

    private void setLoopController() {
        // Loop Controller
        this.loopController = new LoopController();
        this.loopController.initialize();
        this.loopController.setLoops(-1);
        this.loopController.setEnabled(true);
        this.loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        this.loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        this.loopController.setName("Loop Controller");
    }

    private void setThreadGroup() {
        this.threadGroup = new ThreadGroup();
        this.threadGroup.setName("Thread Group");
        this.threadGroup.setNumThreads(ThreadParameters.get("Threads"));
        this.threadGroup.setRampUp(ThreadParameters.get("RampUp"));
        this.threadGroup.setDuration(ThreadParameters.get("Duration"));
        this.threadGroup.setDelay(0);
        this.threadGroup.setScheduler(true);
        this.threadGroup.setEnabled(true);
        this.threadGroup.setProperty("ThreadGroup.on_sample_error", "continue");
        this.setLoopController();
        this.threadGroup.setSamplerController(this.loopController);
        this.threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        this.threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
    }

    private void setTestPlan() {
        // Test Plan
        this.testPlan = new TestPlan("Post Test Plan");
        this.testPlan.setEnabled(true);
        this.testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        this.testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        this.testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
        this.testPlan.setFunctionalMode(false);
        this.testPlan.setSerialized(false);
    }

    public void setHeader(String name, String value) {
        Header header = new Header();
        header.setName(name);
        header.setValue(value);
        this.headerQueue.add(header);
    }

    private void setHeaderManager() {
        // Set headers
        this.headerManager = new HeaderManager();
        this.headerManager.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        this.headerManager.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        this.headerManager.setName("HTTP Header Manager");
        this.headerManager.setEnabled(true);
        while (!this.headerQueue.isEmpty()) {
            this.headerManager.add(this.headerQueue.remove());
        }
    }

    public HashMap<String, Double> runJmeter() throws Exception {

        // JMeter Test Plan, basically JOrphan HashTree
        this.setHTTPProxy();
        this.setThreadGroup();
        this.setTestPlan();
        this.setHeaderManager();

        HashTree requestHashTree = new HashTree();
        requestHashTree.add(this.proxyToHit, this.headerManager);
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(this.testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(this.testPlan, this.threadGroup);
        threadGroupHashTree.add(requestHashTree);


        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Store execution results into a .jtl file
        String logFile = this.logParameters.get("LogName");
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        threadGroupHashTree.add(threadGroupHashTree.getArray()[0], logger);

        // Collector for plugin
//
        MuleCollector collector = new MuleCollector(ThreadParameters.get("Threads"), this.logParameters.get("LogName"));
        threadGroupHashTree.add(threadGroupHashTree.getArray()[0],collector);

        // Run Test Plan
        this.engine.configure(testPlanTree);
        SaveService.saveTree(testPlanTree, new FileOutputStream(this.logParameters.get("JMXName")));
        this.engine.run();
        return collector.getSlaResults();
    }
}
