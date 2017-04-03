package br.univel.jshare.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GenerateDialogController {

	Alert alert;
	
	public GenerateDialogController() {
	}
	
	public void generateDialog(AlertType type, String title, String headerText, String contextText){
		alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contextText);
		alert.showAndWait();
	}
}
