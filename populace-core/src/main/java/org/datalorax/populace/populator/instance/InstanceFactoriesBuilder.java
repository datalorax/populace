package org.datalorax.populace.populator.instance;

import org.datalorax.populace.type.TypeUtils;
import org.datalorax.populace.typed.ImmutableTypeMap;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Helper functions for working with {@link org.datalorax.populace.populator.instance.InstanceFactory instance factories}
 *
 * @author datalorax - 02/03/2015.
 */
final class InstanceFactoriesBuilder implements InstanceFactories.Builder {
    private static final InstanceFactories DEFAULT;

    private InstanceFactory nullObjectFactory = NullInstanceFactory.INSTANCE;
    private final ImmutableTypeMap.Builder<InstanceFactory> factoriesBuilder;

    public static InstanceFactories.Builder defaults() {
        return InstanceFactories.asBuilder(DEFAULT);
    }

    public static InstanceFactory chain(final ChainableInstanceFactory first, final InstanceFactory second) {
        return ChainedInstanceFactory.chain(first, second);
    }

    public static ChainableInstanceFactory chain(final ChainableInstanceFactory first, final ChainableInstanceFactory second) {
        return ChainedInstanceFactory.chain(first, second);
    }

    @Override
    public InstanceFactories.Builder withSpecificFactories(final Map<Type, ? extends InstanceFactory> factories) {
        factoriesBuilder.withSpecificTypes(factories);
        return this;
    }

    @Override
    public InstanceFactories.Builder withSpecificFactory(final Type type, final InstanceFactory factory) {
        factoriesBuilder.withSpecificType(type, factory);
        return this;
    }

    @Override
    public InstanceFactories.Builder withSuperFactories(final Map<Class<?>, ? extends InstanceFactory> factories) {
        factoriesBuilder.withSuperTypes(factories);
        return this;
    }

    @Override
    public InstanceFactories.Builder withSuperFactory(final Class<?> baseClass, final InstanceFactory factory) {
        factoriesBuilder.withSuperType(baseClass, factory);
        return this;
    }

    @Override
    public InstanceFactories.Builder withArrayDefaultFactory(final InstanceFactory factory) {
        factoriesBuilder.withArrayDefault(factory);
        return this;
    }

    @Override
    public InstanceFactories.Builder withDefaultFactory(final InstanceFactory factory) {
        factoriesBuilder.withDefault(factory);
        return this;
    }

    @Override
    public InstanceFactories.Builder withNullObjectFactory(final InstanceFactory factory) {
        nullObjectFactory = factory;
        return this;
    }

    @Override
    public InstanceFactories build() {
        return new InstanceFactories(nullObjectFactory, factoriesBuilder.build());
    }

    InstanceFactoriesBuilder(final InstanceFactory nullObjectFactory, final ImmutableTypeMap<InstanceFactory> factories) {
        this.nullObjectFactory = nullObjectFactory;
        this.factoriesBuilder = ImmutableTypeMap.asBuilder(factories);
    }

    private InstanceFactoriesBuilder() {
        this.factoriesBuilder = ImmutableTypeMap.newBuilder();
    }

    static {
        final InstanceFactoriesBuilder builder = new InstanceFactoriesBuilder();

        TypeUtils.getPrimitiveTypes().forEach(type -> builder.withSpecificFactory(type, PrimitiveInstanceFactory.INSTANCE));
        TypeUtils.getBoxedPrimitiveTypes().forEach(type -> builder.withSpecificFactory(type, PrimitiveInstanceFactory.INSTANCE));

        builder.withSuperFactory(Enum.class, EnumInstanceFactory.INSTANCE);
        builder.withSuperFactory(Map.class, new NonConcreteInstanceFactory(Map.class, HashMap.class, DefaultInstanceFactory.INSTANCE));
        builder.withSuperFactory(Set.class, new NonConcreteInstanceFactory(Set.class, HashSet.class, DefaultInstanceFactory.INSTANCE));
        builder.withSuperFactory(List.class, new NonConcreteInstanceFactory(List.class, ArrayList.class, DefaultInstanceFactory.INSTANCE));
        builder.withSuperFactory(Collection.class, new NonConcreteInstanceFactory(Collection.class, ArrayList.class, DefaultInstanceFactory.INSTANCE));    // Todo(ac): Questionable..

        DEFAULT = builder
            .withArrayDefaultFactory(DefaultInstanceFactory.INSTANCE)   // Todo(ac): we'll need specific array factory
            .withDefaultFactory(DefaultInstanceFactory.INSTANCE)
            .build();
    }
}
