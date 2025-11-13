package org.third.li.office.excel.etl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;

public class ExcelApp {

    public static void printCreateDdlString(String filePath) throws IOException, InvalidFormatException {
        File file = new File(filePath);
        TableDdlResolver ddlResolver = TableDdlCreateResolverFactory.getResolver(file.getName());
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file)){
            for (Sheet sheet : xssfWorkbook) {

                GenericTableSource excelTableSource = new ExcelTableSource(sheet);
                excelTableSource.setHeaderStartNumber(1);
                excelTableSource.refresh();

                TableMetaData tableMetaData  =  new TableDdlCreateMetaDataResolver().toTableMetaData(excelTableSource);
                System.out.println();
                System.out.println(ddlResolver.toString(tableMetaData));
            }
        }
    }


    public static void printInsertDmlStringExcel(String filePath) throws IOException, InvalidFormatException {
        File file = new File(filePath);
        var fileName = file.getName();
        TableDmlResolver tableDmlResolver = TableDmlInsertResolverFactory.getResolver(fileName);
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file)){
            for (Sheet sheet : xssfWorkbook) {

                GenericTableSource excelTableSource = new ExcelTableSource(sheet);
                excelTableSource.setHeaderStartNumber(1);
                excelTableSource.refresh();

                TableMetaData tableMetaData = TableDmlInsertMetaDataResolverFactory.getResolver(fileName).toTableMetaData(excelTableSource);
                System.out.println();
                System.out.println(tableDmlResolver.toString(tableMetaData));
            }
        }
    }

    public static void printInsertDmlStringHtml(String filePath){

        File file = new File(filePath);
        var fileName = file.getName();

        GenericTableSource excelTableSource = new HtmlTableSource(file);
        excelTableSource.setHeaderStartNumber(0);
        excelTableSource.refresh();

        TableDmlResolver tableDmlResolver = TableDmlInsertResolverFactory.getResolver(file.getName());
        TableMetaData tableMetaData = TableDmlInsertMetaDataResolverFactory.getResolver(fileName).toTableMetaData(excelTableSource);
        System.out.println();
        System.out.println(tableDmlResolver.toString(tableMetaData));
    }

    public static void printInsertDmlString(String filePath) throws IOException, InvalidFormatException {
        if(filePath.endsWith("xlsx")){
            printInsertDmlStringExcel(filePath);
        } else if (filePath.endsWith("html")) {
            printInsertDmlStringHtml(filePath);
        }else {
            throw new RuntimeException(" can not support file. path :"+filePath);
        }
    }


    public static void main(String[] args) throws IOException, InvalidFormatException {
//        File file = new File("C:\\Users\\yineng\\Desktop\\DWD_DDL_党建活动.xlsx");
//        printCreateDdlString("C:\\Users\\yineng\\Desktop\\ZYGDZ01 党建基础数据类.xlsx");
        printInsertDmlString("C:\\Users\\yineng\\Desktop\\ZYGDZ01 党建基础数据类.xlsx");
//        printInsertDmlString("C:\\Users\\yineng\\Desktop\\DWD.xlsx");
    }

}
