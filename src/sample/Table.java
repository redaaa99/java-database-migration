package sample;

import java.util.List;

public class Table {
    String name;
    List<Column> columns;
    List<String> primary;
    List<String> foreign;

    public Table(String name,List<Column> columns,List<String> primary,List<String> foreign){
        this.name = name;
        this.columns = columns;
        this.foreign=foreign;
        this.primary=primary;
    }
}
