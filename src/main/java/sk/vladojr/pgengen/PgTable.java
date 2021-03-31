package sk.vladojr.pgengen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PgTable<T extends PgTable> {

	protected long txid;
	protected Object ctid;

	@FunctionalInterface
	public interface RowProcessor<T> {
		void row(final int row, final T value) throws Exception;
	}

	public abstract void insert(final Connection conn) throws SQLException;

	public abstract void insert(final Connection conn, final SqlExpression<T>... expressions) throws SQLException;

	public abstract void updateByCtid(final Connection conn) throws SQLException;

	public abstract void updateByCtid(final Connection conn, final SqlExpression<T>... expressions) throws SQLException;

	public abstract void deleteByCtid(final Connection conn) throws SQLException;

	protected static void bindParams(final PreparedStatement s, final Object[] params) throws SQLException {
		final int l = params == null ? 0 : params.length;
		for (int p = 0, i = 1; p < l; p++, i++) s.setObject(p, params[i]);
	}

	protected void checkLoaded() throws SQLException {
		if (ctid == null) throw new SQLException("Row must be loaded before update!");
	}

	protected static boolean getBooleanNotNull(final ResultSet rs, final String column) throws SQLException {
		return rs.getBoolean(column);
	}

	protected static Boolean getBoolean(final ResultSet rs, final String column) throws SQLException {
		final boolean value = rs.getBoolean(column);
		return rs.wasNull() ? null : value;
	}

	protected static int getIntegerNotNull(final ResultSet rs, final String column) throws SQLException {
		return rs.getInt(column);
	}

	protected static Integer getInteger(final ResultSet rs, final String column) throws SQLException {
		final int value = rs.getInt(column);
		return rs.wasNull() ? null : value;
	}

	protected static short getShortNotNull(final ResultSet rs, final String column) throws SQLException {
		return rs.getShort(column);
	}

	protected static Short getShort(final ResultSet rs, final String column) throws SQLException {
		final short value = rs.getShort(column);
		return rs.wasNull() ? null : value;
	}

	protected static long getLongNotNull(final ResultSet rs, final String column) throws SQLException {
		return rs.getLong(column);
	}

	protected static Long getLong(final ResultSet rs, final String column) throws SQLException {
		final long value = rs.getLong(column);
		return rs.wasNull() ? null : value;
	}

	protected static Character getChar(final ResultSet rs, final String column) throws SQLException {
		final String value = rs.getString(column);
		return value != null ? value.charAt(0) : null;
	}

	protected static char getCharNotNull(final ResultSet rs, final String column) throws SQLException {
		return rs.getString(column).charAt(0);
	}

	protected abstract int bindFields(final PreparedStatement s) throws SQLException;

}
