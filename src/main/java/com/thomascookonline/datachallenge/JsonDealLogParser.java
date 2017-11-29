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

public class JsonDealLogParser {

    private List<String> jsonObjects = new ArrayList<>();

    List<String> getJsonObjects() {
        return jsonObjects;
    }

    JsonDealLogParser(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String thisLine;
        while ((thisLine = br.readLine()) != null) {
            try {
                jsonObjects.add(thisLine);
            } catch (Exception e) {
                Logger.getAnonymousLogger().info("Row is not a JSON");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: JsonDealLogParser <path_to_debug_logs>");
            System.exit(0);
        }

        long numberOfJsons = 0;
        long numberOfCorrectJsons = 0;
        long numberOfIncorrectJsons = 0;
        String[] arrFNames;
        String shortFilename;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("deal_urns.csv"))) {
            for (String filename : listFilesForFolder(new File(args[0]))) {
                HashMap<String, Object> results = (HashMap<String, Object>) checkFileForErrors(filename);
                List<String> errorMessages = (List<String>) results.get("listOfErrors");
                numberOfJsons += (long)results.get("numberOfJsons");
                numberOfCorrectJsons += (long)results.get("numberOfCorrectJson");
                numberOfIncorrectJsons += (long)results.get("numberOfErrorJson");
                arrFNames = filename.split("\\\\");
                shortFilename = arrFNames[arrFNames.length - 1];
                for(String errorMessage : errorMessages){
                    bw.write(shortFilename + ": " + errorMessage);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total number of JSONs: " + numberOfJsons);
        System.out.println("Correctly mapped URNs: " + numberOfCorrectJsons);
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
        String consultationReference = "$.consultation.reference";
        String confirmedDealSeqNum = "$.salesTransaction.deals[0].dealSequenceNumber";
        String enquiriesDealSeqNum = "$.enquiries.deals[0].dealSequenceNumber";
        String reqSalesTransactionsDeal0Urn = "$.salesTransaction.deals[0].urn";
        String reqEnquiriesDeal0Urn = "$.enquiries.deals[0].urn";
        String thisLine;
        long jsonCount = 0;
        long correctJson = 0;
        long incorrectJson = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            while ((thisLine = br.readLine()) != null) {
                if (thisLine.endsWith("Request JSON:")) {
                    thisLine = br.readLine();
                } else {
                    continue;
                }
                try {
                    jsonCount++;
                    DocumentContext jsonContext = JsonPath.parse(thisLine);
                    String consultationReferenceValue = jsonContext.read(consultationReference);
                    String gbgIdValue, dealSeqNoValue, requestUrnValue;
                    try {
                        gbgIdValue = jsonContext.read(gbgCustomerId);
                    } catch (PathNotFoundException e) {
                        gbgIdValue = "<missing>";
                    }
                    try {
                        dealSeqNoValue = jsonContext.read(confirmedDealSeqNum);
                        requestUrnValue = jsonContext.read(reqSalesTransactionsDeal0Urn);
                    } catch (PathNotFoundException e) {
                        try {
                            dealSeqNoValue = jsonContext.read(enquiriesDealSeqNum);
                            requestUrnValue = jsonContext.read(reqEnquiriesDeal0Urn);
                        } catch (PathNotFoundException e2) {
                            dealSeqNoValue = "<skip>";
                            requestUrnValue = "<how-come!?>";
                        }
                    }
                    if ("<skip>".equals(dealSeqNoValue)) {
                        continue;
                    }
                    String urn = "urn:WR:" + consultationReferenceValue.replace("/", ":") + ":deal:" + dealSeqNoValue;
                    if (urn.equals(requestUrnValue)) {
                        correctJson++;
                    } else {
                        //System.out.println(String.format("Items %s and %s are not equal", urn, requestUrnValue));
                        result.add(gbgIdValue + "," + urn + "," + requestUrnValue);
                        incorrectJson++;
                    }
                } catch (PathNotFoundException e) {
                    System.out.println("Parsing error: " + e.getLocalizedMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        results.put("listOfErrors", result);
        results.put("numberOfJsons", jsonCount);
        results.put("numberOfCorrectJson", correctJson);
        results.put("numberOfErrorJson", incorrectJson);
        return results;
    }
}
