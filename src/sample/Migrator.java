package sample;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static sample.Converter.*;

public class Migrator {
        private Connection src;
        private Connection dst;

        private String srcName;
        private String dstName;

        private String dstDriver;
        private List<String> deleteAllKeys= new ArrayList<>();
        private Statement srcStmt;
        private Statement dstStmt;

        private ResultSet srcRs;
        private ResultSet dstRs;

        private DatabaseMetaData databaseMetaData;


        public Migrator(Connection srcdb,String srcName,Connection dstdb,String dstName,String destdriver) throws SQLException
        {
            this.src=srcdb;
            this.dst=dstdb;
            this.srcName=srcName;
            this.dstName=dstName;
            this.dstDriver=destdriver;
            this.srcStmt=src.createStatement();
            this.dstStmt=dst.createStatement();

            //BDA la migration!
            this.databaseMetaData = srcdb.getMetaData();
        }

        public List<Table> getAllTablesFromSource() throws SQLException{
            List<Table> tables  = new ArrayList<>();
            List<String> tablesName = getSrcTables();
            for(String table:tablesName) {
                Table tab = new Table(table,getSrcColumns(table),primaryKeys(table),foreignKeys(table));
                tables.add(tab);
            }
            return tables;
        }

        private void insertIntoDestination(List<Table> tables) {
            for(Table table : tables) {
                String request = "select * from " + table.name;
                String value = "";
                try {
                    srcRs = srcStmt.executeQuery(request);
                    while (srcRs.next()) {
                        String sql3 = "INSERT INTO " + table.name + " VALUES (";
                        for (int i = 1; i <= table.columns.size(); i++) {
                              value = srcRs.getString(i);
                              if(value==null){value="NULL";}
                              if(value!=null && value.equals("null")){value="NULL";}
                              if(value!=null && !value.equals("NULL")){value="'"+clean(value)+"'";}
    //                        value = (srcRs.getString(i) != "null") ? srcRs.getString(i) : "";
    //                        if(value != null){ value = clean(value);}
    //                        if(value != null && value.equals("null")){value="";}
    //                        if(value==null){value="";}
                            if (i < table.columns.size())
                                sql3 += value + ",";
                            else
                                sql3 += value + ");";
                        }
                        System.out.println(sql3);
                        dstStmt.executeUpdate(sql3);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        public String clean( String sourceString )
        {
            StringBuffer target = new StringBuffer();
            int length = sourceString.length();
            char c;
            for( int i=0; i<length; i++ )
            {
                c = sourceString.charAt( i );
                target.append( c );
                if ( c == '\'' )
                {
                    target.append( c ); // add another single quote for escape char
                }
            }
            return target.toString();
        }
        public void start() throws SQLException
        {
            List<Table> tables  = getAllTablesFromSource();
            createAllTables(tables);
            insertIntoDestination(tables);
            deleteAllKeysFromDst();
            addPrimaryKeys(tables);
            addForeignKeys(tables);
        }

        void deleteAllKeysFromDst() throws SQLException
        {
            for(String req : deleteAllKeys)
            {
                dstStmt.executeUpdate(req);
            }
        }

        void addPrimaryKeys(List<Table> tables) throws SQLException
        {
            for(Table table:tables)
            {
                for(String key:table.primary){
                    System.out.println("ALTER TABLE "+table.name+" "+key);
                    dstStmt.executeUpdate("ALTER TABLE "+table.name+" "+key);
                }
            }
        }

        void addForeignKeys(List<Table> tables) throws SQLException
        {
            for(Table table:tables)
            {
                for(String key:table.foreign){
                    System.out.println("ALTER TABLE "+table.name+" "+key);
                    dstStmt.executeUpdate("ALTER TABLE "+table.name+" "+key);
                }
            }
        }

        private List<String> primaryKeys(String table) throws SQLException
        {
            List<String> primaires=new ArrayList<>();
            srcRs = databaseMetaData.getPrimaryKeys(src.getCatalog(), null, table);
            while (srcRs.next()) {
                String pk_nom = srcRs.getString("COLUMN_NAME");
                primaires.add(pk_nom);
            }
            int count =0;
            String keys="";
            for(String key:primaires)
            {
                if(count==primaires.size()-1){
                    keys+=key;
                }else
                {
                    keys+=key+",";
                }

                count++;
            }
            String req = "ADD CONSTRAINT PK_"+table+"_"+primaires.get(0)+" PRIMARY KEY ("+keys+");";
            deleteAllKeys.add("ALTER TABLE "+table+" DROP CONSTRAINT IF EXISTS PK_"+table+"_"+primaires.get(0)+";");
            //System.out.println(req);
            primaires.clear();
            primaires.add(req);
            return primaires;
        }

        //Les clés étrangers de la table
        private List<String> foreignKeys(String table) throws SQLException
        {
            List<String> etrange=new ArrayList<>();
            srcRs = databaseMetaData.getImportedKeys(src.getCatalog(), null, table);
            while (srcRs.next()) {
                String pkTable = srcRs.getString("PKTABLE_NAME");
                String pkColName = srcRs.getString("PKCOLUMN_NAME");
                String fkTable = srcRs.getString("FKTABLE_NAME");
                String fkColName = srcRs.getString("FKCOLUMN_NAME");
                String req = "ADD CONSTRAINT FK_"+fkTable+"_"+fkColName+" FOREIGN KEY ("+fkColName+") REFERENCES "+pkTable+" ("+pkColName+");";
                deleteAllKeys.add("ALTER TABLE "+fkTable+" DROP CONSTRAINT IF EXISTS FK_"+fkTable+"_"+fkColName+";");
                //System.out.println(req);
                etrange.add(req);
            }
            return etrange;
        }

        private void createAllTables(List<Table> liste)throws  SQLException
        {
            for(Table table : liste) {
                dstStmt = dst.createStatement();
                StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                sql.append(table.name + " (");
                int count = 0;
                for (Column col : table.columns) {
                    if (count == table.columns.size() - 1) {
                        sql.append(col.name + " " + col.type + "" + col.size + " " + col.nullable);
                        break;
                    }
                    count++;
                    sql.append(col.name + " " + col.type + "" + col.size + " " + col.nullable + ",");
                }
                sql.append(");");
                String req = sql.toString();
                //System.out.println(req);
                dstStmt.executeUpdate(req);
            }

        }

        private String formatType(String type,String size)
        {
            switch (dstDriver)
            {
                case "MySQL":
                    return toMysqlType(type,size);
                case "PostgreSQL":
                    return toPostgresqlType(type,size);
                case "Oracle":
                    return toOracleType(type,size);
                case "SQL MS Server":
                    return toServerType(type,size);
                case "Sqlite":
                    return toSqliteType(type,size);
            }
            return "VARCHAR";
        }

        private List<String> getSrcTables() throws SQLException{
            srcRs = databaseMetaData.getTables(src.getCatalog(),null,"%",null);
            List<String> tables= new ArrayList<>();
            while(srcRs.next())
            {
                tables.add(srcRs.getString("TABLE_NAME"));
            }
            return tables;
        }

        private List<Column> getSrcColumns(String tableName) throws SQLException
        {
            List<Column> columns= new ArrayList<>();
            srcRs = databaseMetaData.getColumns(src.getCatalog(), null, tableName, "%");
            while(srcRs.next()){

                String name,type,nullable,autoinc,size,data_type_name,precision;
                name = srcRs.getString("COLUMN_NAME");
                type = JDBCType.valueOf(srcRs.getInt("DATA_TYPE")).toString();
                size = srcRs.getString("COLUMN_SIZE");
                nullable = srcRs.getString("NULLABLE");
                autoinc = srcRs.getString("IS_AUTOINCREMENT");
                //Hadchi khsso i testa pour chaque database
                type = formatType(type,size);
                if(Integer.valueOf(size)>4000){size="4000";}
                if(type.contains("VARCHAR")){size="("+size+")";}else{size="";}
                if(nullable.equals("0")){nullable="NOT NULL";}else{nullable="";}
                //System.out.println(tableName+" "+name+" "+type+" "+size+" "+nullable+" "+autoinc);
                Column col = new Column(name,type,nullable,autoinc,size);
                columns.add(col);
            }
            return columns;
        }
}
