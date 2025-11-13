package org.third.li.office.excel.etl;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

public class ExcelTableSource implements GenericTableSource {

    private String name;
    private List<String> headers;
    private List<List<String>> body;
    private int headerStartNumber;
    private Sheet source;

    public ExcelTableSource(Sheet sheet){
        this.source = sheet;
    }

    public void refresh(){
        this.name = source.getSheetName();
        headers = new ArrayList<>();
        body = new ArrayList<>();
        Row header = source.getRow(getHeaderStartNumber());
        for (Cell cell : header) {
            this.headers.add(ExcelUtils.getValue(cell));
        }
        int headerSize = headers.size();
        for (Row cells : source) {
            if(cells.getRowNum() < getBodyStartNumber()){
                continue;
            }
            if(!isValidRow(cells)){
                continue;
            }
            var bodyRowList = new ArrayList<String>();
            for(int i=0;i<headerSize;i++){
                Cell cell = cells.getCell(i);
                if(cell == null){
                    bodyRowList.add("");
                }else{
                    bodyRowList.add(ExcelUtils.getValue(cell));
                }
            }
            body.add(bodyRowList);
        }
    }

    private static boolean isValidRow(Row row){
        for(int i=3;i<5;i++){
            Cell cell = row.getCell(i);
            String cellValue = ExcelUtils.getValue(cell);
            if(StringUtils.isNotEmpty(cellValue)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void setHeaderStartNumber(int headerStartNumber) {
        this.headerStartNumber = headerStartNumber;
    }

    @Override
    public int getHeaderStartNumber() {
        return headerStartNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    @Override
    public List<List<String>> getBody() {
        return body;
    }

    public void setBody(List<List<String>> body) {
        this.body = body;
    }



}
