package org.github.terminological.roogledocs;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.units.qual.C;

public class StreamHelper<X> {

	Stream<X> x;
	public StreamHelper(Stream<X> x) {
		this.x = x;
	}

	public static <T> Stream<T> ofNullable(T t) {
	    return t == null ? Stream.empty() : Stream.of(t);
	}
	
	static <T> Stream<T> ofNullable(Collection<T> list) {
	    return list == null ? Stream.empty() : list.stream();
	}
	
	public static <T> List<T> ls(T t) {
	    return t == null ? Collections.emptyList() : Collections.singletonList(t);
	}
	
	static <T> List<T> ls(Collection<T> list) {
	    return list == null ? Collections.emptyList() : new ArrayList<>(list);
	}
	
	static <T> List<T> ls(Optional<T> opt) {
	    return opt == null ? Collections.emptyList() : 
	    	opt.map(o -> Collections.singletonList(o)).orElse(Collections.emptyList());
	}
	
	public static <T> Stream<T> optionalToStream(Optional<T> t) {
		return t.map(o -> Stream.of(o)).orElse(Stream.empty());
	}
	
	public static <Y> StreamHelper<Y> str(Y y) {
		return str(Stream.of(y));
	}
	
	public static <Y> StreamHelper<Y> str(Collection<Y> y) {
		return new StreamHelper<Y>(y.stream());
	}
	
	public static <Y> StreamHelper<Y> str(Stream<Y> y) {
		return new StreamHelper<Y>(y);
	}
	
	public <W,Y extends Collection<W>> StreamHelper<W> flatMap(Function<X,Y> fn) {
		return new StreamHelper<W>(x.flatMap(t -> ofNullable(fn.apply(t))));
	}
	
	public <Y> StreamHelper<Y> map(Function<X,Y> fn) {
		return new StreamHelper<Y>(x.flatMap(t -> ofNullable(fn.apply(t))));
	}
	
	public Stream<X> get() {return x;}
	
	@SuppressWarnings("unchecked")
	public static <Y> List<Y> recurse(AbstractMap<String,Object> map, Class<Y> value, String... keys) {
		String level = keys[0];
		int length = keys.length;
		
		if (keys.length > 1) {
			String[] nextLevel = Arrays.copyOfRange(keys, 1, length);
			if (level == "*") {
				List<Y> tmp = new ArrayList<>();
				for (Object v: map.values()) {
					if (v instanceof AbstractMap) {
						tmp.addAll(recurse((AbstractMap<String,Object>) v, value, nextLevel));
					}
				}
				return tmp;
			} else {
				if (map.containsKey(level)) {
					Object v = map.get(level);
					if (v == null) return Collections.emptyList();
					if (v instanceof AbstractMap) {
						return recurse((AbstractMap<String,Object>) v, value, nextLevel);
					} else {
						return Collections.emptyList();
					}
				} else {
					return Collections.emptyList();
				}
			}
		} else {
			if (level=="*") {
				return map.values().stream().filter(v -> v != null).map(v -> value.cast(v)).collect(Collectors.toList());
			} else if (map.containsKey(level)) {
				Object v = map.get(level);
				if (v == null) return Collections.emptyList();
				return Collections.singletonList(value.cast(v));
			} else {
				return Collections.emptyList();
			}
		}
	}
	
	
	public static <Y> Optional<Y> recurseOne(AbstractMap<String,Object> map, Class<Y> value, String... keys) {
		List<Y> tmp = recurse(map,value,keys);
		if (tmp.size() != 1) return Optional.empty();
		return Optional.of(tmp.get(0));
	}
}
