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
 * Generic exception raised when a text does not provide a meaningful parsing expression
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public class FilterTextParsingException extends Exception {

	private static final long serialVersionUID = -1854149011685993092L;
	
	private int position;

    /**
     * Full constructor
     *
     * @param  reason    The problem behind the exception
     * @param  position  The position, on the originally parsed expression, that raised this error
     */
    public FilterTextParsingException(String reason, int position) {
        super(reason);
        this.position = position;
    }

    /**
     * Constructor, where the position, on the originally parsed expression, that raised this error
     * is left unspecified
     */
    public FilterTextParsingException(String reason) {
        this(reason, 0);
    }


    /**
     * Returns the position, in the originally parsed expression, that raised this exception.
     */
    public int getPosition() {
        return position;
    }
}
