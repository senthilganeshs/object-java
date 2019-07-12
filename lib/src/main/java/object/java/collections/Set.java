package object.java.collections;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface Set<T> {

	void contains (final T value, final Consumer<Boolean> fn);

	void forEach (final Consumer<T> action);

	@SuppressWarnings("unchecked")
	public static <R> Set<R> empty() {
		return (Set<R>) Empty.INST;
	}
	
	public static <R> Set<R> of (final R...values) {
		return new Hashed<>(values);
	}
	
	final static class Hashed<T> implements Set<T> {

		private final List<T>[] table;
		
		Hashed (final T...values) {
			this.table = new List[values.length];
			for (T value : values) {
				int hash = hash(value);
				final List<T> list = table[hash];
				if (list == null) {
					table[hash] = List.of(value);
				} else {
					table[hash] = List.tailList(list, value);
				}
			}
		}
		
		private int hash(final T object) {
			int h = (h = object.hashCode()) ^ (h >>> 16);
			return (int) h & (table.length - 1);	
		}
		
		
		@Override
		public void contains(final T value, final Consumer<Boolean> fn) {
			
			final List<T> hashed = table[hash(value)];
			if (hashed != null) {
				AtomicBoolean result = new AtomicBoolean(false);
				hashed.forEach(t -> {
					if (t.equals(value)) {
						result.compareAndSet(false, true);
					}
				});
				fn.accept(result.get());				
			}
		}

		@Override
		public void forEach(final Consumer<T> action) {
			for (final List<T> list : table) {
				if (list != null) {
					list.forEach(action);
				}
			}
		}		
	}

	static enum Empty implements Set<Void> {
		INST
		;

		@Override
		public void contains(Void value, Consumer<Boolean> fn) {
			fn.accept(false);
		}

		@Override
		public void forEach(Consumer<Void> action) {
			//DO NOTHING
		}	   
	}
}
