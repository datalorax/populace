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

package org.datalorax.populace.core.populate.instance;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Modifier;

/**
 * Instance factory for handling non-concrete types e.g. interface and abstract types. The factory delegates any
 * concrete types its called with to the {@code concreteFactory} provided to the constructor. For non-concrete
 * types the factory creates an instance of the {@code defaultType} passed to the constructor, using the
 * {@code concreteFactory} passed in.
 *
 * @author Andrew Coates - 02/03/2015.
 */
public class DefaultTypeInstanceFactory implements InstanceFactory {
    private final Class<?> baseType;
    private final Class<?> defaultType;
    private final InstanceFactory concreteFactory;

    /**
     * @param baseType        the base type that this instance factory supports, i.e. the lowest common denominator.
     * @param defaultType     the type to instantiate when a call to
     *                        {@link InstanceFactory#createInstance(Class, Object, InstanceFactories)} is for a
     *                        non-concrete type. The type must be a concrete sub-type of {@code baseType}
     * @param <T>             The base type this factory will be used to instantiate.
     * @param concreteFactory the instance factory to delegate to for concrete types and to create instances of
     */
    public <T> DefaultTypeInstanceFactory(final Class<T> baseType, final Class<? extends T> defaultType,
                                          final InstanceFactory concreteFactory) {
        Validate.notNull(baseType, "baseType null");
        Validate.notNull(defaultType, "defaultType null");
        Validate.notNull(concreteFactory, "concreteFactory null");
        this.baseType = baseType;
        this.defaultType = defaultType;
        this.concreteFactory = concreteFactory;
    }

    @Override
    public <T> T createInstance(final Class<? extends T> type, final Object parent, final InstanceFactories instanceFactories) {
        if (notSupported(type)) {
            return null;
        }

        if (isConcrete(type)) {
            return concreteFactory.createInstance(type, parent, instanceFactories);
        }

        if (notCompatibleWithDefaultType(type)) {
            return null;
        }

        return createDefaultType(parent, instanceFactories);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DefaultTypeInstanceFactory that = (DefaultTypeInstanceFactory) o;
        return baseType.equals(that.baseType) &&
            concreteFactory.equals(that.concreteFactory) &&
            defaultType.equals(that.defaultType);
    }

    @Override
    public int hashCode() {
        int result = baseType.hashCode();
        result = 31 * result + defaultType.hashCode();
        result = 31 * result + concreteFactory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultTypeInstanceFactory{" +
            "baseType=" + baseType +
            ", defaultType=" + defaultType +
            ", concreteFactory=" + concreteFactory +
            '}';
    }

    private boolean notSupported(final Class<?> rawType) {
        return !baseType.isAssignableFrom(rawType);
    }

    private boolean notCompatibleWithDefaultType(final Class<?> rawType) {
        return !rawType.isAssignableFrom(defaultType);
    }

    @SuppressWarnings("unchecked")
    private <T> T createDefaultType(final Object parent, final InstanceFactories instanceFactories) {
        return (T) concreteFactory.createInstance(defaultType, parent, instanceFactories);
    }

    private static boolean isConcrete(final Class<?> rawType) {
        return !rawType.isInterface() && !Modifier.isAbstract(rawType.getModifiers());
    }
}
