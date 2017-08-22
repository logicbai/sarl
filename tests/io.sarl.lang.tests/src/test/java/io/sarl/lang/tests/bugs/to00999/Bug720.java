/*
 * Copyright (C) 2014-2017 the original authors or authors.
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

package io.sarl.lang.tests.bugs.to00999;

import com.google.inject.Inject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.junit.Test;

import io.sarl.lang.SARLVersion;
import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.tests.api.AbstractSarlTest;

/** Testing class for issue: Incorrect generic method compilation.
 *
 * <p>https://github.com/sarl/sarl/issues/720
 *
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class Bug720 extends AbstractSarlTest {

	private static final String SNIPSET1 = multilineString(
			"package io.sarl.lang.tests.bug720",
			"capacity ExampleCapacity", 
			"{", 
			"    def exampleMethod(clazz : Class<T>) with T", 
			"}");

	private final String EXPECTED1 = multilineString(
			"package io.sarl.lang.tests.bug720;",
			"",
			"import io.sarl.lang.annotation.SarlElementType;", 
			"import io.sarl.lang.annotation.SarlSpecification;", 
			"import io.sarl.lang.core.AgentTrait;", 
			"import io.sarl.lang.core.Capacity;", 
			"", 
			"@FunctionalInterface", 
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING+ "\")", 
			"@SarlElementType(" + SarlPackage.SARL_CAPACITY + ")", 
			"@SuppressWarnings(\"all\")", 
			"public interface ExampleCapacity extends Capacity {", 
			"  public abstract <T extends Object> void exampleMethod(final Class<T> clazz);", 
			"  ", 
			"  public static class ContextAwareCapacityWrapper<C extends ExampleCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements ExampleCapacity {", 
			"    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {", 
			"      super(capacity, caller);", 
			"    }", 
			"    ", 
			"    public <T extends Object> void exampleMethod(final Class<T> clazz) {", 
			"      try {", 
			"        ensureCallerInLocalThread();", 
			"        this.capacity.exampleMethod(clazz);", 
			"      } finally {", 
			"        resetCallerInLocalThread();", 
			"      }", 
			"    }", 
			"  }", 
			"}",
			"");

	private static final String SNIPSET2 = multilineString(
			"package io.sarl.lang.tests.bug720",
			"capacity ExampleCapacity", 
			"{", 
			"    def exampleMethod(clazz : Class<T>) with T extends Number", 
			"}");

	private final String EXPECTED2 = multilineString(
			"package io.sarl.lang.tests.bug720;",
			"",
			"import io.sarl.lang.annotation.SarlElementType;", 
			"import io.sarl.lang.annotation.SarlSpecification;", 
			"import io.sarl.lang.core.AgentTrait;", 
			"import io.sarl.lang.core.Capacity;", 
			"", 
			"@FunctionalInterface", 
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING+ "\")", 
			"@SarlElementType(" + SarlPackage.SARL_CAPACITY + ")", 
			"@SuppressWarnings(\"all\")", 
			"public interface ExampleCapacity extends Capacity {", 
			"  public abstract <T extends Number> void exampleMethod(final Class<T> clazz);", 
			"  ", 
			"  public static class ContextAwareCapacityWrapper<C extends ExampleCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements ExampleCapacity {", 
			"    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {", 
			"      super(capacity, caller);", 
			"    }", 
			"    ", 
			"    public <T extends Number> void exampleMethod(final Class<T> clazz) {", 
			"      try {", 
			"        ensureCallerInLocalThread();", 
			"        this.capacity.exampleMethod(clazz);", 
			"      } finally {", 
			"        resetCallerInLocalThread();", 
			"      }", 
			"    }", 
			"  }", 
			"}",
			"");

	private static final String SNIPSET3 = multilineString(
			"package io.sarl.lang.tests.bug720",
			"capacity ExampleCapacity", 
			"{", 
			"    def exampleMethod(clazz : Class<T>, clazz2 : T2) with T extends Number, T2 extends T", 
			"}");

	private final String EXPECTED3 = multilineString(
			"package io.sarl.lang.tests.bug720;",
			"",
			"import io.sarl.lang.annotation.SarlElementType;", 
			"import io.sarl.lang.annotation.SarlSpecification;", 
			"import io.sarl.lang.core.AgentTrait;", 
			"import io.sarl.lang.core.Capacity;", 
			"", 
			"@FunctionalInterface", 
			"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING+ "\")", 
			"@SarlElementType(" + SarlPackage.SARL_CAPACITY + ")", 
			"@SuppressWarnings(\"all\")", 
			"public interface ExampleCapacity extends Capacity {", 
			"  public abstract <T extends Number, T2 extends T> void exampleMethod(final Class<T> clazz, final T2 clazz2);", 
			"  ", 
			"  public static class ContextAwareCapacityWrapper<C extends ExampleCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements ExampleCapacity {", 
			"    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {", 
			"      super(capacity, caller);", 
			"    }", 
			"    ", 
			"    public <T extends Number, T2 extends T> void exampleMethod(final Class<T> clazz, final T2 clazz2) {", 
			"      try {", 
			"        ensureCallerInLocalThread();", 
			"        this.capacity.exampleMethod(clazz, clazz2);", 
			"      } finally {", 
			"        resetCallerInLocalThread();", 
			"      }", 
			"    }", 
			"  }", 
			"}",
			"");

	@Inject
	private CompilationTestHelper compiler;

	@Test
	public void parsing_01() throws Exception {
		SarlScript mas = file(SNIPSET1);
		final Validator validator = validate(mas);
		validator.assertNoErrors();
	}

	@Test
	public void compiling_01() throws Exception {
		this.compiler.compile(SNIPSET1, (it) -> {
			final String actual = it.getGeneratedCode("io.sarl.lang.tests.bug720.ExampleCapacity");
			assertEquals(EXPECTED1, actual);
		});
	}

	@Test
	public void parsing_02() throws Exception {
		SarlScript mas = file(SNIPSET2);
		final Validator validator = validate(mas);
		validator.assertNoErrors();
	}

	@Test
	public void compiling_02() throws Exception {
		this.compiler.compile(SNIPSET2, (it) -> {
			final String actual = it.getGeneratedCode("io.sarl.lang.tests.bug720.ExampleCapacity");
			assertEquals(EXPECTED2, actual);
		});
	}

	@Test
	public void parsing_03() throws Exception {
		SarlScript mas = file(SNIPSET3);
		final Validator validator = validate(mas);
		validator.assertNoErrors();
	}

	@Test
	public void compiling_03() throws Exception {
		this.compiler.compile(SNIPSET3, (it) -> {
			final String actual = it.getGeneratedCode("io.sarl.lang.tests.bug720.ExampleCapacity");
			assertEquals(EXPECTED3, actual);
		});
	}

}