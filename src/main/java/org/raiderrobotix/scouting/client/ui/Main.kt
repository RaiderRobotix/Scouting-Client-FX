package org.raiderrobotix.scouting.client.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.util.*
import kotlin.system.exitProcess

/**
 * Entry point to the program
 * Responsible for creating the main scene and initializing the TBA API
 */
class Main : Application() {
	@Throws(Exception::class)
	override fun start(primaryStage: Stage) {
		try {
			val root = FXMLLoader.load<Parent>(
					javaClass
						.getResource("/fxml/main.fxml")!!
			)
			primaryStage.icons.add(Image(javaClass.getResourceAsStream("/img/team_25_logo.png")))
			primaryStage.title = "Raider Robotix Scouting Client"
			primaryStage.scene = Scene(root, 820.0, 400.0)
			primaryStage.isResizable = false
			primaryStage.show()
			primaryStage.onCloseRequest = EventHandler {
				Platform.exit()
				exitProcess(0)
			}
		} catch (np: NullPointerException) {
			np.printStackTrace()
		}
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>)  = launch(Main::class.java, *args)
	}
}
