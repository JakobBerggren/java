package com.jsoniter;

import com.jsoniter.spi.JsonException;
import com.jsoniter.static_codegen.StaticCodegen;
import junit.framework.TestCase;
import org.junit.Test;

public class TestStaticCodegen  extends TestCase {

    @Test(expected = Test.None.class)
    public void test_no_args() throws Exception {
        StaticCodegen.main(new String[]{});
    }

    public void test_invalid_args() {
        int err = 0;
        try {
            StaticCodegen.main(new String[]{"aJavaFileThatCertainlyDoesntExist"});
        } catch (JsonException e) {
            err = 1;
        } catch (Exception e) {}
        assertEquals(1, err);
    }
}