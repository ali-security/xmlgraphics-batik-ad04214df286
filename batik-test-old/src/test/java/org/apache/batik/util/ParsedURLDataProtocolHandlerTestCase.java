/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.util;

import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ParsedURLDataProtocolHandlerTestCase {
    @Test
    public void testParseErrorWithNoDataUri() {
        String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                "width=\"1418\" height=\"370\">" +
                "<image xlink:href=\"data:example.com," +
                "%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20xmlns%3Axlink%3D%22http%3A//www.w3.org/1999/xlink" +
                "%22%20width%3D%2250%22%20height%3D%2250%22%3E%3Cimage%20xlink%3Ahref%3D%22http%3A//example.com/s/favicon.png" +
                "%22%20width%3D%2250%22%20height%3D%2250%22/%3E%3C/svg%3E\" width=\"200\" height=\"200\"/></svg>\n";
        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgContent.getBytes()));
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);
        TranscoderException ex = null;
        try {
            transcoder.transcode(input, output);
            Assert.fail("Expected TranscoderException to be thrown");
        } catch (TranscoderException e) {
            ex = e;
        }
        Assert.assertEquals("null\n" +
                "Enclosed Exception:\n" +
                "The document references a external resource (http://example.com/s/favicon.png) " +
                "which comes from different location than the document itself. This is not allowed " +
                "for security reasons and that resource will not be loaded.", ex.getMessage());
    }

    @Test
    public void testMediaTypeIsNotReportedAsHost() {
        ParsedURL purl = new ParsedURL("data:example.com;charset=utf-8;base64,PHN2Zy8+");
        Assert.assertEquals("data", purl.getProtocol());
        Assert.assertNull(purl.getHost());
        Assert.assertEquals("example.com;charset=utf-8", purl.getContentType());
        Assert.assertEquals("base64", purl.getContentEncoding());
        Assert.assertEquals("utf-8", purl.getContentTypeCharset());
        Assert.assertEquals("data:example.com;charset=utf-8;base64,PHN2Zy8+", purl.toString());
    }

    @Test
    public void testScriptFromHostMatchingDataUriMediaType() {
        ParsedURL docUrl = new ParsedURL("data:example.com,%3Csvg/%3E");
        ParsedURL scriptUrl = new ParsedURL("http://example.com/s/evil.js");
        String err = "";
        try {
            new DefaultScriptSecurity("text/ecmascript", scriptUrl, docUrl).checkLoadScript();
        } catch (SecurityException e) {
            err = e.getMessage();
        }
        Assert.assertEquals("The document references a script file (http://example.com/s/evil.js) " +
                "which comes from different location than the document itself. This is not allowed with the current " +
                "security settings and that script will not be loaded.", err);
    }
}
