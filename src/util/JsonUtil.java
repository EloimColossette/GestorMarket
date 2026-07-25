package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import java.time.LocalDate;

public class JsonUtil {

    // Gson não sabe lidar com java.time.LocalDate por padrão,
    // então registramos um adapter simples (formato yyyy-MM-dd,
    // que é o mesmo que o <input type="date"> do HTML já envia).
    private static final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(
                            LocalDate.class,
                            (com.google.gson.JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                                    new JsonPrimitive(src.toString())
                    )
                    .registerTypeAdapter(
                            LocalDate.class,
                            (com.google.gson.JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                                    LocalDate.parse(json.getAsString())
                    )
                    .create();

    public static Gson getGson() {
        return gson;
    }
}