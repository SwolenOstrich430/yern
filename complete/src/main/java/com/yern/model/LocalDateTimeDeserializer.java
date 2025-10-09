package com.yern.model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// TODO: fix this -- terrible
public class LocalDateTimeDeserializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); //
    }

    public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        LocalDateTime localDateTime;

        if(jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            localDateTime = LocalDateTime.of(
                    jsonArray.get(0).getAsInt(),
                    jsonArray.get(1).getAsInt(),
                    jsonArray.get(2).getAsInt(),
                    jsonArray.get(3).getAsInt(),
                    jsonArray.get(4).getAsInt(),
                    jsonArray.get(5).getAsInt(),
                    jsonArray.get(6).getAsInt()
            );
        } else if(jsonElement.isJsonPrimitive()) {
            localDateTime = LocalDateTime.parse(jsonElement.getAsString());
        } else {
            throw new JsonParseException(jsonElement.toString());
        }

        return localDateTime;
    }
}