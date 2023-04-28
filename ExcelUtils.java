package com.seleniumfy.utils;

/**
 * @author Annameni Srinivas
 *  <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

public class ExcelUtils {
    public static FileInputStream  fi;
    public static FileOutputStream fo;
    public static XSSFWorkbook     wb;
    public static XSSFSheet        ws;
    public static XSSFRow          row;
    public static XSSFCell         cell;

    String                         excelFile = System.getProperty ("user.dir") + "/DataFiles/LoginData.xlsx";
    String                         sheetname = "Sheet1";

    @DataProvider (name = "excelData")
    public String[][] excelDataProvider () throws IOException {

        String[][] arrayExcelData = getExcelData (excelFile, sheetname);
        return arrayExcelData;
    }
    
    @DataProvider (name = "excelProxyData")
    public String[][] excelProxyDataProvider () throws IOException {

        String[][] arrayExcelData = getExcelData (excelFile, "Sheet2");
        return arrayExcelData;
    }
    
    @DataProvider (name = "coPlannerSheet")
    public String[][] coPlannerData () throws IOException {

        String[][] arrayExcelData = getExcelData (excelFile, "Sheet3");
        return arrayExcelData;
    }
    @DataProvider (name = "uploadFiles")
    public String[][] uploadFiles () throws IOException {

        String[][] arrayExcelData = getExcelData (excelFile, "Sheet4");
        return arrayExcelData;
    }

    public String[][] getExcelData (String fileName, String sheetName) throws IOException {
        String[][] data = null;
        try {

            FileInputStream fis = new FileInputStream (fileName);
            wb = new XSSFWorkbook (fis);
            ws = wb.getSheet (sheetName);
            row = ws.getRow (0);
            int noOfRows = ws.getPhysicalNumberOfRows ();
            int noOfCols = row.getLastCellNum ();
            Cell cell;
            data = new String[noOfRows - 1][noOfCols];

            for (int i = 1; i < noOfRows; i++) {
                for (int j = 0; j < noOfCols; j++) {
                    row = ws.getRow (i);
                    cell = row.getCell (j);
                    data[i - 1][j] = cell.getStringCellValue ();
                }
            }
        } catch (Exception e) {
            System.out.println ("The exception is: " + e.getMessage ());
        }
        return data;
    }

    public static int getRowCount (String xlfile, String xlsheet) throws IOException {
        fi = new FileInputStream (xlfile);
        wb = new XSSFWorkbook (fi);
        ws = wb.getSheet (xlsheet);
        int rowCount = ws.getLastRowNum ();
        wb.close ();
        fi.close ();
        return rowCount;
    }

    public static int getCellCount (String xlfile, String xlsheet, int rownum) throws IOException {
        fi = new FileInputStream (xlfile);
        wb = new XSSFWorkbook (fi);
        ws = wb.getSheet (xlsheet);
        row = ws.getRow (rownum);
        int Cellcount = row.getLastCellNum ();
        wb.close ();
        fi.close ();
        return Cellcount;
    }

    public static String getCellData (String xlfile, String xlsheet, int rownum, int colnum) throws IOException {
        fi = new FileInputStream (xlfile);
        wb = new XSSFWorkbook (fi);
        ws = wb.getSheet (xlsheet);
        row = ws.getRow (rownum);
        cell = row.getCell (colnum);
        String data;
        try {
            DataFormatter formatter = new DataFormatter ();
            String cellData = formatter.formatCellValue (cell);
            return cellData;
        } catch (Exception e) {
            data = "";
        }
        wb.close ();
        fi.close ();
        return data;
    }

    public static void setCellData (String xlfile, String xlsheet, int rownum, int colnum,
                                    String data) throws IOException {
        fi = new FileInputStream (xlfile);
        wb = new XSSFWorkbook (fi);
        ws = wb.getSheet (xlsheet);
        row = ws.getRow (rownum);
        cell = row.createCell (colnum);
        cell.setCellValue (data);
        fo = new FileOutputStream (xlfile);
        wb.write (fo);
        wb.close ();
        fi.close ();
        fo.close ();
    }
}
