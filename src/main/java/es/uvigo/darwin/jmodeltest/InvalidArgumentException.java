/*
Copyright (C) 2011  Diego Darriba, David Posada

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package es.uvigo.darwin.jmodeltest;

import java.io.File;

public class InvalidArgumentException extends RuntimeException {

	private static final long serialVersionUID = 201104031313L;

	public InvalidArgumentException(String message) {
		super(message);
	}

	public static class UnexistentCriterionException extends
			InvalidArgumentException {

		private static final long serialVersionUID = 201104031313L;

		public UnexistentCriterionException(int criterion) {
			super("Attempting to create an unexistent criterion type: "
					+ criterion);
		}

	}

	public static class InvalidInputFileException extends
			InvalidArgumentException {

		private static final long serialVersionUID = 201104031313L;

		public InvalidInputFileException(String message) {
			super("Invalid input file: " + message);
		}

	}

	public static class InvalidAlignmentFileException extends
			InvalidInputFileException {

		private static final long serialVersionUID = 201104031313L;

		public InvalidAlignmentFileException(String fileName) {
			super("Alignment: " + fileName);
		}
		
		public InvalidAlignmentFileException(File file) {
			super("Alignment: " + file.getAbsolutePath());
		}

	}

	public static class UnexistentAlignmentFileException extends
			InvalidAlignmentFileException {

		private static final long serialVersionUID = 201104031313L;

		public UnexistentAlignmentFileException(File file) {
			super("File does not exist: " + file.getAbsolutePath());
		}

	}

	public static class InvalidTreeFileException extends
			InvalidInputFileException {

		private static final long serialVersionUID = 201104031313L;

		public InvalidTreeFileException(String message) {
			super("Input tree: " + message);
		}
		
		public InvalidTreeFileException(File file) {
			super("Input tree: " + file.getAbsolutePath());
		}

	}

	public static class UnexistentTreeFileException extends
			InvalidTreeFileException {

		private static final long serialVersionUID = 201104031313L;

		public UnexistentTreeFileException(File file) {
			super("File does not exist: " + file.getAbsolutePath());
		}
		
		public UnexistentTreeFileException(String file) {
			super("File does not exist: " + file);
		}

	}
}
