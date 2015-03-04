package org.datalorax.populace.field.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A combination field filter that evaluates to true if any child filters evaluates true
 * @author datalorax - 28/02/2015.
 */
public class AnyFieldFilter implements FieldFilter {
    private final List<FieldFilter> filters;

    public AnyFieldFilter(final FieldFilter first, final FieldFilter... theRest) {
        final List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
        fieldFilters.add(first);
        fieldFilters.addAll(Arrays.asList(theRest));

        for (FieldFilter filter : fieldFilters) {
            Validate.notNull(filter, "at least one filter was null");
        }

        filters = Collections.unmodifiableList(fieldFilters);
    }

    @Override
    public boolean evaluate(final Field field) {
        for (FieldFilter filter : filters) {
            if (filter.evaluate(field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AnyFieldFilter that = (AnyFieldFilter) o;
        return this.filters.size() == that.filters.size() && this.filters.containsAll(that.filters);
    }

    @Override
    public int hashCode() {
        return filters.hashCode();
    }

    @Override
    public String toString() {
        return "ANY {" +  StringUtils.join(filters, ',') + "}";
    }
}