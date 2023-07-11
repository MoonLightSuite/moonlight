package io.github.moonlightsuite.moonlight.tests;

import static org.junit.jupiter.api.Assertions.*;

import io.github.moonlightsuite.moonlight.core.space.DistanceDomain;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import org.junit.jupiter.api.Test;

class TestDoubleDistance {

	@Test
	void testCompareWithInfty() {
		DistanceDomain<Double> dd = new DoubleDomain();
		assertTrue(dd.less(1.0, Double.POSITIVE_INFINITY));
	}


}
