import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;

import java.time.LocalDate;

/**
 * Created by mathbookpeace on 2017/9/13.
 */
public class TwitterDownloaderMain extends Application
{
//	final static Logger logger = LogManager.getRootLogger();
	private GreatTwitterDownloader greatTwitterDownloader;

	public static void main (String[] args)
	{
//		PropertyConfigurator.configure("log4j.properties");
//		logger.info("Program Start !");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		greatTwitterDownloader = new GreatTwitterDownloader();


		primaryStage.setTitle("Great Twitter Downloader !");

		SimpleDoubleProperty simpleDoubleProperty = new SimpleDoubleProperty();
		greatTwitterDownloader.setSimpleDoubleProperty(simpleDoubleProperty);


		// progress
		ProgressBar progressBar = new ProgressBar();
		progressBar.setPrefSize(420 , 20);
		progressBar.progressProperty().bind(simpleDoubleProperty);

		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefSize(40 , 40);
		progressIndicator.progressProperty().bind(simpleDoubleProperty);


		// date
		DatePicker sinceDatePicker = new DatePicker();
		sinceDatePicker.setPrefSize(210 , 20);
		sinceDatePicker.setValue(LocalDate.now());

		DatePicker untilDatePicker = new DatePicker();
		untilDatePicker.setPrefSize(209 , 20);
		untilDatePicker.setValue(LocalDate.now());


		// keyword
		TextField urlTextField = new TextField();
		urlTextField.setPrefSize(370 , 20);

		Button downloadButton = new Button();
		downloadButton.setPrefSize(49 , 20);
		downloadButton.setText("Start");
		downloadButton.setOnAction(event ->
				{
					String searchKeyword = urlTextField.getText();
					LocalDate sinceDate = sinceDatePicker.getValue();
					LocalDate untilDate = untilDatePicker.getValue();
					if(!searchKeyword.isEmpty())
					{
						urlTextField.setText("");
						greatTwitterDownloader.downloaderWithKeyword(searchKeyword , sinceDate , untilDate);
					}
				}
		);



		HBox hBox1_1 = new HBox();
		hBox1_1.setSpacing(1);
		hBox1_1.getChildren().add(urlTextField);
		hBox1_1.getChildren().add(downloadButton);

		HBox hBox1_2 = new HBox();
		hBox1_2.setSpacing(1);
		hBox1_2.getChildren().add(sinceDatePicker);
		hBox1_2.getChildren().add(untilDatePicker);

		HBox hBox1_3 = new HBox();
		hBox1_3.getChildren().add(progressBar);

		VBox vBox = new VBox();
		vBox.setSpacing(1);
		vBox.getChildren().add(hBox1_1);
		vBox.getChildren().add(hBox1_2);
		vBox.getChildren().add(hBox1_3);

		HBox hBoxRoot = new HBox();
		hBoxRoot.setSpacing(1);
		hBoxRoot.getChildren().add(vBox);
		hBoxRoot.getChildren().add(progressIndicator);


		primaryStage.setScene(new Scene(hBoxRoot));
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@Override
	public void stop()
	{
		System.out.println("Terminate");
		greatTwitterDownloader.dispose();
	}
}
