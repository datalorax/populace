package org.datalorax.populace.populator;

import org.apache.commons.lang3.Validate;
import org.datalorax.populace.field.FieldInfo;
import org.datalorax.populace.field.filter.FieldFilter;
import org.datalorax.populace.field.visitor.FieldVisitor;
import org.datalorax.populace.field.visitor.FieldVisitors;
import org.datalorax.populace.field.visitor.SetAccessibleFieldVisitor;
import org.datalorax.populace.graph.GraphWalker;
import org.datalorax.populace.graph.inspector.Inspectors;
import org.datalorax.populace.populator.instance.InstanceFactories;
import org.datalorax.populace.populator.mutator.Mutators;

import java.lang.reflect.Type;

/**
 * Given an instance, it will populate all fields, recursively, with values.
 *
 * @author datalorax - 25/02/2015.
 */
public final class GraphPopulator {
    private final GraphWalker walker;
    private final PopulatorContext config;

    public static Builder newBuilder() {
        return new GraphPopulatorBuilder();
    }

    public interface Builder {
        // Todo(ac): not nice - client automatically looses default filters, mutators, etc. Either add docs about using *Utils.defaults() or have better pattern. (Also rename *Utils to *s)
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

    // Todo(ac): needs a TypeReference<T> parameter...
    public <T> T populate(final T instance) {
        walker.walk(instance, FieldVisitors.chain(SetAccessibleFieldVisitor.INSTANCE, new Visitor()));  // Todo(ac): this overrides any configured field filter... need to get current
        return instance;
    }

    public <T> T populate(final Class<T> type) {
        //noinspection unchecked
        final T instance = (T) config.createInstance(type, null);
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

    GraphPopulator(final GraphWalker walker, final PopulatorContext config) {
        Validate.notNull(walker, "walker null");
        Validate.notNull(config, "config null");
        this.config = config;
        this.walker = walker;
    }

    private class Visitor implements FieldVisitor {
        @Override
        public void visit(final FieldInfo field) {
            final Type type = field.getGenericType();
            final Object currentValue = field.getValue();
            // Todo(ac): Add debug log line here...
            final Mutator mutator = config.getMutator(type);
            final Object mutated = mutator.mutate(type, currentValue, field.getOwningInstance(), config);
            if (mutated != currentValue) {
                field.setValue(mutated);
            }
        }
    }
}

// Todo(ac): Instance factory for BigDecimal