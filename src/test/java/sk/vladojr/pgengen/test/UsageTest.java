package sk.vladojr.pgengen.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sk.vladojr.pgengen.PgEnGenException.RecordNotLoadedException;
import sk.vladojr.pgengen.Sql;
import sk.vladojr.pgengen.test.gen.TestEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UsageTest {

	@BeforeAll
	public static void prepareDatabase() throws Exception {
		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			Sql.execute(conn, "drop schema if exists pgengentest cascade;");
			Sql.execute(conn, "create schema pgengentest;");
			Sql.execute(conn, "create table pgengentest.test_entity (" +
				"id serial not null primary key," +
				"value text not null," +
				"time timestamptz null" +
				");");
			conn.commit();
		}
	}

	@Test
	public void testRetrieveNoData() throws Exception {
		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			assert TestEntity.first(conn, "where false") == null;
			final List<TestEntity> all = TestEntity.all(conn, "where false");
			assert all != null;
			assert TestEntity.all(conn, "where false").isEmpty();
		}
	}

	@Test
	public void testRetrieve() throws Exception {
		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			final TestEntity e = new TestEntity();
			assertThrows(RecordNotLoadedException.class, () -> e.updateByCtid(conn));
			assertThrows(RecordNotLoadedException.class, () -> e.deleteByCtid(conn));

			e.value = "value";
			e.insert(conn, TestEntity.Columns.id.DEFAULT());
			assert TestEntity.first(conn, "where value='value'") != null;

			e.value = "value2";
			e.updateByCtid(conn);
			assert TestEntity.first(conn, "where value='value2'") != null;
		}
	}

	@Test
	public void testUpdateDeleteNotLoaded() throws Exception {
		final TestEntity e = new TestEntity();

		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			assertThrows(RecordNotLoadedException.class, () -> e.updateByCtid(conn));
		}

		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			assertThrows(RecordNotLoadedException.class, () -> e.updateByCtid(conn));
		}
	}

	@Test
	public void testUpdateDeleteOutsideTx() throws Exception {
		final TestEntity e1;

		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			e1 = new TestEntity(0, "value", OffsetDateTime.now());
			e1.insert(conn, TestEntity.Columns.id.omit());
			conn.commit();
		}

		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			final SQLException ex = assertThrows(SQLException.class, () -> e1.updateByCtid(conn));
			assert ex.getMessage().startsWith("ERROR: txid check failed!");
		}

		try (final Connection conn = GenTest.getConnectionNoAutocommit()) {
			final SQLException ex = assertThrows(SQLException.class, () -> e1.updateByCtid(conn));
			assert ex.getMessage().startsWith("ERROR: txid check failed!");
		}
	}
}
