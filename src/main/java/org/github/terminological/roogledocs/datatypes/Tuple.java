package org.github.terminological.roogledocs.datatypes;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An immutable tuple class
 * @author terminological
 *
 * @param <S1>
 * @param <S2>
 */
public class Tuple<S1, S2> implements Cloneable, Serializable, Entry<S1,S2> {
	
	@Override
	public String toString() {
		return "Tuple [first=" + first + ", second=" + second + "]";
	}
	
	private static final long serialVersionUID = -4442558088391506147L;
	
	public static <X extends Object, Y extends Object> Tuple<X,Y> create(X x,Y y) {
		return new Tuple<X,Y>(x,y);
	}
	
	@SuppressWarnings("unchecked")
	public Tuple<S1, S2> clone() {try {
		return (Tuple<S1, S2>) super.clone();
	} catch (CloneNotSupportedException e) {
		throw new Error("This should not occur since we implement Cloneable");
	}}
	
	private S1 first;
	private S2 second;
	
	public Tuple(S1 item1, S2 item2) {
		put(item1,item2);
	};
	
	private void put(S1 item1, S2 item2) {
		this.first = item1;
		this.second = item2;
	}

	public S1 getFirst() {return first;}
	public S2 getSecond() {return second;}
	
	public boolean secondEquals(Object value) {
		if ((value == null && this.getSecond() == null)) return true;
		else if ((value != null) && (this.getSecond() == null)) return false;
		if (this.getSecond().equals(value)) return true;
		else return false;
	}
	
	public boolean firstEquals(Object value) {
		if ((value == null) && (this.getFirst() == null)) return true;
		else if ((value != null) && (this.getFirst() == null)) return false;
		else if (this.getFirst().equals(value)) return true;
		else return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		try {
			Tuple<S1,S2> t = (Tuple<S1,S2>) o;
			return (firstEquals(t.getFirst()) && secondEquals(t.getSecond()));
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	public int hashCode() { 
	    int hash = 1;
	    hash = hash * 31 + (first == null ? 0 : first.hashCode());
	    hash = hash * 31 + (second == null ? 0 : second.hashCode());
	    return hash;
	  }

	public S1 getKey() {
		return first;
	}

	public S2 getValue() {
		return second;
	}

	/**
	 * required to conform to Map.Entry
	 */
	public S2 setValue(S2 arg0) {
		throw new UnsupportedOperationException("Tuple is an immutable class.");
	}
	
	public void consume(Consumer<Tuple<S1,S2>> function) {
		function.accept(this);
	}

	public <R> R map(Function<Tuple<S1,S2>,? extends R> mapper) {
		return mapper.apply(this);
	}

	public void setSecond(S2 second) {
		this.second = second;
	}
	
	public void setFirst(S1 first) {
		this.first = first;
	}
}
