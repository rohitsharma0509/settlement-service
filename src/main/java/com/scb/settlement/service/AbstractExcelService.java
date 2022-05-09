package com.scb.settlement.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Objects;

public abstract class AbstractExcelService {

    public CellStyle getCellStyle(Workbook workbook, IndexedColors bgColor, IndexedColors fontColor) {
        return getCellStyle(workbook, bgColor, fontColor, Boolean.FALSE, Boolean.FALSE);
    }

    public CellStyle getCellStyle(Workbook workbook, IndexedColors bgColor, IndexedColors fontColor, boolean isHeader, boolean borderRequired) {
        CellStyle style = workbook.createCellStyle();
        if(borderRequired) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
        style.setFillForegroundColor(bgColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        if(isHeader) {
            font.setBold(true);
        }
        font.setColor(fontColor.getIndex());
        style.setFont(font);
        return style;
    }

    public void createRow(Sheet sheet, int rowNumber, List<String> values, CellStyle style) {
        Row row = sheet.createRow(rowNumber);
        for (int i = 0; i < values.size(); i++) {
            createCell(row, i, values.get(i), style);
        }
    }

    public void createCellsWith2Styles(Row row, List<String> style1Values, List<String> style2Values, CellStyle style1, CellStyle style2) {
        int cellCount = 0;
        for (int i = 0; i < style1Values.size(); i++) {
            createCell(row, cellCount, style1Values.get(i), style1);
            cellCount++;
        }

        for (int i = 0; i < style2Values.size(); i++) {
            createCell(row, cellCount, style2Values.get(i), style2);
            cellCount++;
        }
    }

    public void createCells(Row row, List<String> values, CellStyle style) {
        for (int i = 0; i < values.size(); i++) {
            createCell(row, i, values.get(i), style);
        }
    }

    public Cell createCell(Row row, int cellCount, String value, CellStyle style) {
        Cell cell = row.createCell(cellCount);
        if(Objects.nonNull(style)) {
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(style);
        }
        cell.setCellValue(value);
        return cell;
    }

    public void createMergedCell(Sheet sheet, Row row, int cellNumber, String cellValue, CellRangeAddress cellRangeAddress, CellStyle style) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(cellValue);
        if(Objects.nonNull(style)) {
            style.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(style);
        }

        sheet.addMergedRegion(cellRangeAddress);
    }
}
