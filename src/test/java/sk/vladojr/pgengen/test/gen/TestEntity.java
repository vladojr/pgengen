package sk.vladojr.pgengen.test.gen;
 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import sk.vladojr.pgengen.*;

public final class TestEntity extends sk.vladojr.pgengen.AbstractTable<sk.vladojr.pgengen.test.gen.TestEntity> {

	public static final String tableSchema = "pgengentest";
	public static final String tableName = "test_entity";
	public static final String qualifiedTableName = "\"pgengentest\".\"test_entity\"";

	private static final AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, ?>[] columns = new AbstractColumn[]{Columns.id, Columns.value, Columns.time	};
	private static final String selectQuery = selectQuery(qualifiedTableName, columns).toString();

	public static final class Columns {
		public static final AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, ?> id = new IdColumn(1,"id","::int4","");
		public static final AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, ?> value = new ValueColumn(2,"value","::text","");
		public static final AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, ?> time = new TimeColumn(3,"time","::timestamptz","");
	}

	public TestEntity() {
	}

	public TestEntity(final int id,final java.lang.String value,final java.time.OffsetDateTime time) {
this.id = id;
this.value = value;
this.time = time;
	}

	public TestEntity(final ResultSet rs, final int position) throws SQLException {
		load(rs, columns, position);
	}

	public int id;
	public java.lang.String value;
	public java.time.OffsetDateTime time;

	private static class IdColumn extends AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, IdColumn> {

		private IdColumn(final int position, final String name, final String castIn, final String castOut) {
			super(position, name, castIn, castOut);
		}

		@Override
		public void set(final sk.vladojr.pgengen.test.gen.TestEntity instance, final PreparedStatement stmt, final int position) throws SQLException {
			stmt.setInt(position, instance.id);
		}

		@Override
		public void get(final sk.vladojr.pgengen.test.gen.TestEntity instance, final ResultSet rs, final int position) throws SQLException {
			instance.id = rs.getInt(position);
		}
	}

	private static class ValueColumn extends AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, ValueColumn> {

		private ValueColumn(final int position, final String name, final String castIn, final String castOut) {
			super(position, name, castIn, castOut);
		}

		@Override
		public void set(final sk.vladojr.pgengen.test.gen.TestEntity instance, final PreparedStatement stmt, final int position) throws SQLException {
			stmt.setString(position, instance.value);
		}

		@Override
		public void get(final sk.vladojr.pgengen.test.gen.TestEntity instance, final ResultSet rs, final int position) throws SQLException {
			instance.value = rs.getString(position);
		}
	}

	private static class TimeColumn extends AbstractColumn<sk.vladojr.pgengen.test.gen.TestEntity, TimeColumn> {

		private TimeColumn(final int position, final String name, final String castIn, final String castOut) {
			super(position, name, castIn, castOut);
		}

		@Override
		public void set(final sk.vladojr.pgengen.test.gen.TestEntity instance, final PreparedStatement stmt, final int position) throws SQLException {
			stmt.setObject(position, instance.time);
		}

		@Override
		public void get(final sk.vladojr.pgengen.test.gen.TestEntity instance, final ResultSet rs, final int position) throws SQLException {
			instance.time = rs.getObject(position, java.time.OffsetDateTime.class);
		}
	}

	public static sk.vladojr.pgengen.test.gen.TestEntity first(final Connection conn, final String where, final Object... params) throws SQLException {
		try (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {
			bindParams(s, params);
			try (final ResultSet rs = s.executeQuery()) {
				return rs.next() ? new sk.vladojr.pgengen.test.gen.TestEntity(rs, 1) : null;
			}
		}
	}

	public static List<sk.vladojr.pgengen.test.gen.TestEntity> all(final Connection conn, final String where, final Object... params) throws SQLException {
		final List<sk.vladojr.pgengen.test.gen.TestEntity> result = new ArrayList<>();
		try (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {
			bindParams(s, params);
			try (final ResultSet rs = s.executeQuery()) {
				while(rs.next()){
					result.add(new sk.vladojr.pgengen.test.gen.TestEntity(rs, 1));
				}
			}
		}
		return result;
	}

	public static int each(final Connection conn, final RowProcessor<sk.vladojr.pgengen.test.gen.TestEntity> rowProcessor, final String where, final Object... params) throws Exception {
		int x = 0;
		try (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {
			bindParams(s, params);
			try (final ResultSet rs = s.executeQuery()) {
				while(rs.next()){
					rowProcessor.row(x, new sk.vladojr.pgengen.test.gen.TestEntity(rs, 1));
					x++;
				}
			}
		}
		return x;
	}

	public void insert(final Connection conn, final AbstractColumn.SqlExpression... expressions) throws SQLException {
		insert(qualifiedTableName, columns, conn, expressions);
	}

	public void updateByCtid(final Connection conn, final AbstractColumn.SqlExpression... expressions) throws SQLException {
		updateByCtid(qualifiedTableName, columns, conn, expressions);
	}

	public void deleteByCtid(final Connection conn) throws SQLException {
		deleteByCtid(qualifiedTableName, columns, conn);
	}
}