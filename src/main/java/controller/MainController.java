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
import java.util.Arrays;

public class MainController {

    private final String filepath = "D:\\workspaces\\test\\testCVproject\\src\\main\\resources\\lep3.jpg";

    @FXML
    private ImageView beforeImage;

    @FXML
    private ImageView afterImage;

    @FXML
    private Slider threshouldSlider;

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
    private Button btnWaterShared;

    private FileChooser fileChooser = new FileChooser();

    private Mat leftMatrix;

    private Mat rightMatrix;

    private Mat tempMatrix;

    private final static String COLOR_GRAY = "COLOR_RGB2GRAY";

    @FXML
    void initialize() {
        btnCancel.setOnAction(event -> loadRightImage(leftMatrix));

        btnSelectFile.setOnAction(event -> {
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png"));
            fileChooser.setInitialDirectory(new File("D:\\workspaces\\test\\testCVproject\\src\\main\\resources"));
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

        threshouldSlider.setMin(0);
        threshouldSlider.setMax(500);
        threshouldSlider.setShowTickMarks(true);
        threshouldSlider.setOnMouseDragged(event -> threshold((int) threshouldSlider.getValue()));

        btnWaterShared.setOnAction(event -> {
            Mat dst = new Mat();
            Imgproc.pyrMeanShiftFiltering(rightMatrix, dst, 20, 40, 2, new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 50, 0.001));
            loadRightImage(dst);
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

    /*@FXML
    void initialize() {
        Mat matImg1 = loadImg(filepath);
        byte[] imgB1 = loadImg(matImg1);

        Mat matImg2 = loadImg(filepath);




        imgViewer.setImage(new Image(new ByteArrayInputStream(imgB1)));
        imgViewer1.setImage(SwingFXUtils.toFXImage(changeTresh(120, matImg2), null));

        trashSlider.setMin(0);
        trashSlider.setMax(300);

        trashSlider.setShowTickLabels(true);

        trashSlider.setOnMouseDragged(e -> {
            Mat mTemp = changeTreshB((int) trashSlider.getValue(), matImg2);
            MatOfByte mb = new MatOfByte();
            Imgcodecs.imencode(".jpg", mTemp, mb);

        imgViewer1.setImage(SwingFXUtils.toFXImage(changeTresh((int) trashSlider.getValue(), matImg2), null)
                *//*new Image(new ByteArrayInputStream(mb.toArray()))*//*
        );
        });
    }


    BufferedImage changeTresh(int val, Mat m) {
        Mat dst = new Mat();
        Imgproc.threshold(m, dst, val, 500, Imgproc.THRESH_BINARY);

        byte[] data1 = new byte[dst.rows() * dst.cols() * (int) (dst.elemSize())];
        dst.get(0, 0, data1);

        BufferedImage bufImage = new BufferedImage(dst.cols(), dst.rows(),
                BufferedImage.TYPE_BYTE_GRAY);

        bufImage.getRaster().setDataElements(0, 0, dst.cols(), dst.rows(), data1);

        MatOfByte matOfByteDst = new MatOfByte();
        Imgcodecs.imencode(".jpg", dst, matOfByteDst);
        return bufImage;
    }

    Mat changeTreshB(int val, Mat m) {
        Mat dst = new Mat();
        Imgproc.threshold(m, dst, val, 500, Imgproc.THRESH_BINARY);

        byte[] data1 = new byte[dst.rows() * dst.cols() * (int) (dst.elemSize())];
        dst.get(0, 0, data1);

        BufferedImage bufImage = new BufferedImage(dst.cols(), dst.rows(),
                BufferedImage.TYPE_BYTE_GRAY);

        bufImage.getRaster().setDataElements(0, 0, dst.cols(), dst.rows(), data1);

        MatOfByte matOfByteDst = new MatOfByte();
        Imgcodecs.imencode(".jpg", dst, matOfByteDst);
        return dst;
    }

    byte[] loadImgB(String path) {
        return loadImg(loadImg(path));
    }

    byte[] loadImg(Mat matrix) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, matOfByte);
        return matOfByte.toArray();
    }

    Mat loadImg(String path) {
        Mat rgb = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
//        Mat hsv = new Mat();
//        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV);
        return rgb;
    }*/
}
