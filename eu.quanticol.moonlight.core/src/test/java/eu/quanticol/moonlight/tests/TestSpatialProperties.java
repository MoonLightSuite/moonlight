/**
 * 
 */
package eu.quanticol.moonlight.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.TropicalSemiring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

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
		DistanceStructure<Double, Double> ds = new DistanceStructure<Double,Double>(x -> x, new DoubleDistance() , x -> (x<10), model);
		
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
		DistanceStructure<Double, Double> ds = new DistanceStructure<Double,Double>(x -> x , new DoubleDistance() , x -> (x<10), model);
		
		for( int i=0 ; i<size ; i++ ) {
			for( int j=0 ; j<size; j++ ) {
				assertEquals("d("+i+","+j+"): ",Math.min(Math.abs(j-i),size-Math.abs(i-j)),ds.getDistance(i, j),0.0);
			}
		}
		
	}
	
	@Test
	public void testGridGenerationNodeIndexes() {
		int rows = 100;
		int columns = 1000;
		for( int i=0 ; i<rows; i++ ) {
			for( int j=0 ; j<columns ; j++ ) {
				int idx = TestUtils.gridIndexOf(i, j, columns);				
				assertEquals( i*columns+j, idx );
				Pair<Integer,Integer> loc = TestUtils.gridLocationOf(idx, rows, columns);
				assertEquals( i , loc.getFirst().intValue() );
				assertEquals( j , loc.getSecond().intValue() );
			}
		}
	}
	
	@Test 
	public void testDistanceOnGrid() {
		int rows = 40;
		int columns = 40;
		SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
		DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), x -> x<20, model);
		for( int i1=0 ; i1<rows; i1++ ) {
			for( int j1=0 ; j1<columns ; j1++ ) {
				for( int i2=0 ; i2<rows; i2++ ) {
					for( int j2=0 ; j2<columns ; j2++ ) {
						assertEquals(
								"d(<"+i1+","+j1+">,<"+i2+","+j2+">): ",
								Math.abs(i1-i2)+Math.abs(j1-j2),
								ds.getDistance(TestUtils.gridIndexOf(i1, j1, columns), TestUtils.gridIndexOf(i2, j2, columns)),0.0);
					}
				}

			}
		}	
	}
	
	@Test 
	public void testSomewhereOnGrid() {
		int rows = 9;
		int columns = 12;
		double range = 10.0;
		int relevantC = 5;
		int relevantR = 5;
		SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
		DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), x -> x<range, model);
		ArrayList<Boolean> result = ds.somewhere(
				new BooleanDomain(), 
				(i) -> i==TestUtils.gridIndexOf(relevantR, relevantC, columns)
		);
		for( int i=0 ; i<rows ; i++ ) {
			for( int j=0 ; j<columns ; j++ ) {
				assertEquals( "<"+i+","+j+">:", Math.abs(i-5)+Math.abs(j-5)<range, result.get(TestUtils.gridIndexOf(i, j, columns)));
			}
		}
	}	

	@Test 
	public void testEverywhereOnGrid() {
		int rows = 9;
		int columns = 12;
		double range = 10.0;
		int relevantC = 5;
		int relevantR = 5;
		SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
		DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), x -> x<range, model);
		ArrayList<Boolean> result = ds.everywhere(
				new BooleanDomain(), 
				(i) -> i!=TestUtils.gridIndexOf(relevantR, relevantC, columns)
		);
		for( int i=0 ; i<rows ; i++ ) {
			for( int j=0 ; j<columns ; j++ ) {
				assertEquals( "<"+i+","+j+">:", Math.abs(i-5)+Math.abs(j-5)>=range, result.get(TestUtils.gridIndexOf(i, j, columns)));
			}
		}
	}	
	
	@Test 
	public void testEscapeOnGrid() {
		int rows = 5;
		int columns = 5;
		double range = 2.0;
		int wallC = 2;
		int wallR = 2;
		SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
		DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), x -> x>range, model);
		ArrayList<Boolean> result = ds.escape(
				new BooleanDomain(), 
				(i) -> {
					Pair<Integer,Integer> p = TestUtils.gridLocationOf(i, rows, columns);
					return !(((p.getFirst().equals(wallC))&&(p.getSecond()<=wallR))
							||((p.getFirst()<=wallC)&&(p.getSecond().equals(wallR))));
				}
		);
		for( int i=0 ; i<rows ; i++ ) {
			for( int j=0 ; j<columns ; j++ ) {
				assertEquals( "<"+i+","+j+">:", (i>wallR)||(j>wallC), result.get(TestUtils.gridIndexOf(i, j, columns)));
			}
		}
	}		
	
}
