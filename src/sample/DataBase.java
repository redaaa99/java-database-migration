package sample;

import com.mysql.cj.jdbc.*;
import org.postgresql.*;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import oracle.jdbc.driver.OracleDriver;
import org.sqlite.JDBC;

import java.sql.*;

public class DataBase {
    Connection con;
    private String  driver;
    private String  url;
    private String  user;
    private String  password;
    private String  name;

    public Connection initConnection() throws ClassNotFoundException,SQLException{
        String dbDriver;
        switch (driver)
        {
            case "MySQL":
                dbDriver="com.mysql.cj.jdbc.Driver";
                break;
            case "PostgreSQL":
                dbDriver="org.postgresql.Driver";
                break;
            case "Oracle":
                dbDriver="oracle.jdbc.driver.OracleDriver";
                break;
            case "SQL MS Server":
                dbDriver="com.microsoft.sqlserver.jdbc.SQLServerDriver";
                break;
            case "Sqlite":
                dbDriver="org.sqlite.JDBC";
                break;
            default:
                dbDriver="com.mysql.cj.jdbc.Driver";
                break;
        }
        Class.forName(dbDriver);
        System.out.println("jdbc:"+url+"/"+name+"?useSSL=false");
        con= DriverManager.getConnection(
                "jdbc:"+url+"/"+name+"?useSSL=false",user,password);
        System.out.println("Connecté :D");
        return con;
    }


    public void closeConnection() throws SQLException
    {
        con.close();
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
