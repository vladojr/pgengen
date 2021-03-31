package sk.vladojr.pgengen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractColumn<T extends AbstractTable, C extends AbstractColumn<T, C>> {

	public final int position;
	public final String name, quotedName, castIn, castOut;

	public AbstractColumn(final int position, final String name, final String castIn, final String castOut) {
		this.position = position;
		this.name = name;
		this.castIn = castIn;
		this.castOut = castOut;
		this.quotedName = "\"" + name + "\"";
	}

	public static class SqlExpression<T extends AbstractTable, C extends AbstractColumn<T, C>> {

		public final AbstractColumn<T, C> column;
		public final String expression;

		public SqlExpression(final AbstractColumn<T, C> column, final String expression) {
			this.column = column;
			this.expression = expression;
		}
	}

	public abstract void set(final T instance, final PreparedStatement stmt, final int position) throws SQLException;

	public abstract void get(final T instance, final ResultSet rs, final int position) throws SQLException;

	public SqlExpression<T, C> expression(final String expression) {
		return new SqlExpression(this, expression);
	}

	public SqlExpression<T, C> omit() {
		return new SqlExpression(this, null);
	}

	public SqlExpression<T, C> DEFAULT() {
		return new SqlExpression(this, "DEFAULT");
	}

	public SqlExpression<T, C> CURRENT_TIMESTAMP() {
		return new SqlExpression(this, "CURRENT_TIMESTAMP");
	}

	public SqlExpression<T, C> NULL() {
		return new SqlExpression(this, "NULL");
	}

}
