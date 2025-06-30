package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import gif.Gif;
import gif.data.format.ByteArray;

public class Main extends Application {
  private File promptForFile(Stage stage, String title) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().addAll(
      new ExtensionFilter("*.gif", "*.gif"),
      new ExtensionFilter("All files", "*.*")
    );

    var initialTitle = stage.getTitle();
    stage.setTitle(title);

    var file = fileChooser.showOpenDialog(stage);

    stage.setTitle(initialTitle);

    return file;
  }

  private Optional<ButtonType> showWarningDialog(Stage stage, String warning) {
    var dialog = new Dialog<ButtonType>();
    dialog.setTitle("Warning");
    dialog.setContentText(warning);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE, ButtonType.OK);

    return dialog.showAndWait();
  }

  @Override
  public void start(Stage stage) {
    stage.show();

    while (true) {
      File file = null;
      do {
        file = promptForFile(stage, "Select file to inspect");
        if (file == null) {
          var result = showWarningDialog(stage, "Please select a file to continue");
          if (result.isPresent() && result.get() == ButtonType.CLOSE) {
            Platform.exit();
            return;
          }
        }
      } while (file == null);

      stage.setTitle(file.getName());

      try (var stream = new FileInputStream(file)) {
        try {
          var gif = new Gif(stream);

          System.out.println(gif.screen.width);
          System.out.println(gif.screen.height);

          var output = new ByteArrayOutputStream();
          gif.writeTo(output);
          System.out.println(ByteArray.format(output.toByteArray()));
          System.out.flush();
        } catch (Exception e) {
          e.printStackTrace();

          var result = showWarningDialog(stage, "Could not process file: " + e.getMessage());
          if (result.isPresent() && result.get() == ButtonType.CLOSE) {
            Platform.exit();
            return;
          }

          continue;
        }

        return;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        showWarningDialog(stage, "Could not open file: " + e.getMessage());
      } catch (IOException e) {
        e.printStackTrace();
        return;  // ignore exceptions on stream close
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
