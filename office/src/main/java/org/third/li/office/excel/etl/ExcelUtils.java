package org.third.li.office.excel.etl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class ExcelUtils {


    public static String getValue(Cell cell){
        if(cell == null){
            return "";
        }
        var cellType = cell.getCellType();
        if(CellType.STRING == cellType){
            return cell.getStringCellValue();
        }else if(CellType.NUMERIC == cellType){
            return (int) cell.getNumericCellValue() +"";
        }
        return "";
    }

}
