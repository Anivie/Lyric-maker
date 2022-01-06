package ink.bluecloud.lyricmaker.MainView.Node

import CloudNodes.CloudButton.CloudButton
import CloudNodes.CloudButton.Data.ButtonProperty
import CloudNodes.CloudButton.Data.Theme.ButtonBlueTheme
import CloudNodes.CloudButton.Data.Theme.ButtonGreenTheme
import CloudNodes.CloudButton.Data.Theme.ButtonRedTheme
import CloudNodes.CloudButton.Data.Theme.ButtonYellowTheme
import CloudNodes.CloudMessageBox.CloudMessageBox
import CloudNodes.CloudNotice.CloudNotice
import CloudNodes.CloudNotice.Property.NoticeType
import ink.bluecloud.lyricmaker.MainView.MainView.Companion.config
import ink.bluecloud.lyricmaker.MainView.MainView.Companion.font
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.MediaPlayer
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.concurrent.thread

data class ControlArgs(val monitor: TimeMonitor, val musicFile:File, val lrcFile:File, val player: MediaPlayer, val stage: Stage)

class Controller(args: ControlArgs) :BorderPane(){
    init {
        //播放控制器
        top = VBox(40.0).apply {
            children.run controlBox@{
                //控制录制三剑客
                add(StackPane().apply {
                    children.run {
                        add(Label("歌词制作").apply {
                            style = "-fx-text-fill: #989898;" +
                                    "-fx-font-size: 12;" +
                                    "-fx-background-color: white"
                        })

                        add(BorderPane().apply {
                            //播放按钮
                            left = CloudButton(
                                theme = ButtonGreenTheme(),
                                property = ButtonProperty("\uE62E").apply { textFont.set(font) }
                            ).apply {
                                setOnMouseClicked {
                                    if (args.monitor.timeLineList[1] != "[00:00.00]") {
                                        if (CloudMessageBox(title = "重新开始", message = "重新开始将重置您已经录制的时间线,确认继续？",).showAndGet() == 1) return@setOnMouseClicked
                                    }
                                    if (args.monitor.userData == true) args.monitor.userData = false

                                    repeat(args.monitor.timeLineList.size) { if (args.monitor.timeLineList[it] != "[00:00.00]") args.monitor.timeLineList[it] = "[00:00.00]" }

                                    this@Controller.run {
                                        center.isDisable = true
                                        bottom.isDisable = true
                                    }
                                    ((this@controlBox[1] as StackPane).children[1] as BorderPane).isDisable = true
                                    this.isDisable = true
                                    center.isDisable = false

                                    args.player.play()
                                }
                            }

                            center = CloudButton(
                                theme = ButtonYellowTheme(),
                                property = ButtonProperty("\uE62F").apply { textFont.set(font) }
                            ).apply {
                                onMouseClicked = EventHandler {
                                    if (args.player.status == MediaPlayer.Status.PLAYING) {
                                        args.player.pause()
                                        property.text.set("\uE62E")
                                    } else {
                                        args.player.play()
                                        property.text.set("\uE62F")
                                    }
                                }

                                isDisable = true
                            }

                            right = CloudButton(
                                theme = ButtonRedTheme(),
                                property = ButtonProperty("\uE630").apply { textFont.set(font) }
                            ).apply {
                                onMouseClicked = EventHandler {
                                    args.player.seek(Duration.millis(0.0))
                                    args.player.pause()

                                    ((this@controlBox[1] as StackPane).children[1] as BorderPane).isDisable = false
                                    this@Controller.center.isDisable = false
                                    this@Controller.bottom.isDisable = false
                                    left.isDisable = false
                                    center.isDisable = true
                                }
                            }
                            padding = Insets(5.0)
                        })
                    }

                    style = "-fx-border-width: 1;" +
                            "-fx-border-color: #989898;" +
                            "-fx-border-radius: 5;"

                    StackPane.setAlignment(children[0],Pos.TOP_LEFT)
                    StackPane.setMargin(children[0], Insets(-9.0,0.0,0.0,5.0))
                    StackPane.setMargin(children[1],Insets(5.0,0.0,0.0,0.0))
                })

                //控制试听三剑客
                add(StackPane().apply {
                    children.run {
                        children.add(Label("歌词测试").apply {
                            style = "-fx-text-fill: #989898;" +
                                    "-fx-font-size: 12;" +
                                    "-fx-background-color: white"
                        })
                        children.add(BorderPane().apply {
                            //开始
                            left = CloudButton(theme = ButtonGreenTheme(), property = ButtonProperty("\uE62E").apply { textFont.set(font) }).apply {
                                onMouseClicked = EventHandler {
                                    args.monitor.userData = true
                                    args.player.run {
                                        seek(Duration.ZERO)
                                        play()
                                    }
                                    ((this@controlBox[0] as StackPane).children[1] as BorderPane).isDisable = true
                                    this@Controller.center.isDisable = true
                                    this@Controller.bottom.isDisable = true
                                    center.isDisable = false
                                    isDisable = true
                                }
                            }
                            center = CloudButton(theme = ButtonYellowTheme(), property = ButtonProperty("\uE62F").apply { textFont.set(font) }).apply {
                                onMouseClicked = EventHandler {
                                    if (args.player.status == MediaPlayer.Status.PLAYING) {
                                        args.player.pause()
                                        property.text.set("\uE62E")
                                    } else {
                                        args.player.play()
                                        property.text.set("\uE62F")
                                    }
                                }

                                isDisable = true
                            }
                            //停止
                            right = CloudButton(theme = ButtonRedTheme(), property = ButtonProperty("\uE630").apply { textFont.set(font) }).apply {
                                onMouseClicked = EventHandler {
                                    args.player.run {
                                        seek(Duration.millis(0.0))
                                        pause()
                                    }
                                    ((this@controlBox[0] as StackPane).children[1] as BorderPane).isDisable = false
                                    this@Controller.center.isDisable = false
                                    this@Controller.bottom.isDisable = false
                                    left.isDisable = false
                                    center.isDisable = true
                                }
                            }

                            padding = Insets(5.0)
                        })
                    }

                    style = "-fx-border-width: 1;" +
                            "-fx-border-color: #989898;" +
                            "-fx-border-radius: 5;"
                    StackPane.setMargin(children[0], Insets(-9.0,0.0,0.0,5.0))
                    StackPane.setAlignment(children[0],Pos.TOP_LEFT)

                    StackPane.setMargin(children[1],Insets(5.0,0.0,0.0,0.0))
                })
            }

            padding = Insets(10.0,0.0,0.0,5.0)
            alignment = Pos.CENTER
        }

        //文件保存器
        center = VBox(10.0).apply {
            children.add(CloudButton(theme = ButtonBlueTheme(), property = ButtonProperty("\uE961\r构建").apply { textFont.set(font) }).apply {
                setOnMouseClicked {
                    buildFile(args)
                }
            })

            padding = Insets(10.0)
            alignment = Pos.CENTER
        }

        //下方工具栏
        bottom = BorderPane().apply {
            left = StackPane().apply {
                children.add(VBox(10.0).apply {
                    //加
                    children.add(CloudButton("\uE600",theme = ButtonGreenTheme(),property = ButtonProperty().apply { textFont.set(font) }).apply {
                        onMouseClicked = EventHandler {
                        }
                    })
                    //减
                    children.add(CloudButton(theme = ButtonYellowTheme(),property = ButtonProperty("\uE8A7").apply { textFont.set(font) }).apply {
                        onMouseClicked = EventHandler {
                            repeat(args.monitor.timeLineList.size){ if (args.monitor.timeLineList[it] != "[00:00.00]") args.monitor.timeLineList[it] = "[00:00.00]" }
                        }
                    })

                    padding = Insets(0.0,0.0,0.0,0.0)
                })

                (args.monitor.children[1] as ListView<*>).focusedProperty().addListener { _, _, nV ->
                    if (nV){
                        style = "-fx-border-width: 2 2 2 0;" +
                                "-fx-border-color: #DDF2F9;" +
                                "-fx-border-radius: 0 5 5 0"
                        this@Controller.padding = Insets(0.0,0.0,9.0,0.0)
                    }else {
                        style = "-fx-border-width: 1 1 1 0;" +
                                "-fx-border-color: #C8C8C8;" +
                                "-fx-border-radius: 0 5 5 0"
                        this@Controller.padding = Insets(0.0,0.0,10.0,0.0)
                    }
                }
                style = "-fx-border-width: 1 1 1 0;" +
                        "-fx-border-color: #C8C8C8;" +
                        "-fx-border-radius: 0 5 5 0"
                padding = Insets(2.0)
                maxWidth = 45.0
            }

            right = VBox(10.0).apply {
                children.run {
                    //setting
                    add(CloudButton(theme = ButtonBlueTheme(), property = ButtonProperty("\uE6DF").apply { textFont.set(font) }).apply {
                        setOnMouseClicked {
                            val settingView = SettingView(config,args)
                            val back = CloudMessageBox(
                                title = "设置",
                                button = arrayOf("确定", "重置", "取消"),
                                node = settingView,
//                                property = MessageBoxProperty().apply { icon.set(args.stage.icons[0]) }
                            ).showAndGet()
                            settingView.runLock.set(back)
                        }
                    })

                    //help
                    add(CloudButton("\uE601", property = ButtonProperty().apply { textFont.set(font) }))
                }
                alignment = Pos.BOTTOM_RIGHT
                padding = Insets(0.0,0.0,3.0,0.0)
            }
            setAlignment(right,Pos.BOTTOM_RIGHT)
        }

        padding = Insets(0.0,0.0,10.0,0.0)
    }

    private fun buildFile(args: ControlArgs) {
        val notice = CloudNotice(noticeType = NoticeType.Right, message = "歌词文件保存成功!", stage = args.stage)
        val path = FileChooser().apply {
            title = "保存到lrc文件"
            initialDirectory = File(System.getProperty("user.home"))
            extensionFilters.add(FileChooser.ExtensionFilter("Lyric Files", "*.lrc"))
        }.showSaveDialog(Stage()).let { it ?: return }.toPath()

        thread {
            Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE).run {
                repeat(args.monitor.timeLineList.size) {
                    write("${args.monitor.timeLineList[it]} ${args.monitor.lyricList[it]}\r")
                }
                close()
            }
            Platform.runLater { notice.show() }
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