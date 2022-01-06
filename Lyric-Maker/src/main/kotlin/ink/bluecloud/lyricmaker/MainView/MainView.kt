package ink.bluecloud.lyricmaker.MainView


import CloudNodes.CloudLinearProgressBar.CloudLinearProgressBar
import animatefx.animation.ZoomIn
import ink.bluecloud.lyricmaker.Data.Config
import ink.bluecloud.lyricmaker.MainView.Node.BasePane
import ink.bluecloud.lyricmaker.MainView.Node.FileChooser
import ink.bluecloud.lyricmaker.MainView.Node.TimeMonitor
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.nio.file.Files

class MainView :Application() {
//        val files = CloudMessageBox(title = "选择目标文件",node = FileChooser(),button = arrayOf("确定"), property = MessageBoxProperty().apply { icon.set(iconImage) }).showOnNode() as FileController
//        primaryStage.onShowingProperty().addListener { _, _, _ ->
//            val node = (((primaryStage.scene.root as BasePane).children[0] as BorderPane).center as TimeMonitor).lyricView.lookup(".scroll-bar") as ScrollBar
//            node.valueProperty().addListener { _, _, newValue ->
//                println(newValue)
//            }
//        }
    companion object{
        val font: Font = Font.loadFont(this.javaClass.getResourceAsStream("/Font/alibaba_font.ttf"), 20.0)

        val config = Config().let {
            if (File("config.proto").exists()) {
                ProtoBuf.decodeFromByteArray(Files.readAllBytes(File("config.proto").toPath()))
            } else {
                it
            }
        }
    }

    override fun start(primaryStage: Stage) {
        primaryStage.icons.add(Image(javaClass.getResourceAsStream("/icon.png")))
//        val files = CloudMessageBox(title = "选择目标文件",node = FileChooser(),button = arrayOf("确定"), property = MessageBoxProperty().apply { icon.set(primaryStage.icons[0]) }).showOnNode() as FileChooser
        val files = FileChooser()
        val player = MediaPlayer(Media(files.music.get().toURI().toString())).apply {
            var index = 0
            currentTimeProperty().addListener { _, _, newValue ->
                ((primaryStage.scene.root as BasePane).children[0] as BorderPane).run {

//                    控制进度条与播放进度对齐
                    ((bottom as VBox).children[1] as CloudLinearProgressBar).property.progress.set(
                        newValue.toMillis() / cycleDuration.toMillis()
                    )

//                    时间显示器
                    ((bottom as VBox).children[0] as Label).text = "${getTime(currentTime)} / ${getTime(cycleDuration)}"

//                    预览模式滚动歌词
                    (center as TimeMonitor).run {
                        if (!(userData as Boolean)) return@addListener
                        if (timeLineGuardian.size == index) return@addListener
                        if (newValue.toMillis() >= timeLineGuardian[index].toMillis()) {
                            (children[0] as ListView<*>).selectionModel.select(++index)
                            (children[1] as ListView<*>).selectionModel.select(index)
                        }
                    }

                }
            }

//          重新开始时返回初始位置
            statusProperty().addListener { _, _, newValue ->
                (((primaryStage.scene.root as BasePane).children[0] as BorderPane).center as TimeMonitor).run {
                    when (newValue){
                        MediaPlayer.Status.PLAYING -> {
                            this.children[1].requestFocus()
                            if (currentTime == Duration.ZERO) {
                                (children[0] as ListView<*>).selectionModel.select(0)
                                (children[1] as ListView<*>).selectionModel.select(0)
                                (children[0] as ListView<*>).scrollTo(0)
                                (children[1] as ListView<*>).scrollTo(0)
                            }
                        }
                    }
                }
            }

        }

        primaryStage.run {
            scene = Scene(BasePane(icons[0], files, player, primaryStage),920.0,800.0).apply {
                stylesheets.add("/Font/font.css")
                bindHotkey(player)
                fill = null
            }

            setOnShown { ZoomIn(scene.root).play() }
            initStyle(StageStyle.TRANSPARENT)
            title = "LyricMaker"
            show()
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


    private fun Scene.bindHotkey(player: MediaPlayer) {

        accelerators[KeyCodeCombination(config.pauseCode)] = Runnable {
            if (player.status == MediaPlayer.Status.PLAYING) {
                player.pause()
            } else {
                player.play()
            }
        }

        accelerators[KeyCodeCombination(config.backKeyCode)] = Runnable {
            if (player.status == MediaPlayer.Status.PLAYING) {
                player.seek(player.currentTime.subtract(Duration.millis(1000.0)))
            }
        }
        accelerators[KeyCodeCombination(config.fastForwardCode)] = Runnable {
            if (player.status == MediaPlayer.Status.PLAYING) {
                player.seek(player.currentTime.add(Duration.millis(1000.0)))
            }
        }
    }

}

