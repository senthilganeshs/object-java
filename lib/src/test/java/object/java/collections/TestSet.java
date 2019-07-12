package object.java.collections;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestSet extends TestCase {

	public void testSetNonExistent() {
		Set.of("Chennai", "Bangalore").contains("Dubai", Assert::assertFalse);
	}
	
	public void testSetExistent() {
		Set.of("Apple", "Mango").contains("Apple", Assert::assertTrue);
	}
	
	public void testSetForEach() {
		Set.of("Apple").forEach(fruit -> Assert.assertEquals("Apple", fruit));
	}
	
	public void testEmptySet() {
		Set.empty().forEach(i -> Assert.assertTrue(false));		
	}
	
	public void testEmptySetContains() {
		Set.empty().contains(null, Assert::assertFalse);
	}
}
