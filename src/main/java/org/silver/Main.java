package org.silver;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    private final static String USAGE = "Usage: java -jar <path-to-flowdroid-pt> <android-platform-dir> <source-sink-file> <taint-wrapper-file> <apk-file>";
    private static String androidDirPath;
    private static String sourceSinkFilePath;
    private static String taintWrapperFilePath;
    private static String apkFilePath;

    static void parseArgs(String[] args) {
        if (args.length != 4) {
            System.out.println(USAGE);
            return;
        }

        androidDirPath = args[0];
        sourceSinkFilePath = args[1];
        taintWrapperFilePath = args[2];
        apkFilePath = args[3];
    }
    public static void main(String[] args) {
        parseArgs(args);
        try {
            run(apkFilePath);
        } catch (Exception e) {
            System.out.println("Error running Flowdroid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void run(String apkFilePath) throws XmlPullParserException, IOException, URISyntaxException {
        File apk = new File(apkFilePath);
        // Get filename without extension
        String apkName = apk.getName().replaceFirst("[.][^.]+$", "");
        File droidReport = new File("fdrunnerlogs/" + apkName + "-flow-report.xml");

        InfoflowAndroidConfiguration conf = new InfoflowAndroidConfiguration();
        conf.getAnalysisFileConfig().setAndroidPlatformDir(androidDirPath);
        conf.getAnalysisFileConfig().setTargetAPKFile(apkFilePath);
        conf.getAnalysisFileConfig().setSourceSinkFile(sourceSinkFilePath);
        conf.getAnalysisFileConfig().setOutputFile(droidReport.getAbsolutePath());
        conf.getPathConfiguration().setPathReconstructionMode(InfoflowConfiguration.PathReconstructionMode.Precise);
        conf.setLogSourcesAndSinks(true);
        conf.setMergeDexFiles(true);
        conf.getCallbackConfig().setCallbackAnalyzer(InfoflowAndroidConfiguration.CallbackAnalyzer.Fast);
        conf.getCallbackConfig().setEnableCallbacks(true);
        conf.getCallbackConfig().setCallbackAnalysisTimeout(1000);

        SetupApplication setup = new SetupApplication(conf);

        setup.setTaintWrapper(new EasyTaintWrapper(taintWrapperFilePath));
        ResultsAvailableHandler resultsHandler = new ResultsAvailableHandler() {
            @Override
            public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
                // Handle the results
                if (results != null && results.getResults() != null) {
                    for (ResultSinkInfo sink : results.getResults().keySet()) {
                        System.out.println("Sink: " + sink);
                        for (ResultSourceInfo source : results.getResults().get(sink)) {
                            System.out.println("Source: " + source);
                            if (source.getPath() != null) {
                                for (Stmt stmt : source.getPath()) {
                                    System.out.println("Stmt: " + stmt);
                                }
                            }
                        }
                    }
                }
            }
        };

        setup.addResultsAvailableHandler(resultsHandler);
        InfoflowResults results = setup.runInfoflow();
    }
}