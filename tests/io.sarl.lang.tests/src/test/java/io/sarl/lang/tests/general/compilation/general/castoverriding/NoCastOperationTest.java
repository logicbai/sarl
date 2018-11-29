/*
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.tests.general.compilation.general.castoverriding;

import org.eclipse.xtext.common.types.TypesPackage;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.sarl.lang.SARLVersion;
import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.tests.api.AbstractSarlTest;
import io.sarl.tests.api.MassiveCompilationSuite;
import io.sarl.tests.api.MassiveCompilationSuite.CompilationTest;
import io.sarl.tests.api.MassiveCompilationSuite.Context;


/**
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.9
 */
@RunWith(MassiveCompilationSuite.class)
@SuppressWarnings("all")
public class NoCastOperationTest extends AbstractSarlTest {

	private static final String PRIMITIVE_PRIMITIVE_SARL_00 = multilineString(
			"class A0 {",
			"  def fct(x : double) : long {",
			"    return x as long",
			"  }",
			"}");

	private static final String PRIMITIVE_PRIMITIVE_JAVA_00 = multilineString(
			"import io.sarl.lang.annotation.SarlElementType;",
			"import io.sarl.lang.annotation.SarlSpecification;",
			"import io.sarl.lang.annotation.SyntheticMember;",
			"import org.eclipse.xtext.xbase.lib.Pure;",
			"",
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
			"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
			"@SuppressWarnings(\"all\")",
			"public class A0 {",
			"  @Pure",
			"  public long fct(final double x) {",
			"    return ((long) x);",
			"  }",
			"  ",
			"  @SyntheticMember",
			"  public A0() {",
			"    super();",
			"  }",
			"}",
			"");

	@Test
	public void parsePrimitivePrimitive00() throws Exception {
		SarlScript mas = file(PRIMITIVE_PRIMITIVE_SARL_00);
		Validator val = validate(mas);
		val.assertNoIssues();
	}

	@CompilationTest
	public static void compilePrimitivePrimitive00(Context ctx) throws Exception {
		ctx.compileTo(PRIMITIVE_PRIMITIVE_SARL_00, PRIMITIVE_PRIMITIVE_JAVA_00);
	}

	private static final String PRIMITIVE_PRIMITIVE_SARL_01 = multilineString(
			"class A0 {",
			"  def fct(x : long) : double {",
			"    return x as double",
			"  }",
			"}"
			);

	private static final String PRIMITIVE_PRIMITIVE_JAVA_01 = multilineString(
			"import io.sarl.lang.annotation.SarlElementType;",
			"import io.sarl.lang.annotation.SarlSpecification;",
			"import io.sarl.lang.annotation.SyntheticMember;",
			"import org.eclipse.xtext.xbase.lib.Pure;",
			"",
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
			"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
			"@SuppressWarnings(\"all\")",
			"public class A0 {",
			"  @Pure",
			"  public double fct(final long x) {",
			"    return x;",
			"  }",
			"  ",
			"  @SyntheticMember",
			"  public A0() {",
			"    super();",
			"  }",
			"}",
			"");

	@Test
	public void parsePrimitivePrimitive01() throws Exception {
		SarlScript mas = file(PRIMITIVE_PRIMITIVE_SARL_01);
		Validator val = validate(mas);
		val.assertNoErrors();
		val.assertWarning(
				TypesPackage.eINSTANCE.getJvmParameterizedTypeReference(),
				org.eclipse.xtext.xbase.validation.IssueCodes.OBSOLETE_CAST,
				"Unnecessary cast from long to double");
	}

	@CompilationTest
	public static void compilePrimitivePrimitive01(Context ctx) throws Exception {
		ctx.compileTo(PRIMITIVE_PRIMITIVE_SARL_01, PRIMITIVE_PRIMITIVE_JAVA_01);
	}

	private static final String SUBTYPE_TYPE_SARL_00 = multilineString(
			"class A0 {}",
			"class A1 extends A0 {}",
			"class A2 {",
			"  def fct(x : A1) : A0 {",
			"    return x as A0",
			"  }",
			"}"
			);

	private static final String SUBTYPE_TYPE_JAVA_00 = multilineString(
			"import io.sarl.lang.annotation.SarlElementType;",
			"import io.sarl.lang.annotation.SarlSpecification;",
			"import io.sarl.lang.annotation.SyntheticMember;",
			"import io.sarl.lang.core.tests.compileSubtypeType00.A0;",
			"import io.sarl.lang.core.tests.compileSubtypeType00.A1;",
			"import org.eclipse.xtext.xbase.lib.Pure;",
			"",
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
			"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
			"@SuppressWarnings(\"all\")",
			"public class A2 {",
			"  @Pure",
			"  public A0 fct(final A1 x) {",
			"    return x;",
			"  }",
			"  ",
			"  @SyntheticMember",
			"  public A2() {",
			"    super();",
			"  }",
			"}",
			"");

	@Test
	public void parseSubtypeType00() throws Exception {
		SarlScript mas = file(SUBTYPE_TYPE_SARL_00);
		Validator val = validate(mas);
		val.assertNoErrors();
		val.assertWarning(
				TypesPackage.eINSTANCE.getJvmParameterizedTypeReference(),
				org.eclipse.xtext.xbase.validation.IssueCodes.OBSOLETE_CAST,
				"Unnecessary cast from A1 to A0");
	}

	@CompilationTest
	public static void compileSubtypeType00(Context ctx) throws Exception {
		ctx.compileTo(SUBTYPE_TYPE_SARL_00, SUBTYPE_TYPE_JAVA_00);
	}

	private static final String PRIMITIVE_WRAPPER_SARL_00 = multilineString(
			"class A0 {",
			"  def fct(x : double) : Double {",
			"    return x as Double",
			"  }",
			"}"
			);

	private static final String PRIMITIVE_WRAPPER_JAVA_00 = multilineString(
			"import io.sarl.lang.annotation.SarlElementType;",
			"import io.sarl.lang.annotation.SarlSpecification;",
			"import io.sarl.lang.annotation.SyntheticMember;",
			"import org.eclipse.xtext.xbase.lib.Pure;",
			"",
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
			"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
			"@SuppressWarnings(\"all\")",
			"public class A0 {",
			"  @Pure",
			"  public Double fct(final double x) {",
			"    return Double.valueOf(x);",
			"  }",
			"  ",
			"  @SyntheticMember",
			"  public A0() {",
			"    super();",
			"  }",
			"}",
			"");

	@Test
	public void parsePrimitiveWrapper00() throws Exception {
		SarlScript mas = file(PRIMITIVE_WRAPPER_SARL_00);
		Validator val = validate(mas);
		val.assertNoErrors();
		val.assertWarning(
				TypesPackage.eINSTANCE.getJvmParameterizedTypeReference(),
				org.eclipse.xtext.xbase.validation.IssueCodes.OBSOLETE_CAST,
				"Unnecessary cast from double to Double");
	}

	@CompilationTest
	public static void compilePrimitiveWrapper00(Context ctx) throws Exception {
		ctx.compileTo(PRIMITIVE_WRAPPER_SARL_00, PRIMITIVE_WRAPPER_JAVA_00);
	}

	private static final String WRAPPER_PRIMITIVE_SARL_00 = multilineString(
			"class A0 {",
			"  def fct(x : Double) : double {",
			"    return x as double",
			"  }",
			"}"
			);

	private static final String WRAPPER_PRIMITIVE_JAVA_00 = multilineString(
			"import io.sarl.lang.annotation.SarlElementType;",
			"import io.sarl.lang.annotation.SarlSpecification;",
			"import io.sarl.lang.annotation.SyntheticMember;",
			"import org.eclipse.xtext.xbase.lib.Pure;",
			"",
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
			"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
			"@SuppressWarnings(\"all\")",
			"public class A0 {",
			"  @Pure",
			"  public double fct(final Double x) {",
			"    return (x).doubleValue();",
			"  }",
			"  ",
			"  @SyntheticMember",
			"  public A0() {",
			"    super();",
			"  }",
			"}",
			"");

	@Test
	public void parseWrapperPrimitive00() throws Exception {
		SarlScript mas = file(WRAPPER_PRIMITIVE_SARL_00);
		Validator val = validate(mas);
		val.assertNoErrors();
		val.assertWarning(
				TypesPackage.eINSTANCE.getJvmParameterizedTypeReference(),
				org.eclipse.xtext.xbase.validation.IssueCodes.OBSOLETE_CAST,
				"Unnecessary cast from Double to double");
	}

	@CompilationTest
	public static void compileWrapperPrimitive00(Context ctx) throws Exception {
		ctx.compileTo(WRAPPER_PRIMITIVE_SARL_00, WRAPPER_PRIMITIVE_JAVA_00);
	}

}
