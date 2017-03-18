/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> <Ioannis Moutsatsos, Bruno P. Kinoshita>
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

package org.biouno.unochoice;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.Extension;
import hudson.util.FormValidation;

/**
 * <p>Provides a <b>dynamic reference parameter</b> for users. This is a not so elegant
 * solution, since we are using a ParameterDefinition extension point, but it 
 * actually <b>doesn't provide any parameter value</b>.</p>
 *
 * <p>This kind of parameter is only for reference. An use case is when you have several
 * job parameters, but your input values may vary depending on previous executions. You 
 * can get the previous executions by accessing from your Groovy code the jenkinsProject
 * variable.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class DynamicReferenceParameter extends AbstractCascadableParameter {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 8261526672604361397L;

    /**
     * Choice type.
     */
    private final String choiceType;

    private final Boolean omitValueField;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param randomName parameter random generated name (uuid)
     * @param script script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param omitValueField used in the UI to decide whether to include a hidden empty &lt;input name=value&gt;.
     * <code>false</code> by default.
     */
    @DataBoundConstructor
    public DynamicReferenceParameter(String name, String description, String randomName, Script script,
            String choiceType, String referencedParameters, Boolean omitValueField) {
        super(name, description, randomName, script, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return this.choiceType;
    }

    public Boolean getOmitValueField() {
        return omitValueField;
    }

    @JavaScriptMethod
    public String getChoicesAsStringForUI() {
        String result = getChoicesAsString(getParameters());
        return result;
    }

    // --- descriptor

    @Extension
    public static final class DescriptorImpl extends UnoChoiceParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Reactive Reference Parameter";
        }

        public FormValidation doCheckRequired(@QueryParameter String value) {
            String strippedValue = StringUtils.strip(value);
            if (StringUtils.isBlank(strippedValue)) {
                return FormValidation.error("This field is required.");
            }
            return FormValidation.ok();
        }

    }

}
