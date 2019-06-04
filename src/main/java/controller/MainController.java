package controller;

import constants.ColorsConvertationConstants;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController {

    private final static String COLOR_GRAY = "COLOR_RGB2GRAY";
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
    @FXML
    private Button btnBuildChart;
    @FXML
    private Slider backProjectionSlider;
    @FXML
    private ToggleButton toggleBackProjection;
    @FXML
    private Slider betaSlider;
    @FXML
    private Slider gammaSlider;
    @FXML
    private Slider cannyDetectorSlider;
    @FXML
    private Button btnHoughLine;
    private FileChooser fileChooser = new FileChooser();
    private Mat leftMatrix;
    private Mat rightMatrix;
    private Mat tempMatrix;
    private Mat hue = new Mat();
    private int bins = 25;
    private double contrast = 2;
    private double beta = -0.5;
    private double gamma = 1.5;
    private static final int RATIO = 3;
    private static final int KERNEL_SIZE = 3;
    private static final Size BLUR_SIZE = new Size(3,3);
    private Mat srcBlur = new Mat();
    private Mat detectedEdges = new Mat();

    @FXML
    void initialize() {

        spField.setText(String.valueOf(20));
        srField.setText(String.valueOf(40));
        epsionField.setText(String.valueOf(0.0001));
        maxCountField.setText(String.valueOf(50));
        maxLvlField.setText(String.valueOf(2));
        btnCancel.setOnAction(event -> {
            tempMatrix = null;
            loadRightImage(leftMatrix);
        });

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

        contrastSlider.setMin(-1.5);
        contrastSlider.setMax(3);
        contrastSlider.setBlockIncrement(1.5);
        contrastSlider.setShowTickMarks(true);
        contrastSlider.setShowTickLabels(true);
        contrastSlider.setOnMouseDragged(event -> {
            contrast = contrastSlider.getValue();
            contrast(contrastSlider.getValue(), beta, gamma);
        });

        betaSlider.setMin(-20);
        betaSlider.setMax(20);
        betaSlider.setBlockIncrement(0.1);
        betaSlider.setShowTickMarks(true);
        betaSlider.setShowTickLabels(true);
        betaSlider.setOnMouseDragged(event -> {
            beta = betaSlider.getValue();
            contrast(contrast, betaSlider.getValue(), gamma);
        });

        gammaSlider.setMin(-100);
        gammaSlider.setMax(100);
        gammaSlider.setBlockIncrement(1);
        gammaSlider.setShowTickMarks(true);
        gammaSlider.setShowTickLabels(true);
        gammaSlider.setOnMouseDragged(event -> {
            gamma = gammaSlider.getValue();
            contrast(contrast, beta, gammaSlider.getValue());
        });

        btnBuildChart.setOnAction(event -> {

            List<MatOfByte> listMatrix = dataHistogramm();

            byte[] r_image_b = listMatrix.get(0).toArray();
            byte[] g_image_b = listMatrix.get(1).toArray();
            byte[] b_image_b = listMatrix.get(2).toArray();

            List<Coord3d> coord3ds = new ArrayList<>();
            for (int i = 0; ; i++) {
                coord3ds.add(new Coord3d(
                        i < r_image_b.length ? r_image_b[i] : 0,
                        i < g_image_b.length ? g_image_b[i] : 0,
                        i < b_image_b.length ? b_image_b[i] : 0
                ));
                if (i >= r_image_b.length && i >= g_image_b.length && i >= b_image_b.length)
                    break;
            }

            createForm(coord3ds);
        });

        backProjectionSlider.setBlockIncrement(5);
        backProjectionSlider.setShowTickLabels(true);
        backProjectionSlider.setShowTickMarks(true);
        backProjectionSlider.setMin(0);
        backProjectionSlider.setMax(200);
        backProjectionSlider.setOnMouseDragged(event -> {
            bins = (int) backProjectionSlider.getValue();
            backProjUpdate();
        });

        toggleBackProjection.setOnAction(event -> {
            if (toggleBackProjection.isSelected()) {
                backProjection();
            }
        });

        cannyDetectorSlider.setMin(0);
        cannyDetectorSlider.setMax(200);
        cannyDetectorSlider.setShowTickMarks(true);
        cannyDetectorSlider.setShowTickLabels(true);
        cannyDetectorSlider.setBlockIncrement(1);
        cannyDetectorSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (tempMatrix == null) {
                tempMatrix = rightMatrix;
            }
            cannyUpdate(newValue.intValue());
        });

        btnHoughLine.setOnAction(event -> drawHoughLine());
    }

    void createForm(List<Coord3d> coord3ds) {
        JavaFXChartFactory factory = new JavaFXChartFactory();
        AWTChart chart = getDemoChart(factory, "offscreen", coord3ds);
        ImageView imageView = factory.bindImageView(chart);

        Stage stage = new Stage();
        // JavaFX
        StackPane pane = new StackPane();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        pane.getChildren().add(imageView);

        factory.addSceneSizeChangedListener(chart, scene);

        stage.setWidth(500);
        stage.setHeight(500);
    }

    private AWTChart getDemoChart(JavaFXChartFactory factory, String toolkit, List<Coord3d> coords) {
        // -------------------------------
        // Define a function to plot
        Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
                return Math.log(x * x + y * y);
            }
        };

        final Shape surface = Builder.buildDelaunay(coords);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .8f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        // -------------------------------
        // Create a chart
        Quality quality = Quality.Advanced;
        //quality.setSmoothPolygon(true);
        //quality.setAnimated(true);

        // let factory bind mouse and keyboard controllers to JavaFX node
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        chart.getScene().getGraph().add(surface);
        return chart;
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
        Imgproc.boxFilter(rightMatrix, dst, CvType.CV_64FC4, new Size(3, 3), new Point(-1, -1));
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

    private void mineShift(double sp, double sr, int maxLevel, int maxCount, double epsion) {
        Mat dst = new Mat();
        //sr - http://qaru.site/questions/13056581/what-does-the-color-window-radius-in-pyrmeanshiftfiltering-mean
        //epsion - http://qaru.site/questions/607604/how-does-cvtermcriteria-work-in-opencv
        Imgproc.pyrMeanShiftFiltering(rightMatrix, dst, sp, sr, maxLevel, new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, maxCount, epsion));
        loadRightImage(dst);
    }

    private void contrast(double value, double beta, double gamma) {
        //gaussianBlur();
        Mat destination = new Mat(leftMatrix.rows(), leftMatrix.cols(), leftMatrix.type());
        Core.addWeighted(leftMatrix, value, destination, beta, gamma, destination);
        loadRightImage(destination);
    }

    private void splitedHSV(Mat hsv) {
        List<Mat> splitHsv = new ArrayList<>();
        Core.split(hsv, splitHsv);

    }

    private List<Mat> separateColors() {
        List<Mat> listColors = new ArrayList<>();
        Core.split(rightMatrix, listColors);
        return listColors;
    }

    private List<MatOfByte> dataHistogramm() {
        List<Mat> colors = separateColors();
        Mat r_hist = new Mat(), g_hist = new Mat(), b_hist = new Mat();
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(256);

        Imgproc.calcHist(colors, new MatOfInt(2), new Mat(), r_hist, histSize, ranges);
        Imgproc.calcHist(colors, new MatOfInt(1), new Mat(), g_hist, histSize, ranges);
        Imgproc.calcHist(colors, new MatOfInt(0), new Mat(), b_hist, histSize, ranges);

        MatOfByte mobr = new MatOfByte();
        Imgcodecs.imencode(".jpg", r_hist, mobr);

        MatOfByte mobg = new MatOfByte();
        Imgcodecs.imencode(".jpg", g_hist, mobg);

        MatOfByte mobb = new MatOfByte();
        Imgcodecs.imencode(".jpg", b_hist, mobb);

        Core.normalize(b_hist, b_hist, 0, rightMatrix.width());
        Core.normalize(r_hist, r_hist, 0, rightMatrix.width());
        Core.normalize(g_hist, g_hist, 0, rightMatrix.width());
        return Arrays.asList(mobr, mobg, mobb);
    }

    private void backProjection() {
        Mat hsv = new Mat();
        Imgproc.cvtColor(rightMatrix, hsv, Imgproc.COLOR_BGR2HSV);
        hue = new Mat(hsv.size(), hsv.depth());
        Core.mixChannels(Arrays.asList(hsv), Arrays.asList(hue), new MatOfInt(0, 0));
    }

    private void backProjUpdate() {
        if (!toggleBackProjection.isSelected()) {
            return;
        }
        int histSize = Math.max(bins, 2);
        float[] hueRange = {0, 180};
        Mat hist = new Mat();
        List<Mat> hueList = Arrays.asList(hue);
        Imgproc.calcHist(hueList, new MatOfInt(0), new Mat(), hist, new MatOfInt(histSize), new MatOfFloat(hueRange), false);
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
        Mat backproj = new Mat();
        Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, backproj, new MatOfFloat(hueRange), 1);
        loadRightImage(backproj);
    }

    private void cannyUpdate(int value) {
        Imgproc.blur(tempMatrix, srcBlur, BLUR_SIZE);
        Mat dst = new Mat();
        Imgproc.cvtColor(srcBlur, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(dst, detectedEdges, value, value * RATIO, KERNEL_SIZE, false);
        dst = new Mat(tempMatrix.size(), CvType.CV_8UC3, Scalar.all(0));
        tempMatrix.copyTo(dst, detectedEdges);
        loadRightImage(dst);
    }

    private void drawHoughLine() {
        Mat dst = rightMatrix;
        //Imgproc.cvtColor(dst, cdstP, Imgproc.COLOR_GRAY2BGR);
        // Probabilistic Line Transform
        Mat grey = new Mat();
        Imgproc.cvtColor(dst, grey, Imgproc.COLOR_RGB2GRAY);
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(grey, linesP, 1, Math.PI/180, 50, 50, 10); // runs the actual detection
        // Draw the lines
        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Imgproc.line(dst, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
        loadRightImage(dst);
    }
}
