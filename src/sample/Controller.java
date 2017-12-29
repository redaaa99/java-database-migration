package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML ComboBox<String> driver;
    @FXML ComboBox<String> driver1;
    @FXML TextField url,url1,userid,userid1,dbname,dbname1;
    @FXML PasswordField password,password1;


    @FXML
    public void initialize(URL urli, ResourceBundle rb) {
        List<String> list = new ArrayList<>();
        list.add("MySQL");
        list.add("PostgreSQL");
        list.add("Oracle");
        list.add("SQL MS Server");
        list.add("Sqlite");
        ObservableList obList = FXCollections.observableList(list);
        driver.setItems(obList);
        driver1.setItems(obList);
        driver.getSelectionModel().selectFirst();
        driver1.getSelectionModel().selectFirst();

        url.setPromptText("mysql://localhost:3306");
        url.setText("mysql://localhost:3306");
        url1.setPromptText("mysql://localhost:3306");
        url1.setText("mysql://localhost:3306");



        userid.setPromptText("i.e: root");
        userid.setText("root");
        userid1.setPromptText("i.e: root");
        userid.setText("root");


        dbname.setPromptText("i.e: Modele");
        dbname.setText("classicmodels");
        dbname1.setPromptText("i.e: Modele");
        dbname1.setText("migreee");

        password1.setText("redaader99");
        password.setText("redaader99");

        //TEST POSTGRESQL
        driver1.getSelectionModel().select(1);
        url1.setText("postgresql://localhost:5432");
        userid1.setText("postgres");

    }

    @FXML
    private void handleClick(ActionEvent event) {
        String srcDriver = driver.getValue();
        String dstDriver = driver1.getValue();
        String srcUrl = url.getText();
        String dstUrl = url1.getText();
        String srcUser = userid.getText();
        String dstUser = userid1.getText();
        String srcDb = dbname.getText();
        String dstDb = dbname1.getText();
        String srcPass = password.getText();
        String dstPass = password1.getText();
        DataBase src = new DataBase();
        DataBase dst = new DataBase();

        Connection srccon,dstcon;

        src.setDriver(srcDriver);
        src.setName(srcDb);
        src.setPassword(srcPass);
        src.setUrl(srcUrl);
        src.setUser(srcUser);
        try {
            srccon = src.initConnection();
            dst.setDriver(dstDriver);
            dst.setName(dstDb);
            dst.setPassword(dstPass);
            dst.setUrl(dstUrl);
            dst.setUser(dstUser);

            try {
                if( dstDriver.equals(srcDriver) && dstUrl.equals(srcUrl) && dstPass.equals(srcPass) && dstDb.equals(srcDb) && dstUser.equals(srcUser)){
                    throw new Exception();
                }
                dstcon = dst.initConnection();
                //Kolchi mzian hna donc ngolo bsmellah la migration!
                Migrator migrator = new Migrator(srccon,srcDb,dstcon,dstDb,dstDriver);
                migrator.start();
                Dialog alert = new Alert(Alert.AlertType.CONFIRMATION, "DONE !");
                alert.setTitle("INFOS");
                alert.setHeaderText("Reussi!");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    alert.close();
                }
            } catch (ClassNotFoundException e) {
                Dialog alert = new Alert(Alert.AlertType.ERROR, "Erreur de Driver");
                alert.setTitle("DESTINATION");
                alert.setHeaderText("DESTINATION");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    alert.close();
                }
            } catch (SQLException e) {
                Dialog alert = new Alert(Alert.AlertType.ERROR, "Destination Error");
                alert.setTitle("DESTINATION");
                alert.setHeaderText("DESTINATION");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    alert.close();
                }
                e.printStackTrace();
            }catch (Exception e) {
                Dialog alert = new Alert(Alert.AlertType.ERROR, "Meme base de Données changer les infos ou erreur interne");
                alert.setTitle("Erreur");
                alert.setHeaderText("Meme base de Donnees");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    alert.close();
                }
                e.printStackTrace();
            }finally {
                dst.closeConnection();
            }
        }catch (ClassNotFoundException e){
            Dialog alert = new Alert(Alert.AlertType.ERROR, "Erreur de Driver");
            alert.setTitle("SOURCE");
            alert.setHeaderText("SOURCE");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alert.close();
            }
        }catch (SQLException e){
            Dialog alert = new Alert(Alert.AlertType.ERROR, "Source Error");
            alert.setTitle("SOURCE");
            alert.setHeaderText("SOURCE");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alert.close();
            }
        }finally{
            try {
                src.closeConnection();
            }catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClickAbout(ActionEvent event){
        Dialog alert = new Alert(Alert.AlertType.INFORMATION, "Meskali Reda GI2");
        alert.setHeaderText("Author");
        alert.setTitle("Author");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            alert.close();
        }
    }

}
