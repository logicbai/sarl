/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2018 the original authors or authors.
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

package io.sarl.lang.core.tests.scoping.extensions.numbers.cast.longprimitive;

import static io.sarl.lang.scoping.extensions.numbers.cast.PrimitiveLongCastExtensions.toAtomicDouble;
import static io.sarl.lang.scoping.extensions.numbers.cast.PrimitiveLongCastExtensions.toAtomicInteger;
import static io.sarl.lang.scoping.extensions.numbers.cast.PrimitiveLongCastExtensions.toAtomicLong;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDouble;
import org.junit.Test;

import io.sarl.tests.api.AbstractSarlTest;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see "https://github.com/eclipse/xtext-extras/issues/186"
 */
@SuppressWarnings("all")
public class CodeTest extends AbstractSarlTest {

	private static long left = 4;

	@Test
	public void toAtomicLong_long() {
		AtomicLong value = toAtomicLong(left);
		assertNotNull(value);
		assertEpsilonEquals(4., value.doubleValue());
	}

	@Test
	public void toAtomicInteger_long() {
		AtomicInteger value = toAtomicInteger(left);
		assertNotNull(value);
		assertEpsilonEquals(4., value.doubleValue());
	}

	@Test
	public void toAtomicDouble_long() {
		AtomicDouble value = toAtomicDouble(left);
		assertNotNull(value);
		assertEpsilonEquals(4., value.doubleValue());
	}

}
