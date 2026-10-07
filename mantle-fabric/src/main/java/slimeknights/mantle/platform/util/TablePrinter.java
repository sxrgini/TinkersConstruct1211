package slimeknights.mantle.platform.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** Builds a plain text table for logging, equivalent of Forge's TablePrinter */
public class TablePrinter<T> {
  private record Column<T>(String header, Function<T,String> getter) {}

  private final List<Column<T>> columns = new ArrayList<>();
  private final List<T> rows = new ArrayList<>();

  /** Adds a column */
  public void header(String name, Function<T,String> getter) {
    columns.add(new Column<>(name, getter));
  }

  /** Adds a row */
  public void add(T row) {
    rows.add(row);
  }

  /** Adds all rows */
  public void add(Collection<? extends T> rows) {
    this.rows.addAll(rows);
  }

  /** Appends the formatted table to the builder */
  public void build(StringBuilder builder) {
    int count = columns.size();
    int[] widths = new int[count];
    List<String[]> cells = new ArrayList<>(rows.size());
    for (int i = 0; i < count; i++) {
      widths[i] = columns.get(i).header.length();
    }
    for (T row : rows) {
      String[] line = new String[count];
      for (int i = 0; i < count; i++) {
        String value = columns.get(i).getter.apply(row);
        line[i] = value == null ? "" : value;
        widths[i] = Math.max(widths[i], line[i].length());
      }
      cells.add(line);
    }
    String[] header = new String[count];
    for (int i = 0; i < count; i++) {
      header[i] = columns.get(i).header;
    }
    appendLine(builder, header, widths);
    for (int i = 0; i < count; i++) {
      builder.append(i == 0 ? "" : "-+-").append("-".repeat(widths[i]));
    }
    builder.append(System.lineSeparator());
    for (String[] line : cells) {
      appendLine(builder, line, widths);
    }
  }

  private static void appendLine(StringBuilder builder, String[] line, int[] widths) {
    for (int i = 0; i < line.length; i++) {
      if (i > 0) {
        builder.append(" | ");
      }
      builder.append(line[i]).append(" ".repeat(widths[i] - line[i].length()));
    }
    builder.append(System.lineSeparator());
  }
}
