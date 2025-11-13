package org.third.li.office.excel.etl;

import java.io.Serializable;
import java.util.function.Function;

public interface TableResolver {

    class MetaDataColumn {
        private String column;
        private String dbColumn;

        public MetaDataColumn(String column, String dbColumn) {
            this.column = column;
            this.dbColumn = dbColumn;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getDbColumn() {
            return dbColumn;
        }

        public void setDbColumn(String dbColumn) {
            this.dbColumn = dbColumn;
        }
    }

    class FunctionMetaDataColumn<T extends MetaData>{

        private MetaDataColumn metaDataColumn;

        private Function<T,? extends Serializable> function;

        public FunctionMetaDataColumn(MetaDataColumn metaDataColumn,Function<T,? extends Serializable> function) {
            this.metaDataColumn = metaDataColumn;
            this.function = function;
        }

        public MetaDataColumn getMetaDataColumn() {
            return metaDataColumn;
        }

        public void setMetaDataColumn(MetaDataColumn metaDataColumn) {
            this.metaDataColumn = metaDataColumn;
        }

        public Function<T, ? extends Serializable> getFunction() {
            return function;
        }

        public void setFunction(Function<T, String> function) {
            this.function = function;
        }
    }

}
