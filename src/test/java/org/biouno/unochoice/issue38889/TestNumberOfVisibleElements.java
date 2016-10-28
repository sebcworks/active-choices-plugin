/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Ioannis Moutsatsos, Bruno P. Kinoshita
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.biouno.unochoice.issue38889;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.biouno.unochoice.AbstractScriptableParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TestNumberOfVisibleElements {

    private final String SCRIPT = "return [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22]";
    private final String SHORTER_SCRIPT = "return [1,2,3]";
    private final String FALLBACK_SCRIPT = "";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(SHORTER_SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    public void testMustRespectTheDefaultMax() {
        ChoiceParameter parameter = new ChoiceParameter(
                "script001", "description", "random name", new GroovyScript(SCRIPT, FALLBACK_SCRIPT),
                ChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true, true, 1);

        parameter.getChoices(Collections.<Object, Object>emptyMap());
        int visibleItemCount = parameter.getComputedVisibleItemCount();
        assertEquals(AbstractScriptableParameter.DEFAULT_MAX_VISIBLE_ITEM_COUNT, visibleItemCount);
    }

    @Test
    public void testMustNotUseTheDefaultIfLess() {
        ChoiceParameter parameter = new ChoiceParameter(
                "script001-shorter", "description", "random name", new GroovyScript(SHORTER_SCRIPT, FALLBACK_SCRIPT),
                ChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true, true, 1);

        Map<?,?> r = parameter.getChoices(Collections.<Object, Object>emptyMap());
        int visibleItemCount = parameter.getComputedVisibleItemCount();
        assertEquals(r.size(), visibleItemCount);
    }

    @Test
    public void testMustReturnSpecifiedVisibleItemCount() {
        ChoiceParameter parameter = new ChoiceParameter(
                "script001", "description", "random name", new GroovyScript(SCRIPT, FALLBACK_SCRIPT),
                ChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true, false, 12);

        parameter.getChoices(Collections.<Object, Object>emptyMap());
        int visibleItemCount = parameter.getComputedVisibleItemCount();
        assertEquals(12, visibleItemCount);
    }

}
