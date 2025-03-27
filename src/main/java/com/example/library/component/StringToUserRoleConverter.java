package com.example.library.component;
import com.example.library.model.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserRoleConverter implements Converter<String, UserRole> {

    @Override
    public UserRole convert(String source) {
        return UserRole.valueOf(source.toUpperCase());
    }
}