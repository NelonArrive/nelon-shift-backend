package nelon.arrive.nelonshift.services;

import nelon.arrive.nelonshift.dto.ProjectDTO;
import nelon.arrive.nelonshift.dto.ShiftDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ProjectExcelExportService {
	public ByteArrayInputStream exportProject(
		ProjectDTO project,
		List<ShiftDTO> shifts
	) throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Табель");
			
			List<ShiftDTO> sortedShifts = shifts.stream()
				.sorted((a, b) -> a.getDate().compareTo(b.getDate()))
				.toList();
			
			// Создаём стили
			CellStyle boldStyle = createBoldStyle(workbook);
			CellStyle headerStyle = createHeaderStyle(workbook);
			CellStyle numberStyle = createNumberStyle(workbook);
			CellStyle borderStyle = createBorderStyle(workbook);
			
			// Строка 1: Общая сумма
			createCell(sheet, 0, 0, "Общая сумма", boldStyle);
			Cell totalCell = createCell(sheet, 0, 1, "", boldStyle);
			totalCell.setCellFormula("K12");
			
			// Строка 3: Имя проекта
			createCell(sheet, 2, 0, project.getName(), boldStyle);
			
			// Подготавливаем данные
			int dataStartCol = 1;
			int hoursColumn = dataStartCol + sortedShifts.size();
			int sumColumn = hoursColumn + 1;
			
			// Строка 4: Заголовки
			createCell(sheet, 3, 0, "Дата", headerStyle);
			createCell(sheet, 3, hoursColumn, "Часы", headerStyle);
			createCell(sheet, 3, sumColumn, "Сумма", headerStyle);
			
			// Заполняем даты
			for (int i = 0; i < sortedShifts.size(); i++) {
				LocalDate date = sortedShifts.get(i).getDate();
				String formattedDate = formatDate(date);
				Cell cell = createCell(sheet, 3, dataStartCol + i, formattedDate, borderStyle);
				cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
			}
			
			// Строка 5: Время
			createCell(sheet, 4, 0, "Время", headerStyle);
			for (int i = 0; i < sortedShifts.size(); i++) {
				ShiftDTO shift = sortedShifts.get(i);
				String timeStr = shift.getStartTime() + "-" + shift.getEndTime();
				Cell cell = createCell(sheet, 4, dataStartCol + i, timeStr, borderStyle);
				cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
			}
			
			// Строка 6: Смена (часы)
			createCell(sheet, 5, 0, "Смена", headerStyle);
			for (int i = 0; i < sortedShifts.size(); i++) {
				Cell cell = createCell(sheet, 5, dataStartCol + i,
					sortedShifts.get(i).getHours(), borderStyle);
				cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
			}
			// Формула суммы часов
			String hoursFormula = createColumnRange(dataStartCol,
				sortedShifts.size(), 5);
			Cell hourTotalCell = createCell(sheet, 5, hoursColumn, "", boldStyle);
			hourTotalCell.setCellFormula("SUM(" + hoursFormula + ")");
			
			// Сумма за смены
			Cell shiftSumCell = createCell(sheet, 5, sumColumn, "", numberStyle);
			shiftSumCell.setCellFormula(getCellAddress(5, hoursColumn) + "/12*4000");
			
			// Строка 7: Переработка
			createCell(sheet, 6, 0, "Переработка", headerStyle);
			for (int i = 0; i < sortedShifts.size(); i++) {
				Integer overtime = sortedShifts.get(i).getOvertimeHours();
				if (overtime != null && overtime > 0) {
					Cell cell = createCell(sheet, 6, dataStartCol, overtime, borderStyle);
					cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
				}
			}
			// Формула суммы переработок
			String overtimeFormula = createColumnRange(dataStartCol,
				sortedShifts.size(), 6);
			Cell overtimeTotalCell = createCell(sheet, 6, hoursColumn, "", boldStyle);
			overtimeTotalCell.setCellFormula("SUM(" + overtimeFormula + ")");
			
			// Сумма за переработки
			Cell overtimeSumCell = createCell(sheet, 6, sumColumn, "", numberStyle);
			
			
			return null;
		} catch (Exception e) {
			throw new IOException("Ошибка при экспорте в Excel", e);
		}
	}
	
	private Cell createCell(Sheet sheet, int row, int col, Object value, CellStyle style) {
		Row sheetRow = sheet.getRow(row);
		if (sheetRow == null) {
			sheetRow = sheet.createRow(row);
		}
		Cell cell = sheetRow.createCell(col);
		
		if (value instanceof String) {
			cell.setCellValue((String) value);
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
		}
		
		if (style != null) {
			cell.setCellStyle(style);
		}
		
		return cell;
	}
	
	
	private CellStyle createBoldStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}
	
	private CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = createBoldStyle(workbook);
		style.setAlignment(HorizontalAlignment.CENTER);
		return style;
	}
	
	private CellStyle createNumberStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
		return style;
	}
	
	private CellStyle createBorderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		BorderStyle thin = BorderStyle.THIN;
		style.setBorderTop(thin);
		style.setBorderBottom(thin);
		style.setBorderLeft(thin);
		style.setBorderRight(thin);
		return style;
	}
	
	private String formatDate(LocalDate date) {
		return date.format
			(DateTimeFormatter.ofPattern("d.MMM", Locale.forLanguageTag("ru")));
	}
	
	private String createColumnRange(int startCol, int count, int row) {
		String startCell = getCellAddress(row, startCol);
		String endCell = getCellAddress(row, startCol + count - 1);
		return startCell + ":" + endCell;
	}
	
	private String getCellAddress(int row, int col) {
		return (char) ('A' + col) + String.valueOf(row + 1);
	}
}
