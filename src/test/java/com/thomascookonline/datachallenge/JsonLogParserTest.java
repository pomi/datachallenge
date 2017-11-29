package com.thomascookonline.datachallenge;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

class JsonLogParserTest {

    private static JsonCustLogParser jsonCustLogParser;

    @BeforeAll
    static void init(){
        try {
            jsonCustLogParser = new JsonCustLogParser("src\\test\\resources\\crm-xcom-2017-10-04-1.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkJsonForUrn(){

        String jsonpathCaptureUrnPath = "$..capture.urn";
        for(String jsonObject : jsonCustLogParser.getJsonObjects()){
            //Logger.getAnonymousLogger().info(jsonObject);
            DocumentContext jsonContext = JsonPath.parse(jsonObject);
            List<String> jsonpathCaptureUrn = jsonContext.read(jsonpathCaptureUrnPath);
            //Logger.getAnonymousLogger().info(String.valueOf(jsonpathCaptureUrn.size()));
            if(jsonpathCaptureUrn.size() != 0) {
                String firstItem = jsonpathCaptureUrn.get(0);
                int i = 0;
                for (String urn : jsonpathCaptureUrn) {
                    if(!firstItem.equals(urn)) {
                        System.out.println(String.format("Items %s and %s are equal", firstItem, urn));
                    }
                }
            }else{
                Logger.getAnonymousLogger().info("No URN in provided Json");
            }
        }
    }

}
