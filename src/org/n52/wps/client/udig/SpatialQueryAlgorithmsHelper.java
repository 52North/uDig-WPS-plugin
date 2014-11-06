/**
 * 
 */
package org.n52.wps.client.udig;

import java.util.HashMap;

/**
 * @author Benjamin Proﬂ
 *
 */
public class SpatialQueryAlgorithmsHelper {

	
	public enum SpatialQueryAlgorithms {

		TouchesAlgorithm, OverlapsAlgorithm, CrossesAlgorithm, DisjointAlgorithm, ContainsAlgorithm, DistanceAlgorithm, WithinAlgorithm, IntersectsAlgorithm, EqualsAlgorithm,

	}
	
	public final static String DISTANCE_PLACEHOLDER = "ßdistß";
	
	private static SpatialQueryAlgorithmsHelper instance;
	private HashMap<String, String[]> algorithmsToMessagesMap;	
	private final String touchesTRUE = "The geometries touch each other.";
	private final String touchesFALSE = "The geometries do not touch each other.";
	private final String overlapsTRUE = "The geometries overlap each other.";
	private final String overlapsFALSE = "The geometries do not overlap each other.";
	private final String crossesTRUE = "The geometries cross each other.";
	private final String crossesFALSE = "The geometries do not cross each other.";
	private final String disjointTRUE = "The geometries are disjoint.";
	private final String disjointFALSE= "The geometries are not disjoint.";
	private final String containsTRUE = "The first geometry overlaps the second.";
	private final String containsFALSE = "The first geometry does not overlap the second.";
	private final String distance = "The distance between the geometries is " + DISTANCE_PLACEHOLDER + " map units.";
	private final String withinTRUE = "The first geometry is within the second.";
	private final String withinFALSE = "The first geometry is not within the second.";
	private final String intersectsTRUE = "The first geometry intersects the second.";
	private final String intersectsFALSE = "The first geometry does not intersect the second.";
	private final String equalsTRUE = "The first geometry equals the second.";
	private final String equalsFALSE = "The first geometry does not equal the second.";
	
	
	private SpatialQueryAlgorithmsHelper(){
		
		algorithmsToMessagesMap = new HashMap<String, String[]>();
		
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.TouchesAlgorithm.toString(), new String[]{touchesTRUE, touchesFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.OverlapsAlgorithm.toString(), new String[]{overlapsTRUE, overlapsFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.CrossesAlgorithm.toString(), new String[]{crossesTRUE, crossesFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.DisjointAlgorithm.toString(), new String[]{disjointTRUE, disjointFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.ContainsAlgorithm.toString(), new String[]{containsTRUE, containsFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.DistanceAlgorithm.toString(), new String[]{distance});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.WithinAlgorithm.toString(), new String[]{withinTRUE, withinFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.IntersectsAlgorithm.toString(), new String[]{intersectsTRUE, intersectsFALSE});
		algorithmsToMessagesMap.put(SpatialQueryAlgorithms.EqualsAlgorithm.toString(), new String[]{equalsTRUE, equalsFALSE});
		
	}	
	
	public static SpatialQueryAlgorithmsHelper getInstance(){
		
		if(instance == null){
			instance = new SpatialQueryAlgorithmsHelper();
		}
		return instance;
	}

	public HashMap<String, String[]> getAlgorithmsToMessagesMap() {
		return algorithmsToMessagesMap;
	}
	
}