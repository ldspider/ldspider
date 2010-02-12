/*
 * Copyright (c) 2003, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of OSJava nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.osjava.norbert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

public class NoRobotClientTest extends TestCase {

    private String hardCode = "file://"+new File("data/").getAbsoluteFile()+"/";

    public NoRobotClientTest(String name) {
        super(name);
    }

    //-----------------------------------------------------------------------
    // To test: 
    // create -> parse -> isUrlAllowed?

    public void testAllowed() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "basic/";
        NoRobotClient nrc = new NoRobotClient("Scabies-1.0");
        nrc.parse( new URL(base) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"index.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"view-cvs/") ) );
    }

    // Tests the example given in the RFC
    public void testRfcExampleUnhipbot() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "rfc/";

        NoRobotClient nrc = new NoRobotClient("unhipbot");
        nrc.parse( new URL(base) );

        // Start of rfc test
        assertFalse( nrc.isUrlAllowed( new URL(base) ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"index.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"robots.txt") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"server.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"services/fast.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"services/slow.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"orgo.gif") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"org/about.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"org/plans.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"%7Ejim/jim.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"%7Emak/mak.html") ) );
        // End of rfc test
    }

    public void testRfcExampleWebcrawler() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "rfc/";

        NoRobotClient nrc = new NoRobotClient("webcrawler");
        nrc.parse( new URL(base) );
        // Start of rfc test
        assertTrue( nrc.isUrlAllowed( new URL(base) ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"index.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"robots.txt") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"server.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/fast.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/slow.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"orgo.gif") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"org/about.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"org/plans.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Ejim/jim.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Emak/mak.html") ) );
        // End of rfc test
    }

    public void testRfcExampleExcite() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "rfc/";

        NoRobotClient nrc = new NoRobotClient("excite");
        nrc.parse( new URL(base) );
        // Start of rfc test
        assertTrue( nrc.isUrlAllowed( new URL(base) ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"index.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"robots.txt") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"server.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/fast.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/slow.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"orgo.gif") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"org/about.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"org/plans.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Ejim/jim.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Emak/mak.html") ) );
        // End of rfc test
    }

    public void testRfcExampleOther() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "rfc/";

        NoRobotClient nrc = new NoRobotClient("other");
        nrc.parse( new URL(base) );
        // Start of rfc test
        assertFalse( nrc.isUrlAllowed( new URL(base) ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"index.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"robots.txt") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"server.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/fast.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"services/slow.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"orgo.gif") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"org/about.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"org/plans.html") ) );
        assertFalse( nrc.isUrlAllowed( new URL(base+"%7Ejim/jim.html") ) );
        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Emak/mak.html") ) );
        // End of rfc test
    }

    public void testRfcBadWebDesigner() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "bad/";

        NoRobotClient nrc = new NoRobotClient("other");
        nrc.parse( new URL(base) );

        assertTrue( nrc.isUrlAllowed( new URL(base+"%7Etest/%7Efoo.html") ) );
    }

    // Tests NRB-3
    // http://www.osjava.org/jira/browse/NRB-3
    public void testNrb3() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "basic/";
        NoRobotClient nrc = new NoRobotClient("Scabies-1.0");
        nrc.parse( new URL(base) );
        assertTrue( nrc.isUrlAllowed( new URL(this.hardCode + "basic" ) ) );
    }

    // Tests NRB-6
    // http://issues.osjava.org/jira/browse/NRB-6
    public void testNrb6() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "order/";
        NoRobotClient nrc = new NoRobotClient("Scabies-1.0");
        nrc.parse( new URL(base) );
        assertTrue( "Specific then Wildcard not working as expected", nrc.isUrlAllowed( new URL(base + "order/" ) ) );

        base = this.hardCode + "order-reverse/";
        nrc = new NoRobotClient("Scabies-1.0");
        nrc.parse( new URL(base) );
        assertTrue( "Wildcard then Specific not working as expected", nrc.isUrlAllowed( new URL(base + "order/" ) ) );
    }      

    // Tests NRB-9
    // http://issues.osjava.org/jira/browse/NRB-9
    public void testNrb9() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "disallow-empty/";
        NoRobotClient nrc = new NoRobotClient("test");
        nrc.parse( new URL(base) );
        assertTrue( "'Disallow: ' should mean to disallow nothing", nrc.isUrlAllowed( new URL(base + "index.html" ) ) );
    }      

    // Tests NRB-8
    // http://issues.osjava.org/jira/browse/NRB-8
    public void testNrb8() throws MalformedURLException, NoRobotException {
        String base = this.hardCode + "ua-case-insensitive/";
        String[] names = new String[] { "test", "TEST", "tEsT" };
        for(int i=0; i<names.length; i++) {
            NoRobotClient nrc = new NoRobotClient(names[i]);
            nrc.parse( new URL(base) );
            assertFalse( "User-Agent names should be case insensitive", nrc.isUrlAllowed( new URL(base + "index.html" ) ) );
        }
    }      

}
