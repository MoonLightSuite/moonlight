/**
 * 
 */
package eu.quanticol.moonlight.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.quanticol.moonlight.formula.TropicalSemiring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;

/**
 * @author loreti
 *
 */
public class TestSpatialProperties {
	
	@Test
	public void testGraphBuild() {
		int size = 10;
		SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x,y) -> (y==(((x+1)%size))?1.0:null ));
		
		assertNotNull(model);
	}

	@Test
	public void testGraphBuildEdges() {
		int size = 10;
		SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x,y) -> (y==(((x+1)%size))?1.0:null ));
		
		assertNotNull(model);
		for( int i=0 ; i<size ; i++ ) {
			for ( int j=0 ; j<size ; j++ ) {
				if (j==((i+1)%size)) {
					assertEquals(1.0,model.get(i, j),0.0);
				} else {
					assertNull(model.get(i, j));
				}
			}
		}
	}
	
	@Test
	public void testDistanceStructure() {
		int size = 3;
		SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x,y) -> (y==(((x+1)%size))?1.0:null ));
		DistanceStructure<Double, Double> ds = new DistanceStructure<Double,Double>((x, y) -> x+y, new TropicalSemiring() , x -> (x<10), model);
		
		for( int i=0 ; i<size ; i++ ) {
			for( int j=0 ; j<size; j++ ) {
				assertEquals("d("+i+","+j+"): ",(j>=i?j-i:size-i+j),ds.getDistance(i, j),0.0);
			}
		}
		
	}
	
	@Test
	public void testDistanceStructure2() {
		int size = 3;
		SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x,y) -> (((y==((x+1)%size))||(x==((y+1)%size)))?1.0:null ));
		DistanceStructure<Double, Double> ds = new DistanceStructure<Double,Double>((x, y) -> x+y, new TropicalSemiring() , x -> (x<10), model);
		
		for( int i=0 ; i<size ; i++ ) {
			for( int j=0 ; j<size; j++ ) {
				assertEquals("d("+i+","+j+"): ",Math.min(Math.abs(j-i),size-Math.abs(i-j)),ds.getDistance(i, j),0.0);
			}
		}
		
	}

}
