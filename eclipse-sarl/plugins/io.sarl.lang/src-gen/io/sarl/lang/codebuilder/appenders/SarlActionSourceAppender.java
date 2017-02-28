/*
 * $Id$
 *
 * File is automatically generated by the Xtext language generator.
 * Do not change it.
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
package io.sarl.lang.codebuilder.appenders;

import io.sarl.lang.codebuilder.builders.IBlockExpressionBuilder;
import io.sarl.lang.codebuilder.builders.IFormalParameterBuilder;
import io.sarl.lang.codebuilder.builders.ISarlActionBuilder;
import io.sarl.lang.codebuilder.builders.ITypeParameterBuilder;
import io.sarl.lang.sarl.SarlAction;
import java.io.IOException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Pure;

/** Source appender of a Sarl SarlAction.
 */
@SuppressWarnings("all")
public class SarlActionSourceAppender extends AbstractSourceAppender implements ISarlActionBuilder {

	private final ISarlActionBuilder builder;

	public SarlActionSourceAppender(ISarlActionBuilder builder) {
		this.builder = builder;
	}

	public void build(ISourceAppender appender) throws IOException {
		build(this.builder.getSarlAction(), appender);
	}

	/** Dispose the resource.
	 */
	public void dispose() {
		this.builder.dispose();
	}

	public IJvmTypeProvider getTypeResolutionContext() {
		return this.builder.getTypeResolutionContext();
	}

	/** Initialize the Ecore element.
	 * @param container - the container of the SarlAction.
	 * @param name - the name of the SarlAction.
	 */
	public void eInit(XtendTypeDeclaration container, String name, String modifier, IJvmTypeProvider context) {
		this.builder.eInit(container, name, modifier, context);
	}

	/** Replies the generated element.
	 */
	@Pure
	public SarlAction getSarlAction() {
		return this.builder.getSarlAction();
	}

	/** Replies the resource.
	 */
	@Pure
	public Resource eResource() {
		return getSarlAction().eResource();
	}

	/** Change the documentation of the element.
	 *
	 * <p>The documentation will be displayed just before the element.
	 *
	 * @param doc the documentation.
	 */
	public void setDocumentation(String doc) {
		this.builder.setDocumentation(doc);
	}

	/** Add a formal parameter.
	 * @param name the name of the formal parameter.
	 */
	public IFormalParameterBuilder addParameter(String name) {
		return this.builder.addParameter(name);
	}

	/** Add a throwable exception.
	 * @param type the fully qualified name of the exception.
	 */
	public void addException(String type) {
		this.builder.addException(type);
	}

	/** Add a fired exception.
	 * @param type the fully qualified name of the event.
	 */
	public void addFiredEvent(String type) {
		this.builder.addFiredEvent(type);
	}

	/** Change the return type.
	 @param type the return type of the member.
	 */
	public void setReturnType(String type) {
		this.builder.setReturnType(type);
	}

	/** Create the block of code.
	 * @return the block builder.
	 */
	public IBlockExpressionBuilder getExpression() {
		return this.builder.getExpression();
	}

	/** Add an annotation.
	 * @param type the qualified name of the annotation
	 */
	public void addAnnotation(String type) {
		this.builder.addAnnotation(type);
	}

	/** Add a modifier.
	 * @param modifier - the modifier to add.
	 */
	public void addModifier(String modifier) {
		this.builder.addModifier(modifier);
	}

	@Override
	@Pure
	public String toString() {
		return this.builder.toString();
	}

	/** Add a type parameter.
	 * @param name - the simple name of the type parameter.
	 * @return the builder of type parameter.
	 */
	public ITypeParameterBuilder addTypeParameter(String name) {
		return this.builder.addTypeParameter(name);
	}

}

