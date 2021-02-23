package com.jsoniter.extra;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import junit.framework.TestCase;

public class TestBase64 extends TestCase {
    static {
        Base64Support.enable();
    }

    public void test_encode() {
        assertEquals("\"YWJj\"", JsonStream.serialize("abc".getBytes()));
    }

    public void test_decode() {
        assertEquals("abc", new String(JsonIterator.deserialize("\"YWJj\"", byte[].class)));
    }

    public void test_empty() {
        assertEquals("", new String(JsonIterator.deserialize("\"\"", byte[].class)));
    }

    public void test_illegal_front() {
        assertEquals("abc", new String(JsonIterator.deserialize("\"@YWJj\"", byte[].class)));
    }
    
    public void test_illegal_back() {
        assertEquals("abc", new String(JsonIterator.deserialize("\"YWJj@\"", byte[].class)));
    }

    public void test_padding_back() {
        assertEquals("abcd", new String(JsonIterator.deserialize("\"YWJjZA==\"", byte[].class)));
    }
}
