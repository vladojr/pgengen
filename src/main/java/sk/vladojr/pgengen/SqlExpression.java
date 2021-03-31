package sk.vladojr.pgengen;

public class SqlExpression<T extends PgTable> {
	public final PgColumn<T> column;
	public final String expression;

	public SqlExpression(final PgColumn<T> column, final String expression) {
		this.column = column;
		this.expression = expression;
	}
}
