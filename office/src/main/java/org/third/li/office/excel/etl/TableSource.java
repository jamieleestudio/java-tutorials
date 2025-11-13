package org.third.li.office.excel.etl;

import java.util.List;

public interface TableSource {

    String getName();

    List<String> getHeaders();

    List<List<String>> getBody();


}
