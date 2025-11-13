package org.third.li.office.excel.etl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HtmlTableSource implements GenericTableSource{

    private String name;
    private List<String> headers;
    private List<List<String>> body;
    private int headerStartNumber;
    private Document source;

    public HtmlTableSource(File inputFile) {
        Document doc;
        try {
            doc = Jsoup.parse(inputFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.source = doc;
        this.name = inputFile.getName();
    }

    @Override
    public void refresh() {
        Elements elements = source.select("table");
        if(elements.size() != 1){
            throw new RuntimeException("only support one table.");
        }

        headers = new ArrayList<>();
        body = new ArrayList<>();

        Element table = elements.get(0);
        Elements rows = table.select("tr");
        Element header  = rows.get(headerStartNumber);
        Elements headerCells = header.select("td");
        for (Element cell : headerCells) {
            headers.add(cell.text());
        }
        for(var i= getBodyStartNumber() ;i<rows.size();i++){
            var rowCells = new ArrayList<String>();
            var cells = rows.get(i);
            Elements bodyCells = cells.select("td");
            for (Element bodyCell : bodyCells) {
                var v = bodyCell.text();
                if(StringUtils.isEmpty(v)){
                    rowCells.add(null);
                }else{
                    rowCells.add(v);
                }
            }
            body.add(rowCells);
        }
    }

    @Override
    public int getHeaderStartNumber() {
        return headerStartNumber;
    }

    @Override
    public void setHeaderStartNumber(int headerStartNumber) {
        this.headerStartNumber = headerStartNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public List<List<String>> getBody() {
        return body;
    }
}
