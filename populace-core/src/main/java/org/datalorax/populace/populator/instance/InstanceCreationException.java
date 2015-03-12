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

package org.datalorax.populace.populator.instance;

import java.lang.reflect.Type;

/**
 * @author Andrew Coates - 09/03/2015.
 */
public class InstanceCreationException extends RuntimeException {
    public InstanceCreationException(final String message, final Type type, final Throwable cause) {
        super(message + ". Type: " + type, cause);
    }
}