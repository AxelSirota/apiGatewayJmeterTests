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

import org.junit.Test;

public class ExecuteJMeterTestPlan {

    @Test
    public void main() throws Exception {
        HashMap<String, Integer> ThreadParameters = new HashMap<String, Integer>();
        ThreadParameters.put("Threads", 10);
        ThreadParameters.put("RampUp", 1);
        ThreadParameters.put("Duration",10);

        HashMap<String, String> HTTPParameters = new HashMap<String, String>();
        HTTPParameters.put("Server", "23.21.206.49");
//        HTTPParameters.put("Port", 8887);
        HTTPParameters.put("UrlPath", "/api");
        HTTPParameters.put("Method", "GET");


        File jmeterProperties = new File("/usr/local/Cellar/jmeter/3.1/libexec/bin/jmeter.properties");
        if (jmeterProperties.exists()) {
            //JMeter Engine
            StandardJMeterEngine jmeter = new StandardJMeterEngine();

            //JMeter initialization (properties, log levels, locale, etc)
            JMeterUtils.setJMeterHome("/usr/local/Cellar/jmeter/3.1/libexec");
            JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
            JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
            JMeterUtils.initLocale();

            // JMeter Test Plan, basically JOrphan HashTree
            HashTree testPlanTree = new HashTree();

            HTTPSamplerProxy proxyToHit = new HTTPSamplerProxy();
            proxyToHit.setName("HTTP Request");
            proxyToHit.setEnabled(true);
            proxyToHit.setPostBodyRaw(true);
            proxyToHit.setDomain(HTTPParameters.get("Server"));
            proxyToHit.setPort(8887);//HTTPParameters.get("Port"));
            proxyToHit.setPath(HTTPParameters.get("UrlPath"));
            proxyToHit.setMethod(HTTPParameters.get("Method"));
            proxyToHit.setFollowRedirects(true);
            proxyToHit.setAutoRedirects(false);
            proxyToHit.setUseKeepAlive(true);
            proxyToHit.setDoMultipartPost(false);
            proxyToHit.setImplementation("HttpClient4");
            proxyToHit.setMonitor(false);
            proxyToHit.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
            proxyToHit.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());


            // Loop Controller
            LoopController loopController = new LoopController();
            loopController.initialize();
            loopController.setLoops(-1);
            loopController.setEnabled(true);
            loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
            loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
            loopController.setName("Loop Controller");

            // Thread Group
            ThreadGroup threadGroup = new ThreadGroup();
            threadGroup.setName("Thread Group");
            threadGroup.setNumThreads(ThreadParameters.get("Threads"));
            threadGroup.setRampUp(ThreadParameters.get("RampUp"));
            threadGroup.setDuration(ThreadParameters.get("Duration"));
            threadGroup.setDelay(0);
            threadGroup.setScheduler(true);
            threadGroup.setEnabled(true);
            threadGroup.setProperty("ThreadGroup.on_sample_error", "continue");


            threadGroup.setSamplerController(loopController);

            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

            // Test Plan
            TestPlan testPlan = new TestPlan("Post Test Plan");
            testPlan.setEnabled(true);
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
            testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
            testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
            testPlan.setFunctionalMode(false);
            testPlan.setSerialized(false);

            // Set headers

            HeaderManager headerManager = new HeaderManager();
            headerManager.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
            headerManager.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
            headerManager.setName("HTTP Header Manager");
            headerManager.setEnabled(true);

            Header header = new Header();
            header.setName("Content-Type");
            header.setValue("text/plain");

            headerManager.add(header);

            // Construct Test Plan from previously initialized elements

            testPlanTree.add(testPlan);
            HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
            threadGroupHashTree.add(proxyToHit, headerManager);

            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }

            // Store execution results into a .jtl file
            String logFile = "example.jtl";
            ResultCollector logger = new ResultCollector(summer);
            logger.setFilename(logFile);
            testPlanTree.add(testPlanTree.getArray()[0], logger);

            // Collector for plugin
//
            MuleCollector collector = new MuleCollector(10);
            testPlanTree.add(testPlanTree.getArray()[0],collector);

            // Run Test Plan
            jmeter.configure(testPlanTree);
            SaveService.saveTree(testPlanTree, new FileOutputStream("example.jmx"));
            jmeter.run();

            System.exit(0);


        }
    }
}
