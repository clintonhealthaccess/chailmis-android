package org.clintonhealthaccess.lmis.app.validators;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AllocationIdValidatorTest {
    @Test
    public void shouldBeValidIfIdIsInCorrectFormat() throws Exception {
        AllocationIdValidator validator = new AllocationIdValidator();
        assertTrue(validator.isValid("SR-0002"));
        assertTrue(validator.isValid("ja-0002"));
        assertTrue(validator.isValid("ja-0009892"));
        assertTrue(validator.isValid("22-0009"));
    }

    @Test
    public void shouldBeInValidIfIdIsInWrongFormat() throws Exception {
        AllocationIdValidator validator = new AllocationIdValidator();
        assertFalse(validator.isValid("SOMEONES NAME"));
        assertFalse(validator.isValid("ALLOCATION_ID"));
        assertFalse(validator.isValid("jks-0009"));
        assertFalse(validator.isValid("ja-0009w892"));
        assertFalse(validator.isValid("1223"));
        assertFalse(validator.isValid("   -"));
        assertFalse(validator.isValid("..."));
    }
}