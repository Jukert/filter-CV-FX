import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

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

/*        Parent rootAdd = FXMLLoader.load(getClass().getResource("/fxml/chart.fxml"));
        Stage s = new Stage();
        s.setScene(new Scene(rootAdd,300, 300));
        s.show();*/
    }
}
