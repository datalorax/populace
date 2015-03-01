package org.datalorax.populace.populator.typed;


import org.apache.commons.lang3.reflect.TypeUtils;
import org.testng.annotations.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author datalorax - 27/02/2015.
 */
public class TypedCollectionTest {
    private static final Map<Type, String> NO_SPECIFIC_VALUES = Collections.emptyMap();
    private static final Map<Class<?>, String> NO_BASE_VALUES = Collections.emptyMap();
    private static final String defaultValue = "default";
    private static final String defaultArrayValue = "default array";

    @Test
    public void shouldGetDefaultValueForNonArrayKeyIfNothingElseMatches() throws Exception {
        // Given:
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSpecificType(String.class, "String")
                .withSuperType(List.class, "List")
                .withDefault(defaultValue)
                .build();

        // When:
        final String value = collection.get(int.class);

        // Then:
        assertThat(value, is(defaultValue));
    }

    @Test
    public void shouldGetDefaultArrayValueForArrayKeyIfNothingElseMatches() throws Exception {
        // Given:
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSpecificType(String.class, "String")
                .withSuperType(List.class, "List")
                .withDefaultArray(defaultArrayValue)
                .build();

        // When:
        final String value = collection.get(TypeUtils.genericArrayType(long.class));

        // Then:
        assertThat(value, is(defaultArrayValue));
    }

    @Test
    public void shouldGetSpecificOverDefaultValue() throws Exception {
        // Given:
        final ParameterizedType specificType = TypeUtils.parameterize(HashSet.class, String.class);
        final String specificValue = "HashSet<String>";
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSpecificType(specificType, specificValue)
                .withDefault(defaultValue)
                .build();

        // When:
        final String value = collection.get(specificType);

        // Then:
        assertThat(value, is(specificValue));
    }

    @Test
    public void shouldGetSpecificOverSuperValue() throws Exception {
        // Given:
        final ParameterizedType specificType = TypeUtils.parameterize(HashSet.class, String.class);
        final String specificValue = "HashSet<String>";
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSpecificType(specificType, specificValue)
                .withSuperType(Set.class, "Set")
                .build();

        // When:
        final String value = collection.get(specificType);

        // Then:
        assertThat(value, is(specificValue));
    }

    @Test
    public void shouldGetSuperOverDefaultValue() throws Exception {
        // Given:
        final String superValue = "Set";
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSuperType(Set.class, "Set")
                .withDefault(defaultValue)
                .build();

        // When:
        final String value = collection.get(HashSet.class);

        // Then:
        assertThat(value, is(superValue));
    }

    @Test
    public void shouldPickMostSpecificSuperValue() throws Exception {
        // Given:
        final String mostSpecificValue = "Set";
        final TypedCollection<String> collection = TypedCollection.<String>newBuilder()
                .withSuperType(Collection.class, "collection")
                .withSuperType(Set.class, mostSpecificValue)
                .build();

        // When:
        final String value = collection.get(HashSet.class);

        // Then:
        assertThat(value, is(mostSpecificValue));
    }

    @Test
    public void shouldPickSameSuperValueRegardlessOfOrder() throws Exception {
        // Given:
        final String mostSpecificValue = "Set";
        final Map<Class<?>, String> naturalSuperValues = new TreeMap<Class<?>, String>(new ClassComparator()) {{
            put(Collection.class, "Collection");
            put(Set.class, mostSpecificValue);
        }};
        final Map<Class<?>, String> reversedSuperValues = new TreeMap<Class<?>, String>(new ClassComparator().reversed()) {{
            put(Collection.class, "Collection");
            put(Set.class, mostSpecificValue);
        }};
        final TypedCollection<String> natural = new TypedCollection<String>(NO_SPECIFIC_VALUES, naturalSuperValues, null, null);
        final TypedCollection<String> reversed = new TypedCollection<String>(NO_SPECIFIC_VALUES, reversedSuperValues, null, null);

        // When:
        final String nValue = natural.get(TypeUtils.parameterize(HashSet.class, String.class));
        final String rValue = reversed.get(TypeUtils.parameterize(HashSet.class, String.class));

        // Then:
        assertThat(nValue, is(rValue));
    }

    private static class ClassComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> c1, Class<?> c2) {
            return c1.getCanonicalName().compareTo(c2.getCanonicalName());
        }
    }
}