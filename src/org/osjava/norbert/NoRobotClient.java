/*
 * Copyright (c) 2003-2005, Henri Yandell
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

import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * A Client which may be used to decide which urls on a website 
 * may be looked at, according to the norobots specification 
 * located at: 
 * http://www.robotstxt.org/wc/norobots-rfc.html
 */
public class NoRobotClient {

    private String userAgent;
    private RulesEngine rules;
    private RulesEngine wildcardRules;
    private URL baseUrl;

    /**
     * Create a Client for a particular user-agent name. 
     *
     * @param userAgent name for the robot
     */
    public NoRobotClient(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Head to a website and suck in their robots.txt file. 
     * Note that the URL passed in is for the website and does 
     * not include the robots.txt file itself.
     *
     * @param baseUrl of the site
     */
//    public void parse(URL baseUrl) throws NoRobotException {
//
//        this.rules = new RulesEngine();
//
//        this.baseUrl = baseUrl;
//
//        URL txtUrl = null;
//        try {
//            // fetch baseUrl+"robots.txt"
//            txtUrl = new URL(baseUrl, "robots.txt");
//        } catch(MalformedURLException murle) {
//            throw new NoRobotException("Bad URL: "+baseUrl+", robots.txt. ", murle);
//        }
//
//        String txt = null;
//        try {
//            txt = loadContent(txtUrl, this.userAgent);
//            if(txt == null) {
//                throw new NoRobotException("No content found for: "+txtUrl);
//            }
//        } catch(IOException ioe) {
//            throw new NoRobotException("Unable to get content for: "+txtUrl, ioe);
//        }
//
//        try {
//            parseText(txt);
//        } catch(NoRobotException nre) {
//            throw new NoRobotException("Problem while parsing "+txtUrl, nre);
//        }
//    }

    public void parse(String txt, URL baseUrl) throws NoRobotException {
    	this.baseUrl = baseUrl;
    	parseText(txt);
    }
    
    public void parseText(String txt) throws NoRobotException {
        this.rules = parseTextForUserAgent(txt, this.userAgent);
        this.wildcardRules = parseTextForUserAgent(txt, "*");
    }

    private RulesEngine parseTextForUserAgent(String txt, String userAgent) throws NoRobotException {

        RulesEngine engine = new RulesEngine();

        // Classic basic parser style, read an element at a time, 
        // changing a state variable [parsingAllowBlock]

        // take each line, one at a time
        BufferedReader rdr = new BufferedReader( new StringReader(txt) );
        String line = "";
        String value = null;
        boolean parsingAllowBlock = false;
        try {
            while( (line = rdr.readLine()) != null ) {
                // trim whitespace from either side
                line = line.trim();

                // ignore startsWith('#')
                if(line.startsWith("#")) {
                    continue;
                }

                // if User-agent == userAgent 
                // record the rest up until end or next User-agent
                // then quit (? check spec)
                if(line.toLowerCase().startsWith("user-agent:")) {

                    if(parsingAllowBlock) {
                        // we've just finished reading allows/disallows
                        if(engine.isEmpty()) {
                            // multiple user agents in a line, let's 
                            // wait til we get rules
                            continue;
                        } else {
                            break;
                        }
                    }

                    value = line.toLowerCase().substring("user-agent:".length()).trim();
                    if(value.equalsIgnoreCase(userAgent)) {
                        parsingAllowBlock = true;
                        continue;
                    }
                } else {
                    // if not, then store if we're currently the user agent
                    if(parsingAllowBlock) {
                        if(line.startsWith("Allow:")) {
                            value = line.substring("Allow:".length()).trim();
                            value = URLDecoder.decode(value, "UTF-8");
                            engine.allowPath( value );
                        } else 
                        if(line.startsWith("Disallow:")) {
                            value = line.substring("Disallow:".length()).trim();
                            value = URLDecoder.decode(value, "UTF-8");
                            engine.disallowPath( value );
                        } else {
                            // ignore
                            continue;
                        }
                    } else {
                        // ignore
                        continue;
                    }
                }
            }
        } catch (IOException ioe) {
            // As this is parsing a String, it should not have an IOE
            throw new NoRobotException("Problem while parsing text. ", ioe);
        }

        return engine;
    }

    /**
     * Decide if the parsed website will allow this URL to be 
     * be seen. 
     *
     * Note that parse(URL) must be called before this method 
     * is called. 
     *
     * @param url in question
     * @return is the url allowed?
     *
     * @throws IllegalStateException when parse has not been called
     */
    public boolean isUrlAllowed(URL url) throws IllegalStateException, IllegalArgumentException {
        if(rules == null) {
            throw new IllegalStateException("You must call parse before you call this method.  ");
        }

        if( !baseUrl.getHost().equals(url.getHost()) ||
            baseUrl.getPort() != url.getPort() ||
            !baseUrl.getProtocol().equals(url.getProtocol()) )
        {
            throw new IllegalArgumentException("Illegal to use a different url, " + url.toExternalForm() + 
                                               ",  for this robots.txt: "+this.baseUrl.toExternalForm());
        }
        String urlStr = url.toExternalForm().substring( this.baseUrl.toExternalForm().length() - 1);
        if("/robots.txt".equals(urlStr)) {
            return true;
        }
        urlStr = URLDecoder.decode( urlStr );
        Boolean allowed = this.rules.isAllowed( urlStr );
        if(allowed == null) {
            allowed = this.wildcardRules.isAllowed( urlStr );
        }
        if(allowed == null) {
            allowed = Boolean.TRUE;
        }

        return allowed.booleanValue();
    }

//    // INLINE: as such from genjava/gj-core's net package. Simple method 
//    // stolen from Payload too.
//    private static String loadContent(URL url, String userAgent) throws IOException {
//        URLConnection urlConn = url.openConnection();
//        if(urlConn instanceof HttpURLConnection) {
//            if(userAgent != null) {
//                ((HttpURLConnection)urlConn).addRequestProperty("User-Agent", userAgent);
//            }
//        }
//        InputStream in = urlConn.getInputStream();
//        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
//        StringBuffer buffer = new StringBuffer();
//        String line = "";
//        while( (line = rdr.readLine()) != null) {
//            buffer.append(line);
//            buffer.append("\n");
//        }
//        in.close();
//        return buffer.toString();
//    }
    
    public String toString() {
    	return this.rules.toString() + " " + this.wildcardRules.toString();
    }

}
