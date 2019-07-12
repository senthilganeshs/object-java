package object.java.collections;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ListTest extends TestCase {

	public void testEmptyList() throws Exception {
		List.of().forEach(System.out::println);
	}
	
	public void testTailListIndexOutOfRange() throws Exception {
		assertIndexOutOfBoundsException(() -> List.tailList(List.empty(), 1).forEach(0, 2, i -> {}));
	}
	
	public void testTailListLast() throws Exception {
		List.tailList(List.tailList(List.tailList(List.empty(), 3), 2), 1).forIndex(2,  i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testTailListFirst() throws Exception {
		List.tailList(List.tailList(List.tailList(List.empty(), 3), 2), 1).forIndex(0, i -> Assert.assertEquals(3, (int) i));
	}
	
	public void testTailListSingletonForEach() throws Exception {
		List.tailList(List.empty(), 1).forEach(i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testTailListSingletonForEachIndexed () throws Exception {
		List.tailList(List.empty(), 1).forEach(0, 1, i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testTailListForEach() throws Exception {
		final AtomicInteger v = new AtomicInteger(1);
		List.tailList(List.of(1,2), 3).forEach(0, 3, i -> Assert.assertEquals(v.getAndIncrement(), (int) i));
	}	
	
	public void testTailListForEachStartToEnd() throws Exception {
		final AtomicInteger v = new AtomicInteger(2);
		List.tailList(List.of(1, 2), 3).forEach(1, 3, i -> Assert.assertEquals(v.getAndIncrement(), (int) i));
	}
	
	public void testTailListForEachStartToEndHeadOnly() throws Exception {
		final AtomicInteger v = new AtomicInteger(1);
		List.tailList(List.linkedList(1,2), 3).forEach(0,  2, i -> Assert.assertEquals(v.getAndIncrement(), (int) i));
	}
	
	public void testTailListForEachStartToEndHeadLessThan() throws Exception {
		final AtomicInteger v = new AtomicInteger(1);
		List.tailList(List.of(1,2, 3), 4).forEach(0,  2, i -> Assert.assertEquals(v.getAndIncrement(), (int) i));
	}
	
	public void testStackPush() throws Exception {
		List.pop(List.push(1, List.push(2, List.push(3, List.empty())))).forEach(i -> Assert.assertEquals(3, (int)i ));
	}
	
	public void testStackPushPopForIndex() throws Exception {
		List.pop(List.push(1, List.push(2, List.push(3, List.empty())))).forIndex(0, i -> Assert.assertEquals(3, (int)i ));
	}
	
	public void testStackPushForIndexOutOfBounds() throws Exception {
		assertIndexOutOfBoundsException(() -> List.pop(List.push(1, List.push(2, List.push(3, List.empty())))).forIndex(1, System.out::println));
	}
	
	public void testStackPushForEachOutOfBounds() throws Exception {
		assertIndexOutOfBoundsException(() -> List.pop(List.push(1, List.push(2, List.push(3, List.empty())))).forEach(0, 2, System.out::println));
	}
	
	public void testStackPushForEach() throws Exception {
		List.push(1, List.linkedList(2, 3)).forEach(0, 2, System.out::println);
	}
	
	public void testStackPushForIndex() throws Exception {
		List.push(1, List.linkedList(2, 3)).forIndex(1, System.out::println);
	}
	
	public void testStackPushAndPopForEach() throws Exception {
		List.pop(List.push(1, List.push(2, List.push(3, List.empty())))).forEach(0, 1, i -> Assert.assertEquals(3, (int)i ));
	}
	
	public void testRepeatedList() throws Exception {
		List.repeated(10, 1).forEach(i -> Assert.assertEquals(1, (int)i));
	}
	
	public void testRepeatedForEachList() throws Exception {
		List.repeated(10, 1).forEach(0, 10, i -> Assert.assertEquals(1, (int)i));
	}
	
	public void testRepeatedForIndexList() throws Exception {
		List.repeated(10, 1).forIndex(9, i -> Assert.assertEquals(1, (int)i));
	}
	
	public void testQueueEnqueueForEach() throws Exception {
		List.dequeue(List.enqueue(List.enqueue(List.empty(), 1), 2)).forEach(i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testQueueEnqueueForEachStartToEnd() throws Exception {
		List.dequeue(List.enqueue(List.enqueue(List.empty(), 1), 2)).forEach(0, 1, i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testQueueEnqueueForIndex() throws Exception {
		List.dequeue(List.enqueue(List.enqueue(List.empty(), 1), 2)).forIndex(0, i -> Assert.assertEquals(1, (int) i));
	}
	
	public void testQueueEnqueueForEachStartToEndOutOfBounds() throws Exception {
		assertIndexOutOfBoundsException(() -> 
		List.dequeue(List.enqueue(List.enqueue(List.empty(), 1), 2)).forEach(0, 2, System.out::println));
	}
	
	public void testQueueEnqueueForIndexOutOfBounds() throws Exception {
		assertIndexOutOfBoundsException(
				() -> List.dequeue(List.enqueue(List.enqueue(List.empty(), 1), 2)).forIndex(2, System.out::println));
	}
	
	public void testMappedList() throws Exception {
		final AtomicInteger v = new AtomicInteger(1);
		
		List.mapped(i -> i * 2, List.linkedList(1, 2, 3)).forEach(i -> Assert.assertEquals(v.getAndIncrement() * 2, (int) i));
	}
	
	public void testMappedListForEach() throws Exception {
		final AtomicInteger v = new AtomicInteger(1);
		List.mapped(i -> i * 2, List.linkedList(1, 2, 3)).forEach(0,  2, i -> Assert.assertEquals(v.getAndIncrement() * 2, (int) i));
	}
	
	public void testMappedListForIndex() throws Exception {
		List.mapped(i -> i * 2, List.linkedList(1,2,3)).forIndex (0, i -> Assert.assertEquals(2, (int)i));
	}
	
	public void testFlatMapped() throws Exception {
		final int values[] = new int[] {1,2,2,3,3,3};
		final AtomicInteger index = new AtomicInteger();
		List.flatMapped(i -> List.repeated(i, i), List.linkedList(1, 2, 3)).forEach(i -> Assert.assertEquals(values[index.getAndIncrement()], (int)i));
	}
	
	public void testFlatMappedForEach() throws Exception {
		final int values[] = new int[] {1,2,2,3,3,3};
		final AtomicInteger index = new AtomicInteger();
		List.flatMapped(i -> List.repeated(i, i), List.linkedList(1, 2, 3)).forEach(0, 4, i -> Assert.assertEquals(values[index.getAndIncrement()], (int)i));
	}
	
	public void testFlatMappedForIndex() throws Exception {
		List.flatMapped(i -> List.repeated(i, i), List.linkedList(1, 2)).forIndex(2, i -> Assert.assertEquals(2, (int)2));
	}
	
	public void testParList() throws Exception {
		List.par(List.linkedList(1,2,3), Executors.newCachedThreadPool()).forEach(i -> System.out.printf("%d - %d\n", Thread.currentThread().getId(), i));
	}
	
	public void testParListForIndex() throws Exception {
		List.par(List.linkedList(1,2,3), Executors.newCachedThreadPool()).forIndex(1, i -> Assert.assertEquals(1, (int) 1));
	}
	
	public void testParListForEach() throws Exception {
		List.par(List.linkedList(1,2,3), Executors.newCachedThreadPool()).forEach(0, 2, i -> System.out.printf("%d - %d\n", Thread.currentThread().getId(), i));
	}
	
	@FunctionalInterface
	interface Thunk {
		void code();
	}
	
	private void assertIndexOutOfBoundsException(Thunk th) {
		try {
		th.code();
		} catch(IndexOutOfBoundsException e) {
			Assert.assertTrue(true);
			return;
		}
		Assert.assertTrue("Expected Index Out of Bounds exception", false);
	}
}