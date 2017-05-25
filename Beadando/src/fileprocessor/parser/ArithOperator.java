package fileprocessor.parser;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum ArithOperator implements Parseable<Integer> {
	
	ADD((x, y) -> x + y),
	SUB((x, y) -> x - y),
	MUL((x, y) -> x * y),
	DIV((x, y) -> x / y);
		
	private BiFunction<Integer, Integer, Integer> op;
	
	private static final Pattern regex = Pattern.compile(
			"^(" + 
			Arrays.asList(ArithOperator.values()).stream()
			.map(ArithOperator::toString)
			.collect(Collectors.joining("|")) + 
			"),(\\d+)$"
		);
		
	private ArithOperator(BiFunction<Integer, Integer, Integer> operator) {
		this.setOp(operator);
	}
	
	@Override
	public Function<Integer, Integer> parse(String s) {
		Matcher m = regex.matcher(s);
		
		if (!m.find()) return null;
		
		ArithOperator operator = ArithOperator.valueOf(m.group(1));
		int n = Integer.parseInt(m.group(2));
		
		// Function<Integer, Integer> lambda = x -> operator.getOp().apply(x, n);
		return (x -> operator.getOp().apply(x, n));
	}

	public BiFunction<Integer, Integer, Integer> getOp() {
		return op;
	}

	public void setOp(BiFunction<Integer, Integer, Integer> op) {
		this.op = op;
	}

}
