package sample;
import com.sun.javafx.scene.control.skin.LabeledText;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class Controller
{
    public Client client;
    public String activeChatId;
    public boolean groupActiveChat;

    public Button btnSignIn;
    public TextField txtUsername;
    public PasswordField txtPassword;
    public TextField txtServerPort;
    public TextField txtServerIP;
    public Stage myStage;
    public Label lblLoggedIn;
    public Button btnExit;

    public Controller()
    {

    }

    public void btnSignInAction(ActionEvent actionEvent)
    {
        client = new Client(txtServerIP.getText(), Integer.valueOf(txtServerPort.getText()), txtUsername.getText(), txtPassword.getText());
        client.sendLoginQuery();
        client.controller = this;
        Controller2.client = client;
        client.start();
    }

    @FXML
    public void showMainForm()
    {
        try
        {
//            Platform.setImplicitExit(false);
            System.out.println("showMainForm!");
            lblLoggedIn.setVisible(true);
            btnExit.setVisible(true);
//            txtServerIP.getScene().getWindow().hide();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void paneKeyPressed(Event _event)
    {

    }

    public void btnExitAction(ActionEvent actionEvent) throws IOException
    {
        activeChatId = "";
        groupActiveChat = false;
        Stage stage = (Stage)(btnSignIn.getScene().getWindow());
        Parent root = FXMLLoader.load(getClass().getResource("MainForm.fxml"));
        Scene scene = new Scene(root);
        myStage = new Stage();
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        Controller2.client.sendFileMessage(false, Controller2.activeChatId, file);
                        System.out.println(filePath);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        myStage.setScene(scene);
        myStage.setTitle("Messenger (" + txtUsername.getText() + ")");
        myStage.show();
        stage.hide();
        myStage.setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        Controller2.client.logOut();
                        System.exit(0);
                    }
                });
            }
        });
    }

    public void btnRegisterAction(ActionEvent actionEvent)
    {
        client = new Client(txtServerIP.getText(), Integer.valueOf(txtServerPort.getText()), txtUsername.getText(), txtPassword.getText());
        client.sendRegisterQuery();
        client.controller = this;
        Controller2.client = client;
        client.start();
    }
}
