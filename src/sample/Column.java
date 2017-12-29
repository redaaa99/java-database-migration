package sample;

public class Column {
    String name;
    String type;
    String nullable;
    String auto_inc;
    String size;

    public Column(String name,String type,String nullable,String auto_inc,String size){
        this.name=name;
        this.auto_inc=auto_inc;
        this.nullable=nullable;
        this.type = type;
        this.size = size;
    }
}
