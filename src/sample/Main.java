package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    static Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        currentStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
        currentStage.setTitle("Login");
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
