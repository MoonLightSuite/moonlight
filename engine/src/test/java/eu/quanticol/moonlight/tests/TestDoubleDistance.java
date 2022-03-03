package eu.quanticol.moonlight.tests;

import static org.junit.jupiter.api.Assertions.*;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import org.junit.jupiter.api.Test;

class TestDoubleDistance {

	@Test
	void testCompareWithInfty() {
		DistanceDomain<Double> dd = new DoubleDomain();
		assertTrue(dd.less(1.0, Double.POSITIVE_INFINITY));
	}


}
