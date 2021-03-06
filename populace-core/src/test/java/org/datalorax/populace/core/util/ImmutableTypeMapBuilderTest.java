/*
 * Copyright (c) 2015 Andrew Coates
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

package org.datalorax.populace.core.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ImmutableTypeMapBuilderTest {
    private ImmutableTypeMapBuilder<String> builder;

    @BeforeMethod
    public void setUp() throws Exception {
        builder = new ImmutableTypeMapBuilder<>("initial default");
    }

    @Test
    public void shouldAlwaysHaveDefault() throws Exception {
        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(int.class), is("initial default"));
    }

    @Test
    public void shouldInstallSpecificType() throws Exception {
        // Given:
        final String specific = "int";

        // When:
        builder.withSpecificType(int.class, specific);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(int.class), is(specific));
    }

    @Test
    public void shouldOverrideExistingSpecificType() throws Exception {
        // Given:
        final String specific = "int";
        final String override = "int override";
        builder.withSpecificType(String.class, specific);

        // When:
        builder.withSpecificType(int.class, override);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(int.class), is(override));
    }

    @Test
    public void shouldInstallSuperType() throws Exception {
        // Given:
        final String superType = "super type";

        // When:
        builder.withSuperType(Map.class, superType);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(HashMap.class), is(superType));
    }

    @Test
    public void shouldOverrideExistingSuperType() throws Exception {
        // Given:
        final String superType = "super type";
        final String override = "super type override";
        builder.withSuperType(Set.class, superType);

        // When:
        builder.withSuperType(Set.class, override);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(HashSet.class), is(override));
    }

    @Test
    public void shouldInstallDefault() throws Exception {
        // Given:
        final Type unregisteredType = ImmutableTypeMapBuilder.class;
        final String defaultV = "default";

        // When:
        builder.withDefault(defaultV);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(unregisteredType), is(defaultV));
    }

    @Test
    public void shouldInstallDefaultArray() throws Exception {
        // Given:
        final Type arrayType = TypeUtils.genericArrayType(int.class);
        final String defaultA = "default array";

        // When:
        builder.withArrayDefault(defaultA);

        // Then:
        final ImmutableTypeMap<String> collection = builder.build();
        assertThat(collection.get(arrayType), is(defaultA));
    }
}