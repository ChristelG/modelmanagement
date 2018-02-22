package com.infosupport.machinelearning.modelmanagement.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class validityCheckerTest {

    private validityChecker m;

    @BeforeEach
    public void setup() {
        m = new validityChecker();
    }

    @Test
    public void testValidNames() {
        assertAll(
                () -> assertTrue(m.isValidName("model-test")),
                () -> assertTrue(m.isValidName("model-test-1")),
                () -> assertFalse(m.isValidName("model--test")),
                () -> assertFalse(m.isValidName("-model-test"))

        );
    }
}
