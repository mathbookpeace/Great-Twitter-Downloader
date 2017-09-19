import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


/**
 * Created by mathbookpeace on 2017/9/13.
 */
public class TwitterDownloaderMain extends Application
{
	public static void main (String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		GreatTwitterDownloader greatTwitterDownloader = new GreatTwitterDownloader();


		primaryStage.setTitle("Great Twitter Downloader !");

		SimpleDoubleProperty simpleDoubleProperty = greatTwitterDownloader.GetProgressProperty();

		ProgressBar progressBar = new ProgressBar();
		progressBar.progressProperty().bind(simpleDoubleProperty);
		progressBar.setLayoutX(1);
		progressBar.setLayoutY(29);
		progressBar.setPrefWidth(469);

		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.progressProperty().bind(simpleDoubleProperty);
		progressIndicator.setLayoutX(484);
		progressIndicator.setLayoutY(3);

		TextField urlTextField = new TextField();
		urlTextField.setLayoutX(1);
		urlTextField.setLayoutY(1);
		urlTextField.setPrefWidth(400);

		Button downloadButton = new Button();
		downloadButton.setLayoutX(402);
		downloadButton.setLayoutY(1);
		downloadButton.setPrefWidth(68);
		downloadButton.setText("Start");
		downloadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String searchKeyword = urlTextField.getText();
				if(!searchKeyword.isEmpty())
				{
					urlTextField.setText("");
					greatTwitterDownloader.DownloaderWithKeyword(searchKeyword);
				}
			}
		});


		Pane rootPane = new Pane();
		rootPane.getChildren().add(downloadButton);
		rootPane.getChildren().add(urlTextField);
		rootPane.getChildren().add(progressBar);
		rootPane.getChildren().add(progressIndicator);


		primaryStage.setScene(new Scene(rootPane, 524, 49));
		primaryStage.setResizable(false);
		primaryStage.show();
	}
}
