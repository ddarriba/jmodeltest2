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
package es.uvigo.darwin.jmodeltest.tree;

/**
 * This class is a distances cache implementing the euclidean distance
 * 
 * @author Diego Darriba
 * 
 * @since 3.0
 */
public class TreeEuclideanDistancesCache extends TreeDistancesCache {

	private static TreeEuclideanDistancesCache instance;
	
	private TreeEuclideanDistancesCache() {
		super(EUCLIDEAN);
	}
	
	public static TreeEuclideanDistancesCache getInstance() {
		
		if (instance == null)
			instance = new TreeEuclideanDistancesCache();
		return instance;
		
	}


}
