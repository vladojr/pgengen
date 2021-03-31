package sk.vladojr.pgengen;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PgEnGen {

	private static final Map<String, Class> type = new HashMap<>();
	private static final Map<String, String> cast = new HashMap<>(), setter = new HashMap<>(), getter = new HashMap<>();

	static {
		type.put("bool", Boolean.class);type.put("boolNotNull", boolean.class);
		type.put("bpchar", String.class);type.put("bpcharNotNull", String.class);
		type.put("bytea", byte[].class);type.put("byteaNotNull", byte[].class);
		type.put("\"char\"", Character.class);type.put("\"char\"NotNull", char.class);
		type.put("inet", String.class);type.put("inetNotNull", String.class);
		type.put("int2", Short.class);type.put("int2NotNull", short.class);
		type.put("int4", Integer.class);type.put("int4NotNull", int.class);
		type.put("int8", Long.class);type.put("int8NotNull", long.class);
		type.put("float4", Float.class);type.put("float4NotNull", float.class);
		type.put("jsonb", String.class);type.put("jsonbNotNull", String.class);
		type.put("name", String.class);type.put("nameNotNull", String.class);
		type.put("text", String.class);type.put("textNotNull", String.class);
		type.put("timestamptz", OffsetDateTime.class);type.put("timestamptzNotNull", OffsetDateTime.class);
		type.put("date", LocalDate.class);type.put("dateNotNull", LocalDate.class);
		type.put("varchar", String.class);type.put("varcharNotNull", String.class);

		cast.put("bool", "");cast.put("boolNotNull", "");
		cast.put("bpchar", "");cast.put("bpcharNotNull", "");
		cast.put("bytea", "");cast.put("byteaNotNull", "");
		cast.put("\"char\"", "::INT");cast.put("\"char\"NotNull", "::INT");
		cast.put("inet", "::TEXT");cast.put("inetNotNull", "::TEXT");
		cast.put("int2", "");cast.put("int2NotNull", "");
		cast.put("int4", "");cast.put("int4NotNull", "");
		cast.put("int8", "");cast.put("int8NotNull", "");
		cast.put("float4", "");cast.put("float4NotNull", "");
		cast.put("jsonb", "::TEXT");cast.put("jsonbNotNull", "::TEXT");
		cast.put("name", "::TEXT");cast.put("nameNotNull", "::TEXT");
		cast.put("text", "");cast.put("textNotNull", "");
		cast.put("timestamptz", "");cast.put("timestamptzNotNull", "");
		cast.put("date", "");cast.put("dateNotNull", "");
		cast.put("varchar", "");cast.put("varcharNotNull", "");

		setter.put("bool", "stmt.setObject(position, instance.$col);");setter.put("boolNotNull", "stmt.setBoolean(position, instance.$col);");
		setter.put("bpchar", "stmt.setString(position, instance.$col);");setter.put("bpcharNotNull", "stmt.setString(position, instance.$col);");
		setter.put("bytea", "stmt.setBytes(position, instance.$col);");setter.put("byteaNotNull", "stmt.setBytes(position, instance.$col);");
		setter.put("\"char\"", "stmt.setObject(position, instance.$col == null ? null : (int) instance.$col);");
		setter.put("\"char\"NotNull", "stmt.setInt(position, instance.$col);");
		setter.put("inet", "stmt.setString(position, instance.$col);");setter.put("inetNotNull", "stmt.setString(position, instance.$col);");
		setter.put("int2", "stmt.setObject(position, instance.$col);");setter.put("int2NotNull", "stmt.setShort(position, instance.$col);");
		setter.put("int4", "stmt.setObject(position, instance.$col);");setter.put("int4NotNull", "stmt.setInt(position, instance.$col);");
		setter.put("int8", "stmt.setObject(position, instance.$col);");setter.put("int8NotNull", "stmt.setLong(position, instance.$col);");
		setter.put("float4", "stmt.setObject(position, instance.$col);");setter.put("float4NotNull", "stmt.setFloat(position, instance.$col);");
		setter.put("jsonb", "stmt.setString(position, instance.$col);");setter.put("jsonbNotNull", "stmt.setString(position, instance.$col);");
		setter.put("name", "stmt.setString(position, instance.$col);");setter.put("nameNotNull", "stmt.setString(position, instance.$col);");
		setter.put("text", "stmt.setString(position, instance.$col);");setter.put("textNotNull", "stmt.setString(position, instance.$col);");
		setter.put("timestamptz", "stmt.setObject(position, instance.$col);");setter.put("timestamptzNotNull", "stmt.setObject(position, instance.$col);");
		setter.put("date", "stmt.setObject(position, instance.$col);");setter.put("dateNotNull", "stmt.setObject(position, instance.$col);");
		setter.put("varchar", "stmt.setString(position, instance.$col);");setter.put("varcharNotNull", "stmt.setString(position, instance.$col);");

		getter.put("bool", "instance.$col = rs.getObject(position, Boolean.class);");getter.put("boolNotNull", "instance.$col = rs.getBoolean(position);");
		getter.put("bpchar", "instance.$col = rs.getString(position);");getter.put("bpcharNotNull", "instance.$col = rs.getString(position);");
		getter.put("bytea", "instance.$col = rs.getBytes(position);");getter.put("byteaNotNull", "instance.$col = rs.getBytes(position);");
		getter.put("\"char\"", "final int v = rs.getInt(position); instance.$col = rs.wasNull()?null:(char)v;");
		getter.put("\"char\"NotNull", "instance.$col = (char) rs.getInt(position);");
		getter.put("inet", "instance.$col = rs.getString(position);");getter.put("inetNotNull", "instance.$col = rs.getString(position);");
		getter.put("int2", "instance.$col = rs.getObject(position, Short.class);");getter.put("int2NotNull", "instance.$col = rs.getShort(position);");
		getter.put("int4", "instance.$col = rs.getObject(position, Integer.class);");getter.put("int4NotNull", "instance.$col = rs.getInt(position);");
		getter.put("int8", "instance.$col = rs.getObject(position, Long.class);");getter.put("int8NotNull", "instance.$col = rs.getLong(position);");
		getter.put("float4", "instance.$col = rs.getObject(position, Float.class);");getter.put("float4NotNull", "instance.$col = rs.getFloat(position);");
		getter.put("jsonb", "instance.$col = rs.getString(position);");getter.put("jsonbNotNull", "instance.$col = rs.getString(position);");
		getter.put("name", "instance.$col = rs.getString(position);");getter.put("nameNotNull", "instance.$col = rs.getString(position);");
		getter.put("text", "instance.$col = rs.getString(position);");getter.put("textNotNull", "instance.$col = rs.getString(position);");
		getter.put("timestamptz", "instance.$col = rs.getObject(position, java.time.OffsetDateTime.class);");getter.put("timestamptzNotNull", "instance.$col = rs.getObject(position, java.time.OffsetDateTime.class);");
		getter.put("date", "instance.$col = rs.getObject(position, java.time.LocalDate.class);");getter.put("dateNotNull", "instance.$col = rs.getObject(position, java.time.LocalDate.class);");
		getter.put("varchar", "instance.$col = rs.getString(position);");getter.put("varcharNotNull", "instance.$col = rs.getString(position);");

	}

	private String normalize(final String name) {
		return Arrays.stream(name.split("_")).filter(s -> s.length() > 0).map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(Collectors.joining());
	}

	private void generateTable(final Writer writer, final String pkg, final String schema, final String table, final String cls, final List<Col> columns) throws Exception {
		final String pkgCls = pkg + "." + cls;
		writer
			.append("package ").append(pkg).append(";\n ")
			.append("\n")
			.append("import java.sql.PreparedStatement;\n")
			.append("import java.sql.ResultSet;\n")
			.append("import java.sql.SQLException;\n")
			.append("import java.sql.Connection;\n")
			.append("import java.util.ArrayList;\n")
			.append("import java.util.List;\n")
			.append("\n")
			.append("import sk.vladojr.pgengen.*;\n")
			.append("\n")
			.append("public final class ").append(cls).append(" extends sk.vladojr.pgengen.AbstractTable<").append(pkgCls).append("> {\n")
			.append("\n")
			.append("\tpublic static final String tableSchema = \"").append(schema).append("\";\n")
			.append("\tpublic static final String tableName = \"").append(table).append("\";\n")
			.append("\tpublic static final String qualifiedTableName = \"\\\"").append(schema).append("\\\".\\\"").append(table).append("\\\"\";\n")
			.append("\n")
			.append("\tprivate static final AbstractColumn<").append(pkgCls).append(", ?>[] columns = new AbstractColumn[]{")
			.append(columns.stream()
				.map(c -> "Columns." + c.name)
				.collect(Collectors.joining(", ")))
			.append("\t};\n")
			.append("\tprivate static final String selectQuery = selectQuery(qualifiedTableName, columns).toString();\n")
			.append("\n")
			.append("\tpublic static final class Columns {\n")
			.append(columns.stream()
				.map(c -> "\t\tpublic static final AbstractColumn<" + pkgCls + ", ?> " + c.name + " = new " + c.cls + "(" + c.position + ",\"" + c.name + "\",\"::" + c.rawType.replace("\"", "\\\"") + "\",\"" + cast.get(c.type) + "\");\n")
				.collect(Collectors.joining()))
			.append("\t}\n")
			.append("\n")
			.append("\tpublic ").append(cls).append("() {\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic ").append(cls).append("(")
			.append(columns.stream()
				.map(c -> "final " + type.get(c.type).getCanonicalName() + " " + c.name)
				.collect(Collectors.joining(",")))
			.append(") {\n")
			.append(columns.stream()
				.map(c -> "this." + c.name + " = " + c.name + ";\n")
				.collect(Collectors.joining()))
			.append("\t}\n")
			.append("\n")
			.append("\tpublic ").append(cls).append("(final ResultSet rs, final int position) throws SQLException {\n")
			.append("\t\tload(rs, columns, position);\n")
			.append("\t}\n")
			.append("\n")
			.append(columns.stream()
				.map(c -> "\tpublic " + type.get(c.type).getCanonicalName() + " " + c.name + ";\n")
				.collect(Collectors.joining()))
			.append("\n")
			.append(columns.stream()
				.map(c -> generateColumn(pkgCls, c))
				.collect(Collectors.joining()))
			.append("\tpublic static ").append(pkgCls).append(" first(final Connection conn, final String where, final Object... params) throws SQLException {\n")
			.append("\t\ttry (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {\n")
			.append("\t\t\tbindParams(s, params);\n")
			.append("\t\t\ttry (final ResultSet rs = s.executeQuery()) {\n")
			.append("\t\t\t\treturn rs.next() ? new ").append(pkgCls).append("(rs, 1) : null;\n")
			.append("\t\t\t}\n")
			.append("\t\t}\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic static List<").append(pkgCls).append("> all(final Connection conn, final String where, final Object... params) throws SQLException {\n")
			.append("\t\tfinal List<").append(pkgCls).append("> result = new ArrayList<>();\n")
			.append("\t\ttry (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {\n")
			.append("\t")
			.append("\t\tbindParams(s, params);\n")
			.append("\t\t\ttry (final ResultSet rs = s.executeQuery()) {\n")
			.append("\t\t\t\twhile(rs.next()){\n")
			.append("\t\t\t\t\tresult.add(new ").append(pkgCls).append("(rs, 1));\n")
			.append("\t\t\t\t}\n")
			.append("\t\t\t}\n")
			.append("\t\t}\n")
			.append("\t\treturn result;\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic static int each(final Connection conn, final RowProcessor<").append(pkgCls).append("> rowProcessor, final String where, final Object... params) throws Exception {\n")
			.append("\t\tint x = 0;\n")
			.append("\t\ttry (final PreparedStatement s = conn.prepareStatement(where != null ? (selectQuery + where) : selectQuery)) {\n")
			.append("\t\t\tbindParams(s, params);\n")
			.append("\t\t\ttry (final ResultSet rs = s.executeQuery()) {\n")
			.append("\t\t\t\twhile(rs.next()){\n")
			.append("\t\t\t\t\trowProcessor.row(x, new ").append(pkgCls).append("(rs, 1));\n")
			.append("\t\t\t\t\tx++;\n")
			.append("\t\t\t\t}\n")
			.append("\t\t\t}\n")
			.append("\t\t}\n")
			.append("\t\treturn x;\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic void insert(final Connection conn, final AbstractColumn.SqlExpression... expressions) throws SQLException {\n")
			.append("\t\tinsert(qualifiedTableName, columns, conn, expressions);\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic void updateByCtid(final Connection conn, final AbstractColumn.SqlExpression... expressions) throws SQLException {\n")
			.append("\t\tupdateByCtid(qualifiedTableName, columns, conn, expressions);\n")
			.append("\t}\n")
			.append("\n")
			.append("\tpublic void deleteByCtid(final Connection conn) throws SQLException {\n")
			.append("\t\tdeleteByCtid(qualifiedTableName, columns, conn);\n")
			.append("\t}\n")
			.append("}")
			.toString();
	}

	private String generateColumn(final String tableCls, final Col c) {
		return new StringBuilder()
			.append("\tprivate static class ").append(c.cls).append(" extends AbstractColumn<").append(tableCls).append(", ").append(c.cls).append("> {\n")
			.append("\n")
			.append("\t\tprivate ").append(c.cls).append("(final int position, final String name, final String castIn, final String castOut) {\n")
			.append("\t\t\tsuper(position, name, castIn, castOut);\n")
			.append("\t\t}\n")
			.append("\n")
			.append("\t\t@Override\n")
			.append("\t\tpublic void set(final ").append(tableCls).append(" instance, final PreparedStatement stmt, final int position) throws SQLException {\n")
			.append("\t\t\t").append(setter.get(c.type).replaceAll("\\$col", c.name)).append("\n")
			.append("\t\t}\n")
			.append("\n")
			.append("\t\t@Override\n")
			.append("\t\tpublic void get(final ").append(tableCls).append(" instance, final ResultSet rs, final int position) throws SQLException {\n")
			.append("\t\t\t").append(getter.get(c.type).replaceAll("\\$col", c.name)).append("\n")
			.append("\t\t}\n")
			.append("\t}\n")
			.append("\n")
			.toString();
	}

	public void generateTables(final Connection conn, final String schemaPattern, final String tablePattern, final String pkg, final Path destination) throws Exception {
		final Path tmpDestination = Files.createTempDirectory("pgengen");
		tmpDestination.toFile().deleteOnExit();

		try (final Sql q = Sql.from(conn, "SELECT " +
			"table_schema, table_name, column_name, is_nullable='YES' is_nullable, udt_name, ordinal_position " +
			"FROM information_schema.\"columns\" " +
			"WHERE table_schema ~ ? AND table_name ~ ? " +
			"ORDER BY table_schema, table_name, ordinal_position", schemaPattern, tablePattern)) {
			String table = null, schema = null;
			List<Col> columns = null;
			while (q.next()) {
				final String s = q.getString("table_schema");
				final String t = q.getString("table_name");
				final Col column = new Col();
				column.position = q.getInt("ordinal_position");
				column.name = q.getString("column_name");
				column.cls = normalize(column.name) + "Column";
				column.nullable = q.getBoolean("is_nullable");
				column.rawType = q.getString("udt_name");
				if ("char".equals(column.rawType)) column.rawType = "\"char\"";
				column.type = column.rawType + (column.nullable ? "" : "NotNull");

				if (table == null) {
					table = t;
					schema = s;
					columns = new ArrayList<>();
				}

				if (table.equals(t)) {
					columns.add(column);

				} else {
					generateTables(tmpDestination, schema, table, pkg, columns);

					table = t;
					schema = s;
					columns = new ArrayList<>();
					columns.add(column);
				}
			}

			generateTables(tmpDestination, schema, table, pkg, columns);
		}

		Files.walk(destination).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		Files.createDirectories(destination);
		Files.move(tmpDestination, destination, StandardCopyOption.REPLACE_EXISTING);
	}

	private void generateTables(final Path destination, final String schema, final String table, final String pkg, final List<Col> columns) throws Exception {
		final String tableCls = normalize(table);
		final Path dst = destination.resolve(tableCls + ".java");
		try (final Writer writer = Files.newBufferedWriter(dst)) {
			System.out.println(tableCls + ":" + dst.toAbsolutePath().toString());
			generateTable(writer, pkg, schema, table, tableCls, columns);
		}
	}

	private class Col {
		int position;
		String name, cls, type, rawType;
		boolean nullable;
	}

}
