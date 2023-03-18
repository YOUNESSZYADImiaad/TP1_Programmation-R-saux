package blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientJavaFX extends Application{
	PrintWriter pw;
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		primaryStage.setTitle("Chat Client");
		BorderPane borderPane = new BorderPane();
		Label labelhost = new Label("HOST :");
		TextField textFieldHost = new TextField("LocalHost");
		Label labelport = new Label("Port :");
		TextField textFieldPort = new TextField("1234");
		Button btnConnecter = new Button("Connect");
		
		HBox hbox = new HBox();
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(10));
		hbox.setBackground (new Background (new BackgroundFill (Color.ORANGE, null, null) )) ;
		hbox.getChildren().addAll(labelhost,textFieldHost,labelport,textFieldPort,btnConnecter);
		borderPane.setTop(hbox);
		
		VBox vbox2 = new VBox();
		vbox2.setSpacing(10);
		vbox2.setPadding(new Insets(10));
		ObservableList<String> listmodel = FXCollections.observableArrayList();
		ListView<String> listview = new ListView<String>(listmodel);
		vbox2.getChildren().add(listview);
		borderPane.setCenter(vbox2);
		
		Label message = new Label("Message :");
		TextField textFieldmessage = new TextField();
		textFieldmessage.setPrefSize(400, 30);
		Button btnEnvoyer = new Button("Send");
		
		HBox hbox2 = new HBox();
		hbox2.setSpacing(10);
		hbox2.setPadding(new Insets(10));
		hbox2.getChildren().addAll(message,textFieldmessage,btnEnvoyer);
		borderPane.setBottom(hbox2);
		
		Scene scene = new Scene(borderPane,800,600);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		btnConnecter.setOnAction((evt)-> {
			String host = textFieldHost.getText();
			int port = Integer.parseInt(textFieldPort.getText());
			try {
				Socket socket = new Socket(host,port);
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				pw = new PrintWriter(socket.getOutputStream(),true);
				new Thread(()-> {
					while(true) {
							
								try {
								String response=br.readLine();
								Platform.runLater(()->{
								listmodel.add(response);
								});
								} catch (IOException e) {
									e.printStackTrace();
								}
							
						
					}
				}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		});
		
		
		btnEnvoyer.setOnAction((evt)-> {
			String msg = textFieldmessage.getText();
			pw.println(msg);
			
		});
		
	}

		
}
