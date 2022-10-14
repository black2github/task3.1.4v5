package ru.kata.spring.boot_security.demo.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomUserSerializer extends StdSerializer<User> {

    public CustomUserSerializer() {
        this(null);
    }

    public CustomUserSerializer(Class<User> t) {
        super(t);
    }

    @Override
    public void serialize(
            User user, JsonGenerator jsonGenerator, SerializerProvider serializer) throws IOException {
        jsonGenerator.writeStartObject();
        if (user.getId()!=null) jsonGenerator.writeNumberField("id", user.getId());
        if (user.getFirstName()!=null) jsonGenerator.writeStringField("firstName", user.getFirstName());
        if (user.getLastName()!=null) jsonGenerator.writeStringField("lastName", user.getLastName());
        jsonGenerator.writeStringField("email", user.getEmail());
        if (user.getPassword()!=null) jsonGenerator.writeStringField("password", user.getPassword());
        jsonGenerator.writeNumberField("age", user.getAge());
        if (user.getRoles()!=null) {
            jsonGenerator.writeFieldName("roles");
            jsonGenerator.writeArray(user.getRoleNames().toArray(new String[0]), 0, user.getRoles().size());
        }
        jsonGenerator.writeEndObject();
    }
}
