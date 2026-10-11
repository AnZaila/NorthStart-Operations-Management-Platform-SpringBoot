package com.nomp.northstar.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelWeb {
  private ExcelWeb() {}

  public static void write(
      HttpServletResponse response, String filename, List<String> headers, List<List<Object>> rows)
      throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("UTF-8");
    response.setHeader(
        "Content-Disposition",
        "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("data");
      Row head = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        head.createCell(i).setCellValue(headers.get(i));
        sheet.setColumnWidth(i, 18 * 256);
      }
      int r = 1;
      for (List<Object> row : rows) {
        Row excelRow = sheet.createRow(r++);
        for (int i = 0; i < row.size(); i++) {
          Object value = row.get(i);
          excelRow.createCell(i).setCellValue(value == null ? "" : String.valueOf(value));
        }
      }
      workbook.write(response.getOutputStream());
    }
  }

  public static List<Map<String, String>> read(InputStream input) throws IOException {
    DataFormatter formatter = new DataFormatter();
    try (Workbook workbook = WorkbookFactory.create(input)) {
      Sheet sheet = workbook.getSheetAt(0);
      Row head = sheet.getRow(0);
      if (head == null) {
        return List.of();
      }
      List<String> headers = new ArrayList<>();
      for (int i = 0; i < head.getLastCellNum(); i++) {
        headers.add(formatter.formatCellValue(head.getCell(i)).trim());
      }
      List<Map<String, String>> rows = new ArrayList<>();
      for (int r = 1; r <= sheet.getLastRowNum(); r++) {
        Row row = sheet.getRow(r);
        if (row == null) {
          continue;
        }
        Map<String, String> item = new LinkedHashMap<>();
        boolean empty = true;
        for (int i = 0; i < headers.size(); i++) {
          Cell cell = row.getCell(i);
          String value = formatter.formatCellValue(cell).trim();
          if (!value.isEmpty()) {
            empty = false;
          }
          item.put(headers.get(i), value);
        }
        if (!empty) {
          rows.add(item);
        }
      }
      return rows;
    }
  }
}
