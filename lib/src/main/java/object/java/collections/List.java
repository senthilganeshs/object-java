package object.java.collections;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public interface List<T> {

    void forEach (final Consumer<T> action);
    
    void forEach (final int start ,final int end, final Consumer<T> action) throws IndexOutOfBoundsException;
    
    void forIndex (final int index, final Consumer<T> action) throws IndexOutOfBoundsException;
    
    
    @SuppressWarnings("unchecked")
	static<T>  List<T> empty() {
        return (List<T>) EMPTY;
    }
    
    @SafeVarargs
	static<T> List<T> of (final T...values) {
    	if (values == null || values.length == 0)
    		return empty();
        return new NonEmpty<>(values);
    }
    
    static <T> List<T> repeated (final int n, final T value) {
        return new Repeated<>(n, value);
    }
    
    static<T, R> List<R> mapped (final Function<T, R> fn, final List<T> list) {
        return new Mapped<>(list, fn);
    }
    
    static <T, R> List<R> flatMapped (final Function<T, List<R>> fn, final List<T> list) {
        return new FlatMapped<>(list, fn);
    }
    
    @SafeVarargs
	static <T> List<T> linkedList (final T...values) {
        List<T> tail = empty();
        List<T> list = tail;
        for (T value : values) {
            list = new TailList <>(list, value);
        }
        return list;
    }
    
    static<T> List<T> tailList (final List<T> head, final T tail) {
    	return new TailList<>(head, tail);
    }
    
    
    static<T> List<T> par (final List<T> list, final ExecutorService es) {
    	return new Par<>(es, list);
    }
    
    static<T> List<T> push (final T value, final List<T> stack) {
    	return new Cons<>(stack, value);
    }
    
    static<T> List<T> pop (final List<T> stack) {
    	return new Popped<>(stack);
    }
    
    static<T> List<T> enqueue (final List<T> queue, final T value) {
    	return new TailList<>(queue, value);
    }
    
    static<T> List<T> dequeue (final List<T> queue) {
    	return new Dequeued<>(queue);
    }
    
    final static class Dequeued<T> implements List<T> {

    	private final List<T> queue;

		Dequeued(final List<T> queue) {
    		this.queue = queue;
    	}
    	
		@Override
		public void forEach(Consumer<T> action) {
			try {
				queue.forIndex(0, action);
			} catch (IndexOutOfBoundsException e) {
				//forEach should not throw exception;
			}
		}

		@Override
		public void forEach(int start, int end, Consumer<T> action) throws IndexOutOfBoundsException {
			if (start == 0 && end == 1) {
				queue.forIndex(0, action);
			} else {
				throw new IndexOutOfBoundsException("index out of range");
			}
		}

		@Override
		public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
			if (index == 0) {
				queue.forIndex(0, action);
			} else {
				throw new IndexOutOfBoundsException("index out of range");
			}
		}
    }
    
    final static class TailList<T> implements List<T> {

    	private final List<T> head;
		private final T tail;
		private final int headSize;

		TailList(final List<T> head, final T tail) {
    		this.tail  = tail;
    		this.head = head;
    		this.headSize = sizeOf (head);
    	}
    	
		private int sizeOf(List<T> head2) {
			final AtomicInteger counter = new AtomicInteger();
			head2.forEach(t -> counter.incrementAndGet());
			return counter.get();
		}

		@Override
		public void forEach(Consumer<T> action) {
			head.forEach(action);
			action.accept(tail);
		}

		@Override
		public void forEach(int start, int end, Consumer<T> action) throws IndexOutOfBoundsException {
			if (start == 0 && end == 1) {
				action.accept(tail); //special case singleton list.
			} else if (end > headSize + 1) {
				throw new IndexOutOfBoundsException("index out of range");
			} else if (end == headSize + 1) {
				head.forEach(start, end - 1, action);
				action.accept(tail);
			}else if (end == headSize) {
				head.forEach(start, end, action);				
			} else {
				head.forEach(start, end, action);
			}
		}

		@Override
		public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
			if (index == headSize) {
				action.accept(tail);
			} else {
				head.forIndex(index, action);
			}
		}
    }

    final static class Par<T> implements List<T> {

    	private final List<T> list;
		private final ExecutorService es;

		Par (final ExecutorService es, final List<T> list) {
    		this.es = es;
    		this.list = list;
    	}
    	
		@Override
		public void forEach(final Consumer<T> action) {
			final AtomicReference<List<Future<?>>> futures = new AtomicReference<>(List.empty());
			list.forEach(t -> {
				futures.set(List.tailList(futures.get(), es.submit(() -> action.accept(t))));
			});
			
			waitForResult(futures);
		}

		private void waitForResult(final AtomicReference<List<Future<?>>> futures) {
			futures.get().forEach(f -> {
				try {
					f.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			});
		}

		@Override
		public void forEach(int start, int end, Consumer<T> action) throws IndexOutOfBoundsException {
			final AtomicReference<List<Future<?>>> futures = new AtomicReference<>(List.empty());
			list.forEach(start, end, t -> {
				futures.set(List.tailList(futures.get(), es.submit(() -> action.accept(t))));
			});
			
			waitForResult (futures);
		}

		@Override
		public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
			
			final AtomicReference<Future<?>> future = new AtomicReference<>();
			
			list.forIndex(index, t -> future.set(es.submit(() -> action.accept(t))));
			
			es.submit(() -> future.get());
		}
    	
    }
    
    static List<Void> EMPTY = new Empty();
    
    final static class Cons<T> implements List<T> {
        private final T one;
        private final List<T> list;
        private final int endIndex;

        Cons(final List<T> list, final T one) {
            this.list = list;
            this.one = one;        
            endIndex = sizeOf (list) - 1;
        }

        private int sizeOf(List<T> list2) {
			final AtomicInteger counter = new AtomicInteger(0);
			list2.forEach(t -> counter.incrementAndGet());
			return counter.get();
		}

		@Override
        public void forEach(Consumer<T> action) {
            action.accept(one);
            list.forEach(action);            
        }

        @Override
        public void forEach(int start, int end, Consumer<T> action)
            throws IndexOutOfBoundsException {
        	if (endIndex == -1 && end == 1 && start == 0) { //empty tail 
        		action.accept(one);
        	} else if (end > endIndex + 2) { //end index greater than tial + j
        		throw new IndexOutOfBoundsException("index out of range");
        	} else if (end <= endIndex + 2) { //end index equal to last value of tail list.
        		if (start == 0) {
                 	action.accept(one);
                 	list.forEach(start, end - 1, action);
                 } else {
                	 list.forEach(start - 1, end - 1, action);
                 }
            }
        }

        @Override
        public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
            if (index == 0)
                action.accept(one);
            else {
                list.forIndex (index - 1, action);
            }
        }
    }
    
    final static class Mapped<T, R> implements List<R> {

        private final Function<T, R> fn;
        private final List<T> list;

        Mapped (final List<T> list, final Function<T, R> fn) {
            this.list = list;
            this.fn = fn;
        }
        
        @Override
        public void forEach(Consumer<R> action) {
            list.forEach(t -> action.accept(fn.apply(t)));
        }

        @Override
        public void forEach(int start, int end, Consumer<R> action)
            throws IndexOutOfBoundsException {
            list.forEach(start, end, t -> action.accept(fn.apply(t)));
        }

        @Override
        public void forIndex(int index, Consumer<R> action) throws IndexOutOfBoundsException {
            list.forIndex (index, t -> action.accept(fn.apply(t)));
        }           
    }
    
    final static class Repeated<T> implements List<T> {

        private final List<T> list;

        @SuppressWarnings("unchecked")
		Repeated (final int n, final T value) {
            java.lang.Object values[] = new java.lang.Object[n];
            for (int i = 0; i < values.length; i ++) {
                values[i] = value;
            }
            this.list = (List<T>) List.of(values);
        }
        
        @Override
        public void forEach(Consumer<T> action) {
            list.forEach(action);
        }

        @Override
        public void forEach(int start, int end, Consumer<T> action)
            throws IndexOutOfBoundsException {
            list.forEach(start, end, action);
        }

        @Override
        public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
            list.forIndex(index, action);
        }
        
    }
    
    final static class FlatMapped<T, R> implements List<R> {
        private final List<R> list;
       
        FlatMapped (final List<T> list, final Function<T, List<R>> fn) {
        	final AtomicReference<List<R>> l = new AtomicReference<>(List.empty());
            list.forEach(t -> fn.apply(t).forEach(t1 -> {
                l.set(List.tailList(l.get(), t1));
            }));
            
            this.list = l.get();
        }

        @Override
        public void forEach(final Consumer<R> action) {
            list.forEach(action);
        }

        @Override
        public void forEach(int start, int end, Consumer<R> action)
            throws IndexOutOfBoundsException {
            list.forEach(start, end, action);
        }

        @Override
        public void forIndex(int index, Consumer<R> action) throws IndexOutOfBoundsException {
            list.forIndex(index, action);
        }            
    }
    
    final static class Popped<T> implements List<T> {

    	private final List<T> list;
		private final int lastIndex;

		Popped (final List<T> stack) {
    		this.list = stack;
    		this.lastIndex = sizeOf(list) - 1;
    	}
    	
		private int sizeOf(List<T> list2) {
			final AtomicInteger counter = new AtomicInteger();
			list2.forEach(t -> counter.incrementAndGet());
			return counter.get();
		}

		@Override
		public void forEach(Consumer<T> action) {
			list.forIndex(lastIndex, action);
		}

		@Override
		public void forEach(int start, int end, Consumer<T> action) throws IndexOutOfBoundsException {
			if (start == 0 && end == 1) {
				list.forIndex(lastIndex, action);
			} else {
				throw new IndexOutOfBoundsException("index out of range");
			}
		}

		@Override
		public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
			if (index == 0) {
				list.forIndex(lastIndex, action);
			} else {
				throw new IndexOutOfBoundsException("index out of range");
			}
		}
    }
    
    final static class NonEmpty<T> implements List<T> {
        
        private final T[] values;

        @SafeVarargs
		NonEmpty(final T...values) {
            this.values = values;
            assert (this.values != null && values.length != 0);
        }

        @Override
        public void forEach(final Consumer<T> action) {
            for (final T value  :values) {
                action.accept(value);
            }
        }

        @Override
        public void forEach(int start, int end, Consumer<T> action)
            throws IndexOutOfBoundsException {
            
            if ((start < 0 || start >= values.length) ||
                (end <= 0   || end > values.length) ||
                (start >= end)) {
                throw new IndexOutOfBoundsException("index out of range.");
            }
            
            for (int i = start; i < end; i ++) {
                action.accept(values[i]);
            }
        }

        @Override
        public void forIndex(int index, Consumer<T> action) throws IndexOutOfBoundsException {
            action.accept(values[index]);
        }            
    }
    
    
    final static class Empty implements  List<Void> {

        Empty() {}
        
        @Override
        public void forEach(Consumer<Void> action) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void forEach(int start, int end, Consumer<Void> action)
            throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("index out of range");
        }

        @Override
        public void forIndex(int index, Consumer<Void> action)
            throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("index out of range");
        }
    }   
}