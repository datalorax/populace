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

package org.datalorax.populace.core.walk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datalorax.populace.core.walk.field.FieldInfo;
import org.datalorax.populace.core.walk.field.PathProvider;
import org.datalorax.populace.core.walk.field.RawField;
import org.datalorax.populace.core.walk.field.filter.FieldFilter;
import org.datalorax.populace.core.walk.inspector.Inspector;
import org.datalorax.populace.core.walk.inspector.Inspectors;
import org.datalorax.populace.core.walk.visitor.FieldVisitor;

/**
 * Type that recursively walks an object graph, calling back to the client code on the provided
 * {@link org.datalorax.populace.core.walk.visitor.FieldVisitor field visitor} interface.
 *
 * @author Andrew Coates - 28/02/2015.
 */
public class GraphWalker {
    private static final Log LOG = LogFactory.getLog(GraphWalker.class);

    private final WalkerContext context;

    /**
     * Construct using the {@link GraphWalker.Builder builder}, which can
     * be obtained via {@link #newBuilder()}
     *
     * @param context the walkers configuration
     */
    GraphWalker(final WalkerContext context) {
        this.context = context;
    }

    /**
     * Obtain a builder capable of configuring and building an immutable {@link GraphWalker} instance. The builder returned
     * is already configured with a sensible defaults, but these can be overridden or augmented as required.
     *
     * @return the new {@link GraphWalker.Builder builder} instance.
     */
    public static Builder newBuilder() {
        return new GraphWalkerBuilder();
    }

    private static void logDebug(String message, PathProvider path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(path.getPath() + " - " + message);
        }
    }

    private static void logInfo(String message, PathProvider path) {
        if (LOG.isInfoEnabled()) {
            LOG.info(path.getPath() + " - " + message);
        }
    }

    /**
     * Recursively walk the fields on {@code instance} and any objects it links too, calling back on {@code visitor} for
     * each field as it is discovered.
     *
     * @param instance the instance to walk
     * @param visitor  the visitor to call back on.
     */
    public void walk(final Object instance, final FieldVisitor visitor) {
        walk(instance, visitor, WalkerStack.newStack(instance));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GraphWalker that = (GraphWalker) o;
        return context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return context.hashCode();
    }

    @Override
    public String toString() {
        return "GraphWalker{" +
            "context=" + context +
            '}';
    }

    private void walk(final Object instance, final FieldVisitor visitor, final WalkerStack stack) {
        final Inspector inspector = context.getInspector(instance.getClass());
        logInfo("Walking type: " + instance.getClass() + ", inspector: " + inspector.getClass(), stack);

        walkFields(instance, visitor, inspector, stack);
        walkChildren(instance, visitor, stack, inspector);
    }

    private void walkFields(final Object instance, final FieldVisitor visitor, final Inspector inspector, final WalkerStack instanceStack) {
        final Iterable<RawField> fields = inspector.getFields(instance.getClass(), context.getInspectors());
        if (!fields.iterator().hasNext()) {
            return;
        }

        logInfo("Walking fields of type: " + instance.getClass() + ", inspector: " + inspector.getClass(), instanceStack);

        for (RawField field : fields) {
            final WalkerStack fieldStack = instanceStack.push(field);
            final FieldInfo fieldInfo = new FieldInfo(field, instance, fieldStack, fieldStack);

            if (context.isExcludedField(fieldInfo)) {
                logDebug("Skipping excluded field: " + fieldInfo.getName(), fieldStack);
                continue;
            }

            logInfo("Visiting field: " + fieldInfo, fieldStack);

            try {
                visitor.visit(fieldInfo);
            } catch (Exception e) {
                throw new WalkerException("Visitor threw exception while visiting field.", fieldStack, e);
            }

            final Object value = fieldInfo.getValue();
            if (value == null) {
                logDebug("Skipping null field: " + fieldInfo.getName(), fieldStack);
                continue;
            }

            walk(value, visitor, fieldStack);
        }
    }

    private void walkChildren(final Object instance, final FieldVisitor visitor, final WalkerStack stack, final Inspector inspector) {
        final Iterable<?> children = inspector.getChildren(instance);
        if (!children.iterator().hasNext()) {
            return;
        }

        logInfo("Walking children of type: " + instance.getClass() + ", inspector: " + inspector.getClass(), stack);

        for (Object child : children) {
            if (child == null) {
                logDebug("Skipping null child", stack);
                continue;
            }

            walk(child, visitor, stack.push(child));
        }
    }

    public interface Builder {
        /**
         * Install a filter to exclude some types of fields from the walk.
         * <p>
         * This replaces the currently installed filter. The existing filter can be obtained by calling
         * {@link #getFieldFilter()}. Filters can be combined using the functions in
         * {@link org.datalorax.populace.core.walk.field.filter.FieldFilters}, such as logical
         * {@link org.datalorax.populace.core.walk.field.filter.FieldFilters#and AND} and
         * {@link org.datalorax.populace.core.walk.field.filter.FieldFilters#or OR}
         *
         * @param filter the filter to install.
         * @return the builder
         */
        Builder withFieldFilter(final FieldFilter filter);

        /**
         * @return obtain the currently installed field filter.
         */
        FieldFilter getFieldFilter();

        /**
         * Install the inspectors that will be used to inspect each object that is encountered when walking the graph.
         * {@link org.datalorax.populace.core.walk.inspector.Inspector inspectors} are used to obtain that set of fields
         * an object has and, for container types, the child objects it contains.
         * <p>
         * This call replaces the currently installer set of
         * {@link org.datalorax.populace.core.walk.inspector.Inspector inspectors}. A builder to manipulate the existing
         * {@link org.datalorax.populace.core.walk.inspector.Inspectors} can be obtained by calling {@link #inspectorsBuilder()}
         *
         * @param inspectors the set of inspectors to install.
         * @return the builder itself
         */
        Builder withInspectors(final Inspectors inspectors);

        /**
         * Obtain a {@link org.datalorax.populace.core.walk.inspector.Inspectors.Builder builder} initialised with the currently
         * installed {@link org.datalorax.populace.core.walk.inspector.Inspector inspectors}. The builder can be used to build
         * a modified set of inspectors by either adding new, or replacing existing, inspectors.
         *
         * @return the inspectors builder
         */
        Inspectors.Builder inspectorsBuilder();

        /**
         * Build an immutable instance of {@link GraphWalker} from the configuration provided.
         *
         * @return the newly constructed {@link GraphWalker} instance.
         */
        GraphWalker build();
    }
}