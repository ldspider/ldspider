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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains a series of Rules. It then runs a path against these 
 * to decide if it is allowed or not. 
 */
 // TODO: Make this package private?
class RulesEngine {

    private List<Rule> rules;

    public RulesEngine() {
        this.rules = new ArrayList<Rule>();
    }

    public void allowPath(String path) {
        add( new AllowedRule(path) );
    }

    public void disallowPath(String path) {
        add( new DisallowedRule(path) );
    }

    public void add(Rule rule) {
        this.rules.add(rule);
    }

    /**
     * Run each Rule in series on the path. 
     * If a Rule returns a Boolean, return that.
     * When no more rules are left, return null to indicate there were 
     * no rules for this path.. 
     */
    public Boolean isAllowed(String path) {

        Iterator<Rule> iterator = this.rules.iterator();
        while(iterator.hasNext()) {
            Rule rule = (Rule)iterator.next();
            Boolean test = rule.isAllowed(path);
            if(test != null) {
                return test;
            }
        }

        return null;
    }

    public boolean isEmpty() {
        return this.rules.isEmpty();
    }

    public String toString() {
        return "RulesEngine: " + this.rules;
    }

}
