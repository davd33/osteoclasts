package org.osteo.scripts.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.imp.ApplyClassifierScript;
import org.osteo.scripts.imp.ChartScript;
import org.osteo.scripts.imp.CountCellsScript;
import org.osteo.scripts.imp.LabelScript;
import org.osteo.scripts.imp.PitScript;
import org.osteo.scripts.imp.SpidersScript;
import org.osteo.scripts.imp.TrainClassifierScript;
import org.osteo.scripts.imp.TrainingDataScript;

public enum Available {

    SPIDERS(
            "Deploy spiders", "(-ds|--deploy-spiders)",
            "-ds or --deploy-spiders		Deploy spiders to detect local regions",
            SpidersScript.class), 
    
    PIC(
            "Particle analyser", "(-pa|--particle-analysis)",
            "-pa or --particle-analysis		Apply the ImageJ particle analysis algorithm on the selected images",
            PitScript.class), 
    
    TRAIN_CLASSIFIER(
            "Train classifier", "(-tc|--train-classifier)",
            "-tc or --train-classifier		Train the classification for all the selected and labelled images",
            TrainClassifierScript.class), 
    
    APPLY_CLASSIFIER(
            "Apply classifier", "(-ac|--apply-classifier)",
            "-ac or --apply-classifier		Apply a classification for all the selected images",
            ApplyClassifierScript.class), 
    
    LABEL(
            "Create binaries", "(-bn|--binaries)",
            "-bn or --binaries			Create binary images from labelled images",
            LabelScript.class), 
    
    CHART(
            "Draw charts", "(-ch|--charts)",
            "-ch or --charts				Compute images and draw charts",
            ChartScript.class), 
    
    CNTCELL(
            "Count cells", "(-cc|--count-cells)",
            "-cc or --count-cells			Count cells in a probability image",
            CountCellsScript.class), 
    
    TRAINING_DATA(
            "Create training data", "(-td|--training-data)",
            "-td or --training-data			Create new image for the training",
            TrainingDataScript.class);
    
    
    private String name;
    private Class<? extends AbstractScript> type;
    private String shellPattern;
    private String description;

    public String getDescription() {
        return description;
    }

    public String getShellPattern() {
        return shellPattern;
    }

    public String getName() {
        return name;
    }

    public Class<? extends AbstractScript> getType() {
        return type;
    }

    Available(String name, String shellPattern, String description,
            Class<? extends AbstractScript> type) {
        this.name = name;
        this.type = type;
        this.shellPattern = shellPattern;
        this.description = description;
    }

    public static String[] options() {
        Available[] values = Available.values();
        String[] names = new String[values.length];
        for (int n = 0; n < names.length; n++) {
            names[n] = values[n].shellPattern;
        }
        return names;
    }

    public static String[] descriptions() {
        Available[] values = Available.values();
        String[] names = new String[values.length];
        for (int n = 0; n < names.length; n++) {
            names[n] = values[n].description;
        }
        return names;
    }

    public static String className(String scriptName) {
        try {
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            String path = "org" + File.separator + "osteo" + File.separator
                    + "scripts" + File.separator + "imp";
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                String resource = resources.nextElement().getFile();
                resource = URLDecoder.decode(resource, "utf-8");

                File[] classFiles = new File(resource).listFiles();
                for (int i = 0; i < classFiles.length; i++) {
                    if (!classFiles[i].getName().contains("$")) {
                        String className = "org.osteo.scripts.imp."
                                + classFiles[i].getName().replaceAll(
                                "\\.class", "");
                        AbstractScript script = (AbstractScript) Class.forName(
                                className).newInstance();
                        if (script.name().equals(scriptName)) {
                            return script.getClass().getName();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (InstantiationException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (IllegalAccessException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        }

        return null;
    }

    public static String[] names() {
        List<String> namesList = new ArrayList<String>();

        try {
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            String path = "org" + File.separator + "osteo" + File.separator
                    + "scripts" + File.separator + "imp";
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                String resource = resources.nextElement().getFile();
                resource = URLDecoder.decode(resource, "utf-8");

                File[] classFiles = new File(resource).listFiles();
                for (int i = 0; i < classFiles.length; i++) {
                    if (!classFiles[i].getName().contains("$")) {
                        String className = "org.osteo.scripts.imp."
                                + classFiles[i].getName().replaceAll(
                                "\\.class", "");
                        AbstractScript script = (AbstractScript) Class.forName(
                                className).newInstance();
                        namesList.add(script.name());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (InstantiationException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (IllegalAccessException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to get scripts names (" + e.getMessage()
                    + ", " + e.getClass().getSimpleName() + ")");
        }

        return namesList.toArray(new String[0]);
    }
}
