package eu.quanticol.moonlight.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import eu.quanticol.moonlight.formula.DoubleDistance;

class TestDoubleDistance {

	@Test
	void testCompareWithInfty() {
		DoubleDistance dd = new DoubleDistance();
		assertTrue(dd.less(1.0, Double.POSITIVE_INFINITY));
	}


}
