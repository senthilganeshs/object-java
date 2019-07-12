package object.java.collections;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Map <K,V> {

    void forEach (final BiConsumer<K,V> action);
    
    void forEachKey (final Consumer<K> action);
    
    void forEachValue (final Consumer<V> action);
    
    void forKey (final K key, final Consumer<V> action);
    
    @SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> empty() {
    	return (Map<K, V>) Empty.INST;
    }
    
    public static <K,V> Map<K,V> of (final K[] keys, final V[] values) {
    	if (keys == null || values == null) {
    		return empty();
    	} else {
    		assert (keys.length == values.length);
    		return new Hashed<>(keys, values);
    	}
    }
    
    final static class Hashed<K,V> implements Map<K,V> {

    	private final V[] values;
		private final K[] keys;

		private final List<V>[] table;
		
		Hashed (final K[] keys, final V[] values) {
    		this.keys = keys;
    		this.values = values;
    		this.table = new List[values.length];
    		
    		for (int i = 0; i < keys.length; i ++) {
    			final int hash = hash(keys[i]);
    			final List<V> list = table[hash];
    			if (list == null) {
    				table[hash] = List.of(values[i]);
    			} else {
    				table[hash] = List.tailList(list, values[i]);
    			}
    		}
    	}
		
		private int hash(final K object) {
			int h = (h = object.hashCode()) ^ (h >>> 16);
			return (int) h & (table.length - 1);	
		}
    	
    	
		@Override
		public void forEach(final BiConsumer<K, V> action) {
			for (int i = 0; i < keys.length; i ++) {
				action.accept(keys[i], values[i]);
			}
		}

		@Override
		public void forKey(final K key, final Consumer<V> action) {
			if (table[hash(key)] != null) {
				table[hash(key)].forEach(action);
			}
		}

		@Override
		public void forEachKey(final Consumer<K> action) {
			for (int i = 0; i < keys.length; i ++) {
				action.accept(keys[i]);
			}
		}

		@Override
		public void forEachValue(final Consumer<V> action) {
			for(int i = 0; i < values.length; i ++) {
				action.accept(values[i]);
			}
		}    	
    }
    
    static enum Empty implements Map<Void, Void> {
		INST
    	;

		@Override
		public void forEach(final BiConsumer<Void, Void> action) {
			
		}

		@Override
		public void forKey(final Void key, final Consumer<Void> action) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void forEachKey(final Consumer<Void> action) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void forEachValue(final Consumer<Void> action) {
			// TODO Auto-generated method stub
			
		}
    	
    }
}
