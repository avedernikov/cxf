/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.common.util;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.cxf.helpers.IOUtils;

import org.junit.Assert;
import org.junit.Test;

public class Base64UtilityTest extends Assert {

    public Base64UtilityTest() {
        super();
    }

    void assertEquals(byte b1[], byte b2[]) {
        assertEquals(b1.length, b2.length);
        for (int x = 0; x < b1.length; x++) {
            assertEquals(b1[x], b2[x]);
        }
    }

    @Test
    public void testEncodeMultipleChunks() throws Exception {
        final String text = "The true sign of intelligence is not knowledge but imagination.";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        // multiple of 3 octets
        assertEquals(63, bytes.length);
        String s1 = new String(Base64Utility.encodeChunk(bytes, 0, bytes.length));

        StringBuilder sb = new StringBuilder();
        int off = 0;
        for (; off + 21 < bytes.length; off += 21) {
            sb.append(Base64Utility.encodeChunk(bytes, off, 21));
        }
        if (off < bytes.length) {
            sb.append(Base64Utility.encodeChunk(bytes, off, bytes.length - off));
        }
        String s2 = sb.toString();
        assertEquals(s1, s2);
    }

    @Test
    public void testEncodeAndStream() throws Exception {
        final String text = "The true sign of intelligence is not knowledge but imagination.";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Base64Utility.encodeAndStream(bytes, 0, bytes.length, bos);
        String decodedText = new String(Base64Utility.decode(bos.toString()));
        assertEquals(decodedText, text);
    }

    @Test
    public void testEncodeDecodeChunk() throws Exception {
        byte bytes[] = new byte[100];
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = (byte)x;
        }

        char encodedChars[] = Base64Utility.encodeChunk(bytes, 0, -2);
        assertNull(encodedChars);
        encodedChars = Base64Utility.encodeChunk(bytes, 0, bytes.length);
        assertNotNull(encodedChars);
        byte bytesDecoded[] = Base64Utility.decodeChunk(encodedChars, 0, encodedChars.length);
        assertEquals(bytes, bytesDecoded);

        //require padding
        bytes = new byte[99];
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = (byte)x;
        }
        encodedChars = Base64Utility.encodeChunk(bytes, 0, bytes.length);
        assertNotNull(encodedChars);
        bytesDecoded = Base64Utility.decodeChunk(encodedChars, 0, encodedChars.length);
        assertEquals(bytes, bytesDecoded);

        //require padding
        bytes = new byte[98];
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = (byte)x;
        }
        encodedChars = Base64Utility.encodeChunk(bytes, 0, bytes.length);
        assertNotNull(encodedChars);
        bytesDecoded = Base64Utility.decodeChunk(encodedChars, 0, encodedChars.length);
        assertEquals(bytes, bytesDecoded);

        //require padding
        bytes = new byte[97];
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = (byte)x;
        }
        encodedChars = Base64Utility.encodeChunk(bytes, 0, bytes.length);
        assertNotNull(encodedChars);
        bytesDecoded = Base64Utility.decodeChunk(encodedChars, 0, encodedChars.length);
        assertEquals(bytes, bytesDecoded);


        bytesDecoded = Base64Utility.decodeChunk(new char[3], 0, 3);
        assertNull(bytesDecoded);
    }

    @Test
    public void testEncodeDecodeString() throws Exception {
        String in = "QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
        byte bytes[] = Base64Utility.decode(in);
        assertEquals("Aladdin:open sesame", IOUtils.newStringFromBytes(bytes));
        String encoded = Base64Utility.encode(bytes);
        assertEquals(in, encoded);
    }

    @Test
    public void testDecodeInvalidString() throws Exception {
        try {
            String in = "QWxhZGRpbjpcGVuIHNlc2FtZQ==";
            Base64Utility.decode(in);
            fail("This test should be fail");
        } catch (Base64Exception e) {
            //nothing to do
        }
    }

    @Test
    public void testEncodeDecodeStreams() throws Exception {
        byte bytes[] = new byte[100];
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = (byte)x;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        Base64Utility.encodeChunk(bytes, 0, bytes.length, bout);
        String encodedString = IOUtils.newStringFromBytes(bout.toByteArray());
        Base64Utility.decode(encodedString.toCharArray(),
                             0,
                             encodedString.length(),
                             bout2);
        assertEquals(bytes, bout2.toByteArray());


        String in = "QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
        bout.reset();
        bout2.reset();
        Base64Utility.decode(in, bout);
        bytes = bout.toByteArray();
        assertEquals("Aladdin:open sesame", IOUtils.newStringFromBytes(bytes));
        StringWriter writer = new StringWriter();
        Base64Utility.encode(bytes, 0, bytes.length, writer);
        assertEquals(in, writer.toString());

    }


}
