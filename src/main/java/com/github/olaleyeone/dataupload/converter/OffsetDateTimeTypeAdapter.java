package com.github.olaleyeone.dataupload.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeTypeAdapter extends TypeAdapter<OffsetDateTime> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (OffsetDateTime.class.isAssignableFrom(type.getRawType())
                    ? (TypeAdapter<T>) new OffsetDateTimeTypeAdapter()
                    : null);
        }
    };

    @Override
    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public OffsetDateTime read(JsonReader in) throws IOException {
        return OffsetDateTime.parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
