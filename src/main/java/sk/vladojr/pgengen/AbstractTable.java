package sk.vladojr.pgengen;

import sk.vladojr.pgengen.AbstractColumn.SqlExpression;
import sk.vladojr.pgengen.PgEnGenException.NothingUpdatedException;
import sk.vladojr.pgengen.PgEnGenException.RecordNotLoadedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AbstractTable<T extends AbstractTable> {

	private static final int QUERY_BUILDER_INIT_LENGTH = 4 * 128;

	protected Object ctid;
	protected long txid;

	protected static StringBuilder selectQuery(final String qualifiedTableName, final AbstractColumn[] columns) {
		final StringBuilder q = new StringBuilder(QUERY_BUILDER_INIT_LENGTH);
		q.append("SELECT t.ctid,txid_current(),");
		appendColumnList(q, columns, null, true, false, true);
		q.append(" FROM ").append(qualifiedTableName).append(" AS t ");
		return q;
	}

	protected static void bindParams(final PreparedStatement s, final Object[] params) throws SQLException {
		final int l = params == null ? 0 : params.length;
		for (int p = 1, x = 0; x < l; p++, x++) s.setObject(p, params[x]);
	}

	private static <T extends AbstractTable> void appendColumnList(final StringBuilder q, final AbstractColumn<T, ?>[] columns, final SqlExpression<T, ?>[] expressions, final boolean useTableAlias, final boolean castIn, final boolean castOut) {
		boolean isFirst = true;
		for (final AbstractColumn<T, ?> c : columns) {
			final SqlExpression<T, ?> e = expressions == null ? null : findExpressionByColumnPosition(expressions, c);
			if (e != null && e.expression == null) continue;//omit
			if (isFirst) isFirst = false;
			else q.append(',');
			if (useTableAlias) q.append("t.");
			q.append(c.quotedName);
			if (castOut) q.append(c.castOut);
			else if (castIn) q.append(c.castIn);
		}
	}

	private static SqlExpression findExpressionByColumnPosition(final SqlExpression[] expressions, final AbstractColumn column) {
		for (final SqlExpression e : expressions)
			if (e.column.position == column.position) return e;
		return null;
	}

	protected int load(final ResultSet rs, final AbstractColumn<T, ?>[] columns, int position) throws SQLException {
		ctid = rs.getObject(position++);
		txid = rs.getLong(position++);
		for (final AbstractColumn c : columns) c.get(this, rs, position++);
		return position;
	}

	private static <T extends AbstractTable> void appendParamPlaceholders(final StringBuilder q, final AbstractColumn<T, ?>[] columns, final SqlExpression<T, ?>[] expressions, final boolean isSetter, final boolean castIn, final boolean castOut) {
		boolean isFirst = true;
		for (final AbstractColumn<T, ?> c : columns) {
			final SqlExpression<T, ?> e = expressions == null ? null : findExpressionByColumnPosition(expressions, c);
			if (e != null && e.expression == null) continue;//omit
			if (isFirst) isFirst = false;
			else q.append(',');
			if (isSetter) q.append(c.quotedName).append('=');
			if (e == null) {
				q.append('?');
				if (castOut) q.append(c.castOut);
				else if (castIn) q.append(c.castIn);
			} else q.append(e.expression);
		}
	}

	public void updateByCtid(final String qualifiedTableName, final AbstractColumn<T, ?>[] columns, final Connection conn, final SqlExpression<T, ?>[] expressions) throws SQLException {
		if (ctid == null) throw new RecordNotLoadedException();
		final StringBuilder q = new StringBuilder(QUERY_BUILDER_INIT_LENGTH);
		q.append("UPDATE ").append(qualifiedTableName).append(" AS t SET ");
		appendParamPlaceholders(q, columns, expressions, true, true, false);
		q.append(" WHERE t.ctid=? AND pgengen.check_tx(?) RETURNING t.ctid,txid_current(),");
		appendColumnList(q, columns, null, true, false, true);
		try (final PreparedStatement stmt = conn.prepareStatement(q.toString())) {
			int position = bind(stmt, columns, expressions, 1);
			stmt.setObject(position++, ctid);
			stmt.setLong(position, txid);
			try (final ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new NothingUpdatedException();
				load(rs, columns, 1);
			}
		}
	}

	protected int bind(final PreparedStatement stmt, final AbstractColumn<T, ?>[] columns, final SqlExpression<T, ?>[] expressions, int position) throws SQLException {
		for (final AbstractColumn c : columns) {
			final SqlExpression<T, ?> e = expressions == null ? null : findExpressionByColumnPosition(expressions, c);
			if (e != null) continue;//omit
			c.set(this, stmt, position++);
		}
		return position;
	}

	public void insert(final String qualifiedTableName, final AbstractColumn<T, ?>[] columns, final Connection conn, final SqlExpression<T, ?>[] expressions) throws SQLException {
		final StringBuilder q = new StringBuilder(QUERY_BUILDER_INIT_LENGTH);
		q.append("INSERT INTO ").append(qualifiedTableName).append(" AS t (");
		appendColumnList(q, columns, expressions, false, false, false);
		q.append(") VALUES (");
		appendParamPlaceholders(q, columns, expressions, false, true, false);
		q.append(") RETURNING t.ctid,txid_current(),");
		appendColumnList(q, columns, null, true, false, true);
		try (final PreparedStatement stmt = conn.prepareStatement(q.toString())) {
			bind(stmt, columns, expressions, 1);
			try (final ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new NothingUpdatedException();
				load(rs, columns, 1);
			}
		}
	}

	public void deleteByCtid(final String qualifiedTableName, final AbstractColumn<T, ?>[] columns, final Connection conn) throws SQLException {
		if (ctid == null) throw new RecordNotLoadedException();
		final StringBuilder q = new StringBuilder(QUERY_BUILDER_INIT_LENGTH);
		q.append("DELETE FROM ").append(qualifiedTableName).append(" AS t ");
		q.append("WHERE t.ctid=? AND pgengen.check_tx(?) RETURNING t.ctid,txid_current(),");
		appendColumnList(q, columns, null, true, false, true);
		try (final PreparedStatement stmt = conn.prepareStatement(q.toString())) {
			stmt.setObject(1, ctid);
			stmt.setLong(2, txid);
			try (final ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) throw new NothingUpdatedException();
				load(rs, columns, 1);
			}
		}
	}

	@FunctionalInterface
	public interface RowProcessor<T> {
		void row(final int row, final T value) throws Exception;
	}

}
