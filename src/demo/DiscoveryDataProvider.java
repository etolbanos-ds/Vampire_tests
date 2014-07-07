package demo;

import java.io.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class DiscoveryDataProvider 
{

    public String[][] getTableArray(final String sheetName, final String tableName) throws Exception 
    {
        String[][] tabArray = null;

        try 
        {
            //Workbook workbook = Workbook.getWorkbook(new File("src\\com\\discovery\\restapi\\helper\\URLsToTest.xls"));
        	Workbook workbook = Workbook.getWorkbook(new File("src"+File.separator+"com"+File.separator+"discovery"+File.separator+"restapi"+File.separator+"helper"+File.separator+"URLsToTest.xls"));

            Sheet sheet = workbook.getSheet(sheetName);

            Cell[] boundaryCells = fndCell1(sheet, tableName);
            Cell startCell = boundaryCells[0];
            Cell endCell = boundaryCells[1];

            int startRow, startCol, endRow, endCol, ci, cj;

            startRow = startCell.getRow();
            startCol = startCell.getColumn();

            endRow = endCell.getRow();
            endCol = endCell.getColumn();

            System.out.println("startRow=" + startRow + ", endRow=" + endRow + ", " + "startCol=" + startCol + ", endCol=" + endCol);

            tabArray = new String[endRow - startRow - 1][endCol - startCol - 1];

            ci = 0;
            for (int i = startRow + 1; i < endRow; i++, ci++) 
            {
                cj = 0;
                for (int j = startCol + 1; j < endCol; j++, cj++) 
                {
                    tabArray[ci][cj] = sheet.getCell(j, i).getContents();
                }
            }

            // following function print two dimensional array
            /**
             * System.out.println("now it will print two dimensional array");
             * for (int r = 0; r < tabArray.length; r++) {
             * for (int c = 0; c < tabArray[r].length; c++) {
             * System.out.print(" " + tabArray[r][c]);
             * }
             * System.out.println("");
             * }
             **/

        } 
        catch (Exception e) 
        {
            System.out.println("File not found exception");
        }
        return (tabArray);
    }


    public String[][] ReadDatafrmExcel(final String sheetName, final String tableName, final String FilePath) throws Exception 
    {
        String[][] tabArray = null;

        try 
        {
            Workbook workbook = Workbook.getWorkbook(new File(FilePath));
            Sheet sheet = workbook.getSheet(sheetName);
            Cell[] boundaryCells = fndCell1(sheet, tableName);
            Cell startCell = boundaryCells[0];
            Cell endCell = boundaryCells[1];
            int startRow, startCol, endRow, endCol, ci, cj;
            startRow = startCell.getRow();
            startCol = startCell.getColumn();
            endRow = endCell.getRow();
            endCol = endCell.getColumn();
//            System.out.println("startRow=" + startRow + ", endRow=" + endRow + ", " + "startCol=" + startCol + ", endCol=" + endCol);
            tabArray = new String[endRow - startRow - 1][endCol - startCol - 1];
            ci = 0;
            for (int i = startRow + 1; i < endRow; i++, ci++) 
            {
                cj = 0;
                for (int j = startCol + 1; j < endCol; j++, cj++) 
                {
                    tabArray[ci][cj] = sheet.getCell(j, i).getContents();
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println("File not found exception");
        }
        return (tabArray);
    }
    // following is helper function to find cell in given sheet for given text / table name identifier
    // Output - Return cells , starting and end cell to define boundary for the data lookup
    public static Cell[] fndCell1(final Sheet shet, final String text) 
    {
        String pos = "start";
        boolean found = false;
        Cell[] cells = new Cell[2];

        for (int i = 0; (i < shet.getRows()) && !found; i++) 
        {
            Cell[] row = shet.getRow(i);

            for (int j = 0; (j < row.length) && !found; j++) 
            {
                if (text.equals(row[j].getContents())) 
                {
                    if (pos.equalsIgnoreCase("start")) 
                    {
                        cells[0] = row[j];
                        pos = "end";
                    } 
                    else 
                    {
                        cells[1] = row[j];
                        found = true;
                    }
                }

            }
        }
        return cells;
    }
}