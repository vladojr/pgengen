package sk.vladojr.pgengen;

import java.util.function.Function;

public class PgColumn<T extends PgTable> {
	public final String name, type;
	public final Function<T, Object> getter;

	public PgColumn(final String name, final String type, final Function<T, Object> getter) {
		this.name = name;
		this.type = type;
		this.getter = getter;
	}

	public SqlExpression<T> expression(final String expression) {
		return new SqlExpression<>(this, expression);
	}

	public SqlExpression<T> omit() {
		return new SqlExpression<>(this, null);
	}

	public SqlExpression<T> DEFAULT() {
		return new SqlExpression<>(this, "DEFAULT");
	}

	public SqlExpression<T> CURRENT_TIMESTAMP() {
		return new SqlExpression<>(this, "CURRENT_TIMESTAMP");
	}

	public SqlExpression<T> NULL() {
		return new SqlExpression<>(this, "NULL");
	}

	public SqlExpression<T> OLD() {
		return new SqlExpression<>(this, "OLD.\"" + name + "\"");
	}

}
