package org.stuartaroth.simplesocketclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class App extends Application {
    private final String MONO_FONT = "PT Mono";

    private final String CLIENT =         "  client  |  message  |  ";
    private final String SERVER_OPEN =    "  server  |  open     |  ";
    private final String SERVER_MESSAGE = "  server  |  message  |  ";
    private final String SERVER_CLOSE =   "  server  |  close    |  ";
    private final String SERVER_ERROR =   "  server  |  error    |  ";

    private class SocketClient extends WebSocketClient {
        public SocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            updateCenter(SERVER_OPEN + handshakedata.getHttpStatusMessage());
        }

        @Override
        public void onMessage(String message) {
            updateCenter(SERVER_MESSAGE + message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            updateCenter(SERVER_CLOSE + reason);
        }

        @Override
        public void onError(Exception ex) {
            updateCenter(SERVER_ERROR + ex.getMessage());
        }
    }

    private SocketClient socketClient;
    private Button connect;
    private TextField url;
    private TextField input;
    private HBox top;
    private VBox center;
    private BorderPane borderPane;
    private ScrollPane scrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    public void updateCenter(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Text text = new Text(message);
                text.setFont(Font.font(MONO_FONT));
                center.getChildren().add(center.getChildren().size() - 1, text);
                scrollPane.setContent(center);
                scrollPane.setFitToWidth(true);
                borderPane.setCenter(scrollPane);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        connect = new Button(" connect");
        connect.setFont(Font.font(MONO_FONT));
        url = new TextField("ws://localhost:8000");
        url.setFont(Font.font(MONO_FONT));
        input = new TextField();
        input.setFont(Font.font(MONO_FONT));

        connect.setOnAction(click -> {
            if (socketClient == null || socketClient.isClosed()) {
                try {
                    socketClient = new SocketClient(new URI(url.getText()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                socketClient.connect();
            }
        });

        input.setOnAction(enter -> {
            if (socketClient != null && socketClient.isOpen()) {
                String clientMessage = input.getText();
                updateCenter(CLIENT + clientMessage);
                socketClient.send(clientMessage);
                input.setText("");
            }
        });

        HBox.setHgrow(url, Priority.ALWAYS);

        top = new HBox();
        top.getChildren().addAll(connect, url);

        center = new VBox(5);
        center.getChildren().add(input);
        scrollPane = new ScrollPane();
        scrollPane.setContent(center);
        scrollPane.setFitToWidth(true);

        borderPane = new BorderPane();
        borderPane.setTop(top);
        borderPane.setCenter(scrollPane);

        Scene scene = new Scene(borderPane, 800,600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
