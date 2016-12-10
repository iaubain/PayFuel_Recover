package apiclient;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.BaseUrl;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Owner on 7/9/2016.
 */
public class ServerClient {
    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        if (retrofit==null) {
            ObjectMapper objectMapper=new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl.BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build();
        }
        return retrofit;
    }
}
