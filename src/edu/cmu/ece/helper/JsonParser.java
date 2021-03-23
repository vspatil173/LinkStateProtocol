package edu.cmu.ece.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.ece.models.LinkStateMessage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonParser {
    public static LinkStateMessage generateMapFromString(String map){
        LinkStateMessage msg = new LinkStateMessage();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            msg = objectMapper.readValue(map, LinkStateMessage.class);
        }catch (JsonMappingException ex){
            System.out.println(ex.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }
    public static String generateStringFromJson(LinkStateMessage map){
       ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
