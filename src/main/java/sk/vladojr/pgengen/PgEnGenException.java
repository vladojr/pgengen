package sk.vladojr.pgengen;

import java.sql.SQLException;

public class PgEnGenException extends SQLException {

	private PgEnGenException(final String message) {
		super(message);
	}

	public static class RecordNotLoadedException extends PgEnGenException {
		public RecordNotLoadedException() {
			super("Record must be loaded before modifying!");
		}
	}

	public static class NothingUpdatedException extends PgEnGenException {
		public NothingUpdatedException() {
			super("No rows were modified! (row modified already?)");
		}
	}


}
