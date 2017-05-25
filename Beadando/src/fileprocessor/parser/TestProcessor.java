package fileprocessor.parser;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestProcessor {

	private static final String originalDirectory = "original";
	private static final String testDirectory = "testdir";

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"test1.txt", 1000, 0, Arrays.asList(1001, 1002, 1003, 1004, 1005)},
			{"test2.txt", 1234, 13, Arrays.asList(2028, 10153, 50778, 253903, 1269528, 1983703)},
			{"test2hibakkal.txt", 1234, 13, Arrays.asList(2028, 10153, 50778, 253903, 1269528, 1983703)}
		});
	}

	@Parameter(0)
	public String fileName;
	@Parameter(1)
	public int max;
	@Parameter(2)
	public int initValue;
	@Parameter(3)
	public List<Integer> result;

	@Before
	public void initialize() {
		Path dir = Paths.get(testDirectory);

		if (Files.notExists(dir)) {
			try {
				Files.createDirectory(dir);
			} catch (IOException e) {
				System.out.println("Failed to create " + testDirectory + " directory");
			}
		}
	}

	@Test
	public void test() {
		try {
			FileProcessor<Integer, Parseable<Integer>> processor = new FileProcessor<Integer, Parseable<Integer>>(max, new ArrayList<Integer>());

			Path originalFile = Paths.get(originalDirectory, fileName);
			Path copiedFile = Paths.get(testDirectory, fileName);

			processor.processDir(initValue, testDirectory, ArithOperator.ADD);
			Thread.sleep(200);
			Files.copy(originalFile, copiedFile, REPLACE_EXISTING);
			Thread.sleep(200);
			processor.stopProcessing();

			List<Integer> log = processor.getLog();
			assertEquals(result.size(), log.size());

			assertFalse(result.retainAll(log));

			/*
			for (int i = 0; i < result.size(); i++)
				assertEquals(result.get(i), log.get(i));
			 */
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void finish() {
		Path copiedFile = Paths.get(testDirectory, fileName);

		try {
			Files.delete(copiedFile);
		} catch (IOException e) {
			System.out.println("Failed to delete " + copiedFile.getFileName());
		}
	}

}
