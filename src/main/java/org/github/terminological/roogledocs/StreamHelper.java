package org.github.terminological.roogledocs;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class StreamHelper {

	public static <T> Stream<T> ofNullable(T t) {
	    return t == null ? Stream.empty() : Stream.of(t);
	}
	
	static <T> Stream<T> ofNullable(Collection<T> list) {
	    return list == null ? Stream.empty() : list.stream();
	}
	
	public static <T> Stream<T> optionalToStream(Optional<T> t) {
		return t.map(o -> Stream.of(o)).orElse(Stream.empty());
	}
	
}
