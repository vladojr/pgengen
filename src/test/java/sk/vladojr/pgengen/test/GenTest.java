package sk.vladojr.pgengen.test;

import org.junit.jupiter.api.Test;
import sk.vladojr.pgengen.PgEnGen;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class GenTest {

	public static final Connection getConnectionNoAutocommit() throws Exception {
		final Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/?user=postgres&password=postgres");
		conn.setAutoCommit(false);
		return conn;
	}

	@Test
	public void generateClasses() throws Exception {
		try (final Connection conn = getConnectionNoAutocommit()) {
			final PgEnGen pgengen = new PgEnGen();
			pgengen.generateTables(conn, "pgengentest", ".*", "sk.vladojr.pgengen.test.gen", Paths.get("src/test/java/sk/vladojr/pgengen/test/gen"));
			conn.commit();
		}
	}

}
