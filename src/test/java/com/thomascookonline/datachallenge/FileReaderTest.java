package com.thomascookonline.datachallenge;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class FileReaderTest {

    static FileReader fileReader;

    @BeforeAll
    static void init(){
        try {
            fileReader = new FileReader("src\\main\\resources\\crm-xcom-2017-10-04-1.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkJsonForUrn(){

        String jsonpathCaptureUrnPath = "$..capture.urn";
        for(String jsonObject : fileReader.getJsonObjects()){
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
