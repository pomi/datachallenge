package com.thomascookonline.datachallenge;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import java.io.BufferedReader;

public class FileReader2 {

    static String json = "{\"response\":{\"customer\":{\"customerId\":\"5bcdeaaa-21ab-476d-8413-77937ff4f9d3\",\"uri\":\"/customers/5bcdeaaa-21ab-476d-8413-77937ff4f9d3\",\"statusCode\":200},\"bookings\":[{\"uri\":\"/customers/5bcdeaaa-21ab-476d-8413-77937ff4f9d3/bookings/booking/detail\",\"statusCode\":201}],\"enquiries\":[{\"message\":\"This feature is switched off by integration layer\",\"statusCode\":501}]}}\n";

    public static void main(String[] args){
        String thisLine = null;
        //String jsonpathCaptureUrnPath = "$..capture.urn";
        String branch = "$['customer']['identity']['branchBudgetCentre']";
        String consultationReference = "$.consultation.reference";
        String customerReferenceNumber = "$.customer.identity.customerReferenceNumber";
        String requestUrn = "$.customer.identity.capture.urn";
        try {

            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new java.io.FileReader("src\\main\\resources\\crm-debug-2017-10-04-1.log"));

            while ((thisLine = br.readLine()) != null) {
                try {
                    JSONObject jsonObj = new JSONObject(thisLine);
                    DocumentContext jsonContext = JsonPath.parse(thisLine);
                    int branchValue = jsonContext.read(branch);
                    String consultationReferenceValue = jsonContext.read(consultationReference);
                    int customerReferenceNumberValue = jsonContext.read(customerReferenceNumber);
                    String urn = "urn:WR:" + branchValue + ":" + consultationReferenceValue.split("/")[1] + ":" + customerReferenceNumberValue;
                    String requestUrnValue = jsonContext.read(requestUrn);
                    if (!urn.equals(requestUrnValue)) {
                            System.out.println(thisLine);
                            System.out.println(String.format("Items %s and %s are not equal", urn, requestUrnValue));
                        }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
