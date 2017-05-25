package fileprocessor.parser;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileProcessor<T extends Comparable<T>, Parser extends Parseable<T>> {

	private T max;
	private List<T> log;
	private Set<Thread> threads = new HashSet<Thread>();
	private boolean keepProcessing;

	public FileProcessor(T value, List<T> list) {
		this.setMax(value);
		this.setLog(list);
	}

	public void processDir(T initValue, String dir, Parser parser) {
		keepProcessing = true;

		Thread t = new Thread() {
			@Override
			public void run() {
				while (keepProcessing) {
					try {
						WatchService watcher = FileSystems.getDefault().newWatchService();
						WatchKey key = Paths.get(dir).register(watcher, ENTRY_MODIFY);

						WatchKey polledKey = watcher.poll(20, TimeUnit.MILLISECONDS);

						if (polledKey == key) {
							for (WatchEvent<?> event: polledKey.pollEvents()) {
								if (event.kind() == ENTRY_MODIFY) {
									Thread.sleep(10);

									String fileName = ((Path) ((WatchEvent<?>) event).context()).toString();
									Path path = Paths.get(dir, fileName);

									(Files.lines(path)).map(line -> {
										return parser.combineParsers(
												Arrays.stream(line.split(" "))
												.map(operator -> { return parser.parse(operator); })
												.collect(Collectors.toList()));
									}).reduce(initValue, (accumulator, lambda) -> {
										T value = lambda.apply(accumulator);

										if (value.compareTo(max) > 0) {
											max = value;
											log.add(value);
										}

										return value;
									}, (oldValue, newValue) -> newValue);
								}
							}

							polledKey.reset();
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		this.threads.add(t);
		t.start();
	}

	public void stopProcessing() {
		keepProcessing = false;

		threads.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		threads.clear();
	}

	public T getMax() {
		return max;
	}

	public void setMax(T max) {
		this.max = max;
	}

	public List<T> getLog() {
		return log;
	}

	public void setLog(List<T> log) {
		this.log = log;
	}

}
