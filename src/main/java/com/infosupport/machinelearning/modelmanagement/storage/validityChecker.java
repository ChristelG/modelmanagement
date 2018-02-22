package com.infosupport.machinelearning.modelmanagement.storage;

import java.io.IOException;
import java.io.InputStream;

public class validityChecker {

    public boolean isValidName(String s) {
        String pattern = "^[a-z0-9](-?[a-z0-9])*$";
        return s.matches(pattern);
    }

    public boolean streamNotEmpty(InputStream entity) throws IOException{
        return entity.available() > 0;
    }

    public boolean isValidInput(String s, InputStream entity) throws IOException{
        return this.isValidName(s) & streamNotEmpty(entity);
    }
}
