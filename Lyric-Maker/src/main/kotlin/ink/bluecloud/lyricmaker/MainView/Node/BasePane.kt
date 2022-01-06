package ink.bluecloud.lyricmaker.MainView.Node

import CloudNodes.CloudButton.CloudButton
import CloudNodes.CloudButton.Data.ButtonProperty
import CloudNodes.CloudLinearProgressBar.CloudLinearProgressBar
import CloudNodes.CloudLinearProgressBar.Property.Theme.LinerBarProgressDefaultTheme
import ink.bluecloud.lyricmaker.MainView.MainView
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration

class BasePane(
    iconImage: Image,
    con: FileChooser,
    player: MediaPlayer,
    primaryStage: Stage
):HBox() {
    /**
     * 定义偏移量，用于处理窗口移动
     */
    private var xOffset = 0.0
    private var yOffset = 0.0

    init {
        children.add(BorderPane().apply {
            //title bar
            top = BorderPane().apply {
                left = HBox(10.0).apply {
                    children.addAll(ImageView(iconImage).apply {
                        fitWidth = 25.0
                        fitHeight = 25.0
                    }, Label("Lrc歌词制作器").apply { style = "-fx-text-fill: #878787;-fx-font-size: 20" })
                }
                right = HBox(10.0).apply {
                    children.run {
                        add(CloudButton(text = "\uE65A", widths = 30.0, heights = 30.0, property = ButtonProperty().apply { textFont.set(
                            MainView.font
                        ) }).apply {
                            setOnMouseClicked {
                                primaryStage.isIconified = true
                            }
                        })
                        add(CloudButton(text = "\uE635", widths = 30.0, heights = 30.0, property = ButtonProperty().apply { textFont.set(
                            MainView.font
                        ) }).apply {
                            setOnMouseClicked {
                                Platform.exit()
                            }
                            padding = Insets(0.0, 0.0, 5.0, 0.0)
                        })
                    }
                }
                regDrag(primaryStage)
            }

            //listview
            center = TimeMonitor(con.lyric.get(), player)

            //button box
            right = Controller(
                ControlArgs(
                    monitor = center as TimeMonitor,
                    musicFile = con.music.get(),
                    lrcFile = con.lyric.get(),
                    player = player,
                    stage = primaryStage
                )
            )

            //progress bar
            bottom = VBox().apply {
                children.run {
                    add(Label().apply {
                        style = "-fx-text-fill: #989898;" +
                                "-fx-font-size: 16;" +
                                "-fx-background-color: white"
                        text = "${getTime(player.currentTime)} / ${getTime(player.cycleDuration)}"
                    })
                    add(CloudLinearProgressBar(width = 880.0, height = 7.0, theme = LinerBarProgressDefaultTheme().apply {
                        backgroundBarColor = Color.web("#EDD2F3")
                        actionBarColor = Color.web("#84DFFF")
                    }))
                }

                alignment = Pos.CENTER_RIGHT
            }

            style = "-fx-background-color: white;-fx-background-radius: 10"
            padding = Insets(10.0, 10.0, 0.0, 10.0)
            effect = DropShadow(20.0, Color.rgb(0, 0, 0, 0.3)).apply { blurType = BlurType.GAUSSIAN }
        })
        padding = Insets(10.0, 10.0, 10.0, 10.0)
        style = "-fx-background-color: transparent"
    }

    private fun regDrag(primaryStage: Stage) {
        setOnMousePressed { event ->
            xOffset = event.sceneX
            yOffset = event.sceneY
        }
        setOnMouseDragged { event ->
            primaryStage.x = event.screenX - xOffset
            primaryStage.y = event.screenY - yOffset
        }
    }

    private fun getTime(duration: Duration) : String {
        return "[${//minute
            duration.toMinutes().toInt().let {
                if (it < 10) {
                    "0$it"
                } else {
                    it
                }
            }
        }:${//second
            (duration.toSeconds() % 60).toInt().let {
                if (it < 10) {
                    "0$it"
                } else {
                    it
                }
            }
        }.${//mills 
            ((duration.toMillis() - ((duration.toMinutes().toInt() * 60000) + ((duration.toSeconds() % 60).toInt() * 1000))) / 10).toInt().let {
                if (it < 10) {
                    "0${it}"
                }else {
                    it
                }
            }
        }]"
    }

}