package com.thomascookonline.datachallenge;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JsonLogParser {

    private List<String> jsonObjects = new ArrayList<>();

    JsonLogParser(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new java.io.FileReader(filePath));
        String thisLine;
        while ((thisLine = br.readLine()) != null) {
            try {
                jsonObjects.add(thisLine);
            } catch (Exception e) {
                Logger.getAnonymousLogger().info("Row is not a JSON");
            }
        }
    }

    List<String> getJsonObjects() {
        return jsonObjects;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("");
            System.exit(0);
        }

        long numberOfJsons = 0;
        long numberOfIncorrectJsons = 0;
        String[] arrFNames;
        String shortFilename;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("wrong_urns.csv"))) {
            for(String filename : listFilesForFolder(new File(args[0]))){
                HashMap<String, Object> results = (HashMap<String, Object>) checkFileForErrors(filename);
                List<String> errorMessages = (List<String>) results.get("listOfErrors");
                numberOfJsons += (long)results.get("numberOfJSons");
                numberOfIncorrectJsons += (long)results.get("numberOfErrorJson");
                arrFNames = filename.split("\\\\");
                shortFilename = arrFNames[arrFNames.length - 1];
                for(String errorMessage : errorMessages){
                    bw.write(shortFilename + "," + errorMessage);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total number of JSONs: " + numberOfJsons);
        System.out.println("Wrongly mapped URNs: " + numberOfIncorrectJsons);
    }

    private static List<String> listFilesForFolder(final File folder) {
        List<String> filesList = new ArrayList<>();
        File[] affFiles = folder.listFiles();
        if (affFiles != null) {
            for (final File fileEntry : affFiles) {
                if (fileEntry.isDirectory()) {
                    listFilesForFolder(fileEntry);
                } else {
                    System.out.println(fileEntry.getName());
                    filesList.add(fileEntry.getAbsolutePath());
                }
            }
        }
        return filesList;
    }

    private static Map<String, Object> checkFileForErrors(String fileName) {
        Map<String,Object> results = new HashMap<>();
        List<String> result = new ArrayList<>();
        String gbgCustomerId = "$.customer.identity.customerID";
        String branch = "$.customer.identity.branchBudgetCentre";
        String consultationReference = "$.consultation.reference";
        String customerReferenceNumber = "$.customer.identity.customerReferenceNumber";
        String requestUrn = "$.customer.identity.capture.urn";
        String thisLine;
        long incorrectJson = 0;
        long jsonCount = 0;
        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(fileName));

            while ((thisLine = br.readLine()) != null) {
                if (thisLine.endsWith("Request JSON:")) {
                    thisLine = br.readLine();
                } else {
                    continue;
                }
                try {
                    jsonCount++;
                    DocumentContext jsonContext = JsonPath.parse(thisLine);
                    int branchValue = jsonContext.read(branch);
                    String consultationReferenceValue = jsonContext.read(consultationReference);
                    int customerReferenceNumberValue = jsonContext.read(customerReferenceNumber);
                    String gbgIdValue = jsonContext.read(gbgCustomerId);
                    String urn = "urn:WR:" + branchValue + ":" + consultationReferenceValue.split("/")[1] + ":" + customerReferenceNumberValue;
                    String requestUrnValue = jsonContext.read(requestUrn);
                    if (!urn.equals(requestUrnValue)) {
                        System.out.println(String.format("Items %s and %s are not equal", urn, requestUrnValue));
                        result.add(gbgIdValue + "," + urn + "," + requestUrnValue);
                        incorrectJson++;
                    }
                } catch (PathNotFoundException e) {
                }
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
