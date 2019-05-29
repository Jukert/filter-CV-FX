package controller;

import constants.ColorsConvertationConstants;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController {

    private final String filepath = "D:\\workspaces\\test\\testCVproject\\src\\main\\resources\\lep3.jpg";

    @FXML
    private ImageView beforeImage;

    @FXML
    private ImageView afterImage;

    @FXML
    private Slider thresholdSlider;

    @FXML
    private Slider blurSlider;

    @FXML
    private Button btnSelectFile;

    @FXML
    private Label lblPath;

    @FXML
    private AnchorPane mainFrame;

    @FXML
    private ComboBox<String> colorsConverter;

    @FXML
    private Button btnCancel;

    @FXML
    private Slider gausBlurSlider;

    @FXML
    private ToggleButton toggleGray;

    @FXML
    private Button btnMineShift;

    @FXML
    private Slider contrastSlider;

    @FXML
    private TextField srField;

    @FXML
    private TextField spField;

    @FXML
    private TextField epsionField;

    @FXML
    private TextField maxLvlField;

    @FXML
    private TextField maxCountField;

    private FileChooser fileChooser = new FileChooser();

    private Mat leftMatrix;

    private Mat rightMatrix;

    private Mat tempMatrix;

    private final static String COLOR_GRAY = "COLOR_RGB2GRAY";

    @FXML
    void initialize() {
        spField.setText(String.valueOf(30));
        srField.setText(String.valueOf(40));
        epsionField.setText(String.valueOf(0.0001));
        maxCountField.setText(String.valueOf(50));
        maxLvlField.setText(String.valueOf(2));
        btnCancel.setOnAction(event -> loadRightImage(leftMatrix));

        btnSelectFile.setOnAction(event -> {
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png"));
            fileChooser.setInitialDirectory(new File("D:\\workspaces\\test\\testCVproject\\src\\main\\resources\\images"));
            File selectedFile = fileChooser.showOpenDialog(mainFrame.getScene().getWindow());

            if (selectedFile != null) {
                loadLeftImage(selectedFile.getPath());
                loadRightImage(selectedFile.getPath());
            }
        });

        String[] nameColors = ColorsConvertationConstants.COLORS.keySet().toArray(new String[ColorsConvertationConstants.COLORS.size()]);
        Arrays.sort(nameColors);
        colorsConverter.setItems(FXCollections.observableArrayList(
                nameColors
        ));

        colorsConverter.valueProperty().addListener((observable, oldValue, newValue) -> {
            Mat rgb = rightMatrix;
            Mat n = new Mat();
            Imgproc.cvtColor(rgb, n, ColorsConvertationConstants.COLORS.get(newValue));
            loadRightImage(n);
        });

        blurSlider.setOnMouseDragged(event -> smoothing());
        gausBlurSlider.setOnMouseDragged(event -> gaussianBlur());

        toggleGray.setOnAction(event -> {
            if (toggleGray.isSelected()) {
                Mat rgb = rightMatrix;
                Mat dst = new Mat();
                this.tempMatrix = rightMatrix;
                Imgproc.cvtColor(rgb, dst, ColorsConvertationConstants.COLORS.get(COLOR_GRAY));
                loadRightImage(dst);
            }

            if (!toggleGray.isSelected()) {
                loadRightImage(tempMatrix);
            }
        });

        thresholdSlider.setMin(0);
        thresholdSlider.setMax(500);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setOnMouseDragged(event -> threshold((int) thresholdSlider.getValue()));

        btnMineShift.setOnAction(event -> mineShift(Double.valueOf(
                spField.getText()), Double.valueOf(srField.getText()),
                Integer.valueOf(maxLvlField.getText()), Integer.valueOf(maxCountField.getText()),
                Double.valueOf(epsionField.getText())
        ));

        contrastSlider.setMin(0);
        contrastSlider.setMax(10);
        contrastSlider.setShowTickMarks(true);
        contrastSlider.setShowTickLabels(true);
        contrastSlider.setOnMouseDragged(event -> {
            contrast((int) contrastSlider.getValue());
//            System.out.println("asdads");
        });
    }


    void loadLeftImage(String path) {
        this.leftMatrix = loadImageMatrix(path);
        beforeImage.setImage(new Image(new ByteArrayInputStream(loadImageBytes(leftMatrix))));
    }

    void loadRightImage(String path) {
        this.rightMatrix = loadImageMatrix(path);
        afterImage.setImage(new Image(new ByteArrayInputStream(loadImageBytes(rightMatrix))));
    }

    void loadLeftImage(Mat mat) {
        this.leftMatrix = mat;
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        beforeImage.setImage(new Image(new ByteArrayInputStream(matOfByte.toArray())));
    }

    void loadRightImage(Mat mat) {
        this.rightMatrix = mat;
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        afterImage.setImage(new Image(new ByteArrayInputStream(matOfByte.toArray())));
    }


    private byte[] loadImageBytes(String filepath) {
        Mat leftMatrix = loadImageMatrix(filepath);
        MatOfByte matrixOfB = new MatOfByte();
        Imgcodecs.imencode(".jpg", leftMatrix, matrixOfB);
        return matrixOfB.toArray();
    }

    private byte[] loadImageBytes(Mat matrix) {
        MatOfByte matrixOfB = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, matrixOfB);
        return matrixOfB.toArray();
    }

    private Mat loadImageMatrix(String filepath) {
        return Imgcodecs.imread(filepath);
    }

    private void smoothing() {
        Mat dst = new Mat();
        Imgproc.boxFilter(rightMatrix, dst, CvType.CV_64FC4,new Size(3, 3), new Point(-1, -1));
        loadRightImage(dst);
    }

    private void gaussianBlur() {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(rightMatrix, dst, new Size(3, 3), 10);
        loadRightImage(dst);
    }

    private void threshold(int value) {
        Mat dst = new Mat();
        Imgproc.threshold(rightMatrix, dst, value, 500, Imgproc.THRESH_TRUNC);

        byte[] data1 = new byte[dst.rows() * dst.cols() * (int) (dst.elemSize())];
        dst.get(0, 0, data1);

        BufferedImage bufImage = new BufferedImage(dst.cols(), dst.rows(),
                BufferedImage.TYPE_BYTE_GRAY);

        bufImage.getRaster().setDataElements(0, 0, dst.cols(), dst.rows(), data1);

        afterImage.setImage(SwingFXUtils.toFXImage(bufImage, null));
    }

    private void mineShift(double sp, double sr, int maxLevel,int maxCount, double epsion) {
        Mat dst = new Mat();
        //sr - http://qaru.site/questions/13056581/what-does-the-color-window-radius-in-pyrmeanshiftfiltering-mean
        Imgproc.pyrMeanShiftFiltering(rightMatrix, dst, sp, sr, maxLevel, new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, maxCount, epsion));
        loadRightImage(dst);
    }

    private void contrast(int value) {
        //gaussianBlur();
        Mat destination = new Mat(rightMatrix.rows(), rightMatrix.cols(), rightMatrix.type());
        Core.addWeighted(rightMatrix, 1.5, destination, -0.5, 0, destination);
        loadRightImage(destination);
    }

    private void splitedHSV(Mat hsv) {
        List<Mat> splittedHsv = new ArrayList<>();
        Core.split(hsv, splittedHsv);

    }
}
