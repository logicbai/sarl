/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2017 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.pythongenerator.validator;

import java.text.MessageFormat;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.featurecalls.IdentifiableSimpleNameProvider;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure3;
import org.eclipse.xtext.xtype.XImportDeclaration;

import io.sarl.lang.generator.extra.IExtraLanguageConversionInitializer;
import io.sarl.lang.validation.extra.AbstractExtraLanguageValidator;
import io.sarl.pythongenerator.PyGeneratorPlugin;
import io.sarl.pythongenerator.generator.PyInitializers;

/** The validator from SARL to the Python target language.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.6
 */
public class PyValidator extends AbstractExtraLanguageValidator {

	/** Error handler for the type conversions.
	 */
	private final Procedure3<EObject, JvmType, String> typeErrorHandler = (source, invalidType, name) -> {
		error(MessageFormat.format(Messages.PyValidator_0, name), source);
	};

	/** Error handler for the feature conversions.
	 */
	private final Function2<EObject, JvmIdentifiableElement, Boolean> featureErrorHandler = (source, element) -> {
		final String message;
		if (element instanceof JvmConstructor) {
			message = MessageFormat.format(Messages.PyValidator_1, this.simpleNameProvider.getSimpleName(element));
		} else if (element instanceof JvmField) {
			message = MessageFormat.format(Messages.PyValidator_2, this.simpleNameProvider.getSimpleName(element));
		} else if (element instanceof JvmOperation) {
			message = MessageFormat.format(Messages.PyValidator_3, this.simpleNameProvider.getSimpleName(element));
		} else {
			// This type of Jvm element is not supposed to be converted
			return false;
		}
		error(message, source);
		return true;
	};

	@Inject
	private IdentifiableSimpleNameProvider simpleNameProvider;

	@Override
	protected IExtraLanguageConversionInitializer getTypeConverterInitializer() {
		return PyInitializers.getTypeConverterInitializer();
	}

	@Override
	protected IExtraLanguageConversionInitializer getFeatureConverterInitializer() {
		return PyInitializers.getFeatureNameConverterInitializer();
	}

	@Override
	protected String getGeneratorPluginID() {
		return PyGeneratorPlugin.PLUGIN_ID;
	}

	@Override
	protected String getErrorMessageFormat() {
		return Messages.PyValidator_4;
	}

	/** Check that import mapping are known.
	 *
	 * @param importDeclaration the declaration.
	 */
	@Check
	public void checkImportsMapping(XImportDeclaration importDeclaration) {
		final JvmDeclaredType type = importDeclaration.getImportedType();
		doTypeMappingCheck(importDeclaration, type, this.typeErrorHandler);
	}

	/** Check that member feature calls have a conversion mapping.
	 *
	 * @param featureCall the feature call.
	 */
	@Check
	public void checkMemberFeatureCallMapping(XMemberFeatureCall featureCall) {
		doCheckMemberFeatureCallMapping(featureCall, this.typeErrorHandler, this.featureErrorHandler);
	}

	/** Check that member feature calls have a convertion mapping.
	 *
	 * @param featureCall the feature call.
	 */
	@Check
	public void checkMemberFeatureCallMapping(XFeatureCall featureCall) {
		doCheckMemberFeatureCallMapping(featureCall, this.typeErrorHandler, this.featureErrorHandler);
	}

}
