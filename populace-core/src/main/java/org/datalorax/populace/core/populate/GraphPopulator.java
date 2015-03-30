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

package org.datalorax.populace.core.populate;

import org.apache.commons.lang3.Validate;
import org.datalorax.populace.core.populate.instance.InstanceFactories;
import org.datalorax.populace.core.populate.mutator.Mutators;
import org.datalorax.populace.core.walk.GraphWalker;
import org.datalorax.populace.core.walk.element.ElementInfo;
import org.datalorax.populace.core.walk.field.FieldInfo;
import org.datalorax.populace.core.walk.field.filter.FieldFilter;
import org.datalorax.populace.core.walk.inspector.Inspectors;
import org.datalorax.populace.core.walk.visitor.ElementVisitor;
import org.datalorax.populace.core.walk.visitor.FieldVisitor;
import org.datalorax.populace.core.walk.visitor.FieldVisitors;
import org.datalorax.populace.core.walk.visitor.SetAccessibleFieldVisitor;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Given an instance, it will populate all fields, recursively, with values.
 *
 * @author Andrew Coates - 25/02/2015.
 */
public final class GraphPopulator {
    private final GraphWalker walker;
    private final PopulatorContext config;

    GraphPopulator(final GraphWalker walker, final PopulatorContext config) {
        Validate.notNull(walker, "walker null");
        Validate.notNull(config, "config null");
        this.config = config;
        this.walker = walker;
    }

    public static Builder newBuilder() {
        return new GraphPopulatorBuilder();
    }

    private static boolean isNotInnerClass(final Class<?> type) {
        return type.getEnclosingClass() == null || Modifier.isStatic(type.getModifiers());
    }

    // Todo(ac): needs a TypeReference<T> parameter...
    public <T> T populate(final T instance) {
        final Visitor visitor = new Visitor();
        final FieldVisitor fieldVisitor = FieldVisitors.chain(SetAccessibleFieldVisitor.INSTANCE, visitor);
        walker.walk(instance, fieldVisitor, visitor);
        return instance;
    }

    public <T> T populate(final Class<T> type) {
        Validate.isTrue(isNotInnerClass(type), "Non-static inner classes are not supported");
        final T instance = createInstance(type);
        return populate(instance);
    }

    public PopulatorContext getConfig() {
        return config;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GraphPopulator that = (GraphPopulator) o;
        return config.equals(that.config) && walker.equals(that.walker);
    }

    @Override
    public int hashCode() {
        int result = walker.hashCode();
        result = 31 * result + config.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GraphPopulator{" +
            "walker=" + walker +
            ", config=" + config +
            '}';
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(final Class<T> type) {
        return (T) config.createInstance(type, null);
    }

    public interface Builder {
        // Todo(ac): these style interfaces should expose and accept builders, not built types
        Builder withFieldFilter(final FieldFilter filter);
        FieldFilter getFieldFilter();

        Builder withInspectors(final Inspectors inspectors);

        Inspectors.Builder inspectorsBuilder();

        Builder withMutators(final Mutators mutators);

        Mutators.Builder mutatorsBuilder();

        Builder withInstanceFactories(final InstanceFactories instanceFactories);

        InstanceFactories.Builder instanceFactoriesBuilder();

        GraphPopulator build();
    }

    private class Visitor implements FieldVisitor, ElementVisitor {
        @Override
        public void visit(final FieldInfo field) {
            try {
                final Type type = field.getGenericType();
                final Object currentValue = field.getValue();
                // Todo(ac): Get mutator based on field type or currentValue type?
                final Mutator mutator = config.getMutator(type);
                final Object mutated = mutator.mutate(type, currentValue, field.getOwningInstance(), config);
                if (mutated != currentValue) {
                    field.setValue(mutated);
                }
            } catch (Exception e) {
                throw new PopulatorException("Failed to populate field: " + field, e);
            }
        }

        @Override
        public void visit(final ElementInfo element) {
            try {
                final Type type = element.getGenericType();
                final Object currentValue = element.getValue();
                // Todo(ac): Get mutator based on collection field type or currentValue type?
                final Mutator mutator = config.getMutator(type);
                final Object mutated = mutator.mutate(type, currentValue, null, config);
                if (mutated != currentValue) {
                    element.setValue(mutated);
                }
            } catch (Exception e) {
                throw new PopulatorException("Failed to populate element: " + element, e);
            }
        }
    }
}
