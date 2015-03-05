package org.datalorax.populace.populator.mutator;

import org.datalorax.populace.populator.Mutator;
import org.datalorax.populace.populator.mutator.change.ChangeEnumMutator;
import org.datalorax.populace.populator.mutator.change.ChangeListElementsMutator;
import org.datalorax.populace.populator.mutator.change.ChangeMapValuesMutator;
import org.datalorax.populace.populator.mutator.change.ChangeSetElementsMutator;
import org.datalorax.populace.populator.mutator.commbination.ChainMutator;
import org.datalorax.populace.populator.mutator.ensure.EnsureCollectionNotEmptyMutator;
import org.datalorax.populace.populator.mutator.ensure.EnsureMapNotEmptyMutator;
import org.datalorax.populace.populator.mutator.ensure.EnsureMutator;
import org.datalorax.populace.type.TypeUtils;
import org.datalorax.populace.typed.ImmutableTypeMap;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for {@link org.datalorax.populace.populator.mutator.Mutators} collection
 *
 * @author datalorax - 01/03/2015.
 */
final class MutatorsBuilder implements  Mutators.Builder {
    private static final Mutators DEFAULT;

    private final ImmutableTypeMap.Builder<Mutator> mutatorsBuilder;

    public static Mutators.Builder defaults() {
        return Mutators.asBuilder(DEFAULT);
    }

    public static Mutator chain(final Mutator first, final Mutator second, final Mutator... additional) {
        return ChainMutator.chain(first, second, additional);
    }

    @Override
    public Mutators.Builder withSpecificMutators(final Map<Type, ? extends Mutator> mutators) {
        mutatorsBuilder.withSpecificTypes(mutators);
        return this;
    }

    @Override
    public Mutators.Builder withSpecificMutator(final Type type, final Mutator mutator) {
        mutatorsBuilder.withSpecificType(type, mutator);
        return this;
    }

    @Override
    public Mutators.Builder withSuperMutators(final Map<Class<?>, ? extends Mutator> mutators) {
        mutatorsBuilder.withSuperTypes(mutators);
        return this;
    }

    @Override
    public Mutators.Builder withSuperMutator(final Class<?> baseClass, final Mutator mutator) {
        mutatorsBuilder.withSuperType(baseClass, mutator);
        return this;
    }

    @Override
    public Mutators.Builder withArrayDefaultMutator(final Mutator mutator) {
        mutatorsBuilder.withArrayDefault(mutator);
        return this;
    }

    @Override
    public Mutators.Builder withDefaultMutator(final Mutator mutator) {
        mutatorsBuilder.withDefault(mutator);
        return this;
    }

    @Override
    public Mutators build() {
        return new Mutators(mutatorsBuilder.build());
    }

    MutatorsBuilder(final ImmutableTypeMap<Mutator> mutators) {
        this.mutatorsBuilder = ImmutableTypeMap.asBuilder(mutators);
    }

    private MutatorsBuilder() {
        this.mutatorsBuilder = ImmutableTypeMap.newBuilder();
    }

    static {
        final Mutators.Builder builder = new MutatorsBuilder();

        TypeUtils.getPrimitiveTypes().forEach(type -> builder.withSpecificMutator(type, PrimitiveMutator.INSTANCE));
        TypeUtils.getBoxedPrimitiveTypes().forEach(type -> builder.withSpecificMutator(type, PrimitiveMutator.INSTANCE));

        // Todo(ac): what about other java lang types..
        builder.withSpecificMutator(String.class, StringMutator.INSTANCE);
        builder.withSpecificMutator(Date.class, DateMutator.INSTANCE);

        // Todo(ac): what about base Collection.class? what instantiates that and populates...
        builder.withSuperMutator(Set.class, chain(EnsureMutator.INSTANCE, ChangeSetElementsMutator.INSTANCE));
        builder.withSuperMutator(List.class, chain(EnsureMutator.INSTANCE, EnsureCollectionNotEmptyMutator.INSTANCE, ChangeListElementsMutator.INSTANCE));
        builder.withSuperMutator(Map.class, chain(EnsureMutator.INSTANCE, EnsureMapNotEmptyMutator.INSTANCE, ChangeMapValuesMutator.INSTANCE));
        builder.withSuperMutator(Enum.class, chain(EnsureMutator.INSTANCE, ChangeEnumMutator.INSTANCE));

        DEFAULT = builder
            .withArrayDefaultMutator(ArrayMutator.INSTANCE)
            .withDefaultMutator(EnsureMutator.INSTANCE)
            .build();
    }
}
