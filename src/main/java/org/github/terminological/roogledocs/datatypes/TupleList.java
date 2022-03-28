package org.github.terminological.roogledocs.datatypes;

import java.util.ArrayList;
import java.util.List;

public class TupleList<X, Y>  extends ArrayList<Tuple<X,Y>> {
	
	private static final long serialVersionUID = 1L;
	
	@SafeVarargs
	public static <S,T> TupleList<S,T> create(Tuple<S,T>... args) {
		TupleList<S,T> out = new TupleList<S,T>();
		for (Tuple<S,T> arg: args) {
			out.add(arg);
		}
		return out;
	}
	
	public static <S,T> TupleList<S,T> create() {
		TupleList<S,T> out = new TupleList<S,T>();
		return out;
	}
	
	public static <S,T> TupleList<S,T> create(Class<S> type1, Class<T> type2) {
		TupleList<S,T> out = new TupleList<S,T>();
		return out;
	}
	
	public static <S,T> TupleList<S,T> with(S arg1,T arg2) {
		TupleList<S,T> out = new TupleList<S,T>();
		out.add(Tuple.create(arg1, arg2));
		return out;
	}
	
	public static <S,T> TupleList<S,T> create(List<S> args1, List<T> args2) {
		TupleList<S,T> out = new TupleList<S,T>();
		out.append(args1, args2);
		return out;
	}
	
	public static <S,T> TupleList<S,T> empty() {
		return new TupleList<S,T>();
	}
	
	public TupleList<X,Y> and(X element1, Y element2) {
		this.add(Tuple.create(element1, element2));
		return this;
	}
	
	public TupleList<X,Y> append(List<X> args1, List<Y> args2) {
		if (args1.size() != args2.size()) throw new ClassCastException("arguments must be the same length");
		for (int i = 0; i<args1.size(); i++) {
			this.and(args1.get(i), args2.get(i));
		}
		return this;
	}

	public boolean contains(X element1, Y element2) {
		return this.contains(Tuple.create(element1, element2));
	}

}
