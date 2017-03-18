/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> Ioannis K. Moutsatsos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.biouno.unochoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.model.ScriptlerScript;
import org.biouno.unochoice.model.ScriptlerScriptParameter;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.ParameterDefinition;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <p>A choice parameter, that gets updated when another parameter changes. The simplest 
 * use case for this, would be having a list of states, and when the user selected a
 * state it would trigger an update of the city fields.</p>
 *
 * <p>The state parameter would be a choice parameter, and the city parameter would be a
 * cascade choice parameter, that referenced the former.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class CascadeChoiceParameter extends AbstractCascadableParameter {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 4524790278642708107L;

    /**
     * Choice type.
     */
    private final String choiceType;

    /**
     * Filter flag.
     */
    private final Boolean filterable;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param script script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param filterable filter flag
     * @deprecated see JENKINS-32149
     */
    public CascadeChoiceParameter(String name, String description, Script script, 
            String choiceType, String referencedParameters, Boolean filterable) {
        super(name, description, script, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
    }

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param randomName parameter random generated name (uuid)
     * @param script script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param filterable filter flag
     */
    @DataBoundConstructor
    public CascadeChoiceParameter(String name, String description, String randomName, Script script, 
            String choiceType, String referencedParameters, Boolean filterable) {
        super(name, description, randomName, script, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return choiceType;
    }

    /**
     * Gets the filter flag.
     *
     * @return filter flag
     */
    public Boolean getFilterable() {
        return filterable;
    }

    @Override
    public Map<Object, Object> getChoices() {
        return Collections.emptyMap();
    }

    // --- descriptor

    @Extension
    public static final class DescriptImpl extends UnoChoiceParameterDescriptor {

        @Override
        public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String projectName = null;
            if (req != null) {
                final Ancestor ancestor = req.findAncestor(AbstractItem.class);
                if (ancestor != null) {
                    final Object o = ancestor.getObject();
                    if (o instanceof AbstractItem) {
                        final AbstractItem parentItem = (AbstractItem) o;
                        projectName = parentItem.getName();
                    }
                }
            }
            final String name = formData.getString("name");
            final String description = formData.getString("description");
            final String randomName = formData.getString("randomName");
            JSONObject scriptJsonObject = formData.getJSONObject("script");
            final Script script;
            if (scriptJsonObject.containsKey("scriptlerScriptId")) {
                final String scriptlerScriptId = scriptJsonObject.getString("scriptlerScriptId");
                final List<ScriptlerScriptParameter> parameters = new ArrayList<ScriptlerScriptParameter>();
                if (scriptJsonObject.containsKey("defineParams")) {
                    JSONObject defineParams = scriptJsonObject.getJSONObject("defineParams");
                    if (defineParams.containsKey("parameters")) {
                        JSONArray scriptlerScriptParameters = defineParams.getJSONArray("parameters");
                        for (int i = 0; i < scriptlerScriptParameters.size(); i++) {
                            JSONObject entry = scriptlerScriptParameters.getJSONObject(i);
                            ScriptlerScriptParameter param = new ScriptlerScriptParameter(
                                    entry.getString("name"), entry.getString("value"));
                            parameters.add(param);
                        }
                    }
                }
                final ScriptlerScript scriptlerScript = new ScriptlerScript(scriptlerScriptId, parameters);
                script = scriptlerScript;
            } else {
                String groovyScriptScript = "";
                Boolean groovyScriptSandbox = false;
                String groovyFallbackScript = "";
                Boolean groovyFallabackSandbox = false;
                if (scriptJsonObject.containsKey("script")) {
                    final JSONObject scriptScriptJsonObject = scriptJsonObject.getJSONObject("script");
                    // so many scripts ay?
                    if (scriptScriptJsonObject.containsKey("script")) {
                        groovyScriptScript = scriptScriptJsonObject.getString("script");
                    }
                    if (scriptScriptJsonObject.containsKey("sandbox")) {
                        groovyScriptSandbox = scriptScriptJsonObject.getBoolean("sandbox");
                    }
                }
                if (scriptJsonObject.containsKey("fallbackScript")) {
                    final JSONObject scriptFallbackJsonObject = scriptJsonObject.getJSONObject("fallbackScript");
                    // so many scripts ay?
                    if (scriptFallbackJsonObject.containsKey("script")) {
                        groovyFallbackScript = scriptFallbackJsonObject.getString("script");
                    }
                    if (scriptFallbackJsonObject.containsKey("sandbox")) {
                        groovyFallabackSandbox = scriptFallbackJsonObject.getBoolean("sandbox");
                    }
                }
                GroovyScript groovyScript = new GroovyScript(
                        new SecureGroovyScript(groovyScriptScript, groovyScriptSandbox, null),
                        new SecureGroovyScript(groovyFallbackScript, groovyFallabackSandbox, null));
                script = groovyScript;
            }
            final String choiceType = formData.getString("choiceType");
            final String referencedParameters = formData.getString("referencedParameters");
            final Boolean omitValueField = formData.containsKey("") ?
                    formData.getBoolean("omitValueField") : Boolean.FALSE;
            CascadeChoiceParameter param = new CascadeChoiceParameter(
                    name,
                    description,
                    randomName,
                    script,
                    choiceType,
                    referencedParameters,
                    omitValueField);
            param.setProjectName(projectName);
            return param;
        }

        @Override
        public String getDisplayName() {
            return "Active Choices Reactive Parameter";
        }

    }

}
