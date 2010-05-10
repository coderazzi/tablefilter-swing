/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.parser.generic;

import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.ITypeBuilder;


/**
 * <p>Factory to create types as they are parsed from a text expression</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface ITypesFactory {

    /**
     * <p>Builds an object out of the passed string, with the given type.</p>
     *
     * <p>The object's type must match the passed class</p>
     *
     * <p>If the factory cannot build the given type, it can return the passed string; in this case,
     * the method {@link ITypesFactory#getBuildType(Class)} must provide this information</p>
     *
     * <p>If the string cannot be parsed into the expected type, the implementation can raise an
     * exception, or simply return null</p>
     */
    public abstract Object buildObject(Class<?> c, String s) throws FilterTextParsingException;

    /**
     * Returns the type that will be returned when the an object of class C is required
     */
    public abstract Class<?> getBuildType(Class<?> c);

    /**
     * Provides a specific type factory for a given class
     */
    public abstract void setFactory(Class<?> c, ITypeBuilder factory);

}
