package utilities;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Hp on 12/9/2016.
 */

public class DataFactory {
    public DataFactory() {
    }

    public Object stringToObject(String jsonString, Object object){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        try{
            return mapper.readValue(jsonString, object.getClass());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String objectToString(Object object){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        try{
            return mapper.writeValueAsString(object);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
