package fileprocessor.parser;

import java.util.List;
import java.util.function.Function;

public interface Parseable<T> {

	default public Function<T, T> combineParsers(List<Function<T, T>> l) {
		Function<T, T> f = x -> x;
		
		for (Function<T, T> function : l) {
			if (function != null)
				f = f.andThen(function);
		}
		
		return f;
	}
	
	public Function<T, T> parse(String s);
	
}
