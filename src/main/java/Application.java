import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

public class Application extends javafx.application.Application {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));

        primaryStage.setTitle("Fines");
        primaryStage.setScene(new Scene(root));
        //primaryStage.setWidth(1280);
        //primaryStage.setHeight(800);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
