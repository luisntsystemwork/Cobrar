package com.navicon.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class CustomCalendarDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        String dateAsString = jsonparser.getText();
        try {
            Date date = CustomCalendarSerializer.FORMATTER.parse(dateAsString);
            return date;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
