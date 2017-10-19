package com.thomascookonline.datachallenge;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FileReader {

    List<String> jsonObjects = new ArrayList<String>();

    public FileReader(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new java.io.FileReader(filePath));
        String thisLine = null;
        while ((thisLine = br.readLine()) != null) {
            try {
                JSONObject jsonObj = new JSONObject(thisLine);
                jsonObjects.add(thisLine);
            } catch (Exception e) {
                Logger.getAnonymousLogger().info("Row is not json");
            }
        }
    }

    public List<String> getJsonObjects() {
        return jsonObjects;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            long numberOfJsons = 0;
            long numberOfErrorJsons = 0;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("datachallenge.csv"))) {
                for(String filename : listFilesForFolder(new File(args[0]))){
                    HashMap<String, Object> results = (HashMap<String, Object>) checkFileForErrors(filename);
                    List<String> errorMessages = (List<String>) results.get("listOfErrors");
                    numberOfJsons = numberOfJsons + (long)results.get("numberOfJSons");
                    numberOfErrorJsons = numberOfErrorJsons + (long)results.get("numberOfErrorJson");
                    for(String errorMessage : errorMessages){
                        bw.write(errorMessage + "," + filename);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(numberOfErrorJsons);
            System.out.println(numberOfJsons);
        }
    }

    static List<String> listFilesForFolder(final File folder) {
        List<String> filesList = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
                filesList.add(fileEntry.getAbsolutePath());
                //checkFileForErrors();
            }
        }
        return filesList;
    }

    static Map<String, Object> checkFileForErrors(String fileName) {
        String thisLine;
        Map<String,Object> results = new HashMap<>();
        List<String> result = new ArrayList<>();
        //String jsonpathCaptureUrnPath = "$..capture.urn";
        String branch = "$['customer']['identity']['branchBudgetCentre']";
        String consultationReference = "$.consultation.reference";
        String customerReferenceNumber = "$.customer.identity.customerReferenceNumber";
        String requestUrn = "$.customer.identity.capture.urn";
        String previousLine = "";
        long incorrectJson = 0;
        long jsonCount = 0;
        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(fileName));

            while ((thisLine = br.readLine()) != null) {
                try {
                    JSONObject jsonObj = new JSONObject(thisLine);
                    jsonCount++;
                    DocumentContext jsonContext = JsonPath.parse(thisLine);
                    int branchValue = jsonContext.read(branch);
                    String consultationReferenceValue = jsonContext.read(consultationReference);
                    int customerReferenceNumberValue = jsonContext.read(customerReferenceNumber);
                    String urn = "urn:WR:" + branchValue + ":" + consultationReferenceValue.split("/")[1] + ":" + customerReferenceNumberValue;
                    String requestUrnValue = jsonContext.read(requestUrn);
                    if (!urn.equals(requestUrnValue)) {
                        if(previousLine.contains("Request JSON:")) {
                            System.out.println(thisLine);
                            System.out.println(String.format("Items %s and %s are not equal", urn, requestUrnValue));
                            result.add(urn + "," + requestUrnValue);
                        }
                        incorrectJson++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                previousLine = thisLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        results.put("listOfErrors", result);
        results.put("numberOfJSons", jsonCount);
        results.put("numberOfErrorJson", incorrectJson);
        return results;
    }
}
