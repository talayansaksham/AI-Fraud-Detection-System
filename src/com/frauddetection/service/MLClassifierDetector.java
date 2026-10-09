package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class MLClassifierDetector implements FraudDetector {
    private Classifier classifier;
    private Instances trainingHeader;
    private boolean ready = false;

    public MLClassifierDetector(String arffFilePath) {
        loadModel(arffFilePath);
    }

    private void loadModel(String arffFilePath) {
        try {
            java.io.File file = new java.io.File(arffFilePath);
            if (!file.exists()) {
                java.io.File resFile = new java.io.File("resources", arffFilePath);
                if (resFile.exists()) {
                    file = resFile;
                }
            }

            Instances data;
            if (file.exists()) {
                DataSource source = new DataSource(file.getAbsolutePath());
                data = source.getDataSet();
            } else {
                java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(arffFilePath);
                if (in == null) {
                    in = getClass().getResourceAsStream("/" + arffFilePath);
                }
                if (in != null) {
                    DataSource source = new DataSource(in);
                    data = source.getDataSet();
                } else {
                    throw new java.io.FileNotFoundException("Could not locate ARFF file: " + arffFilePath);
                }
            }

            data.setClassIndex(data.numAttributes() - 1);
            Classifier j48 = new J48();
            j48.buildClassifier(data);
            this.classifier = j48;
            this.trainingHeader = new Instances(data, 0);
            this.ready = true;
            System.out.println("MLClassifierDetector: J48 model trained successfully.");
        } catch (Throwable e) {
            System.err.println("MLClassifierDetector could not train model: " + e.getMessage());
            this.ready = false;
        }
    }

    @Override
    public String check(Transaction txn) {
        if (!ready) return null;
        try {
            Instance instance = new DenseInstance(trainingHeader.numAttributes());
            instance.setDataset(trainingHeader);
            instance.setValue(0, txn.getAmount());
            instance.setValue(1, txn.getTimestamp().getHour());
            instance.setValue(2, "Unknown-VPN".equalsIgnoreCase(txn.getLocation()) ? 1.0 : 0.0);
            double predictedIndex = classifier.classifyInstance(instance);
            String predictedLabel = trainingHeader.classAttribute().value((int) predictedIndex);
            if ("fraud".equalsIgnoreCase(predictedLabel)) {
                return "ML classifier (J48) predicted this transaction as fraud";
            }
        } catch (Exception e) {
            System.err.println("MLClassifierDetector prediction failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getName() { return "ML Classifier (J48)"; }

    /** Re-train the classifier from a fresh ARFF file. Called by the Algorithm Management panel. */
    public void reloadModel(String arffFilePath) {
        loadModel(arffFilePath);
        if (!ready) {
            throw new RuntimeException("ML model reload failed. Check console for details.");
        }
    }
}
