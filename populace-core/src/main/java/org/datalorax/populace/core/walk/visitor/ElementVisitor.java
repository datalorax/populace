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

package org.datalorax.populace.core.walk.visitor;

import org.datalorax.populace.core.walk.element.ElementInfo;

/**
 * Visitor pattern interface for elements
 *
 * @author Andrew Coates - 28/02/2015.
 */
public interface ElementVisitor {
    /**
     * Called on visiting a element of a collection
     *
     * @param element the field being visited     *
     */
    void visit(final ElementInfo element);
}
