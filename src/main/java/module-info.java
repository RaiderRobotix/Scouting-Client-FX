module org.raiderrobotix.scouting.client {
	requires kotlin.stdlib;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires java.logging;
	requires static lombok;
	requires blue.alliance.api.java.library;
	requires com.google.gson;
	requires annotations;
	requires commons.math3;
	requires org.raiderrobotix.scouting.models;

	exports org.raiderrobotix.scouting.client.ui;
	opens org.raiderrobotix.scouting.client.ui;
}
