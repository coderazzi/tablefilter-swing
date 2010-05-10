/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
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

package net.coderazzi.filters.parser;

/**
 * <p>Interface to provide ways to parse a text as a given object.</p>
 *
 * <p>A parser must know how to build any type out of its string representation.</p>
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public interface ITypeBuilder {

    /**
     * <p>Returns an object out of a given text</p>
     *
     * <p>The parser implementation can require that the returned object belong to specific class
     * hierarchies</p>
     *
     * <p>If the implementation cannot build a meaningful object out of the passed string, it can
     * raise a (@link FilterTextParsingException} exception, or simply return null.</p>
     */
    Object parse(String text) throws FilterTextParsingException;
}
