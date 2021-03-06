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

package org.datalorax.populace.core.populate.mutator.change;

import org.apache.commons.lang3.Validate;
import org.datalorax.populace.core.populate.Mutator;
import org.datalorax.populace.core.populate.PopulatorContext;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * {@link org.datalorax.populace.core.populate.Mutator} for {@link java.math.BigDecimal}
 *
 * @author Andrew Coates - 10/03/2015.
 */
public class ChangeBigDecimalMutator implements Mutator {
    public static final ChangeBigDecimalMutator INSTANCE = new ChangeBigDecimalMutator();
    private static final BigDecimal DIVISOR = new BigDecimal("1.5");

    @Override
    public Object mutate(final Type type, final Object currentValue, final Object parent, final PopulatorContext config) {
        Validate.isTrue(type.equals(BigDecimal.class), "BigDecimal type expected");
        if (currentValue == null) {
            return null;
        }

        final BigDecimal bigDecimal = (BigDecimal) currentValue;
        return bigDecimal.divide(DIVISOR, BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that != null && getClass() == that.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
