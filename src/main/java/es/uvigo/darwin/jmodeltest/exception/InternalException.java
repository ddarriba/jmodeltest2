/*
Copyright (C) 2009  Diego Darriba

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package es.uvigo.darwin.jmodeltest.exception;

/**
 * The Class ProtTestInternalException.
 * 
 * @author Diego Darriba
 */
public class InternalException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 20090728L;

	/**
	 * Instantiates a new prot test internal exception.
	 */
	public InternalException() {}
	
	/**
	 * Instantiates a new prot test internal exception.
	 * 
	 * @param description the description
	 */
	public InternalException(String description) {
		super(description);
	}

}
