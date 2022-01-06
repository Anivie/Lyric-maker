package ink.bluecloud.lyricmaker.MainView.Node

import CloudNodes.CloudNotice.CloudNotice
import CloudNodes.CloudNotice.Property.NoticeType
import CloudNodes.CloudTextFiled.CloudTextFiled
import ink.bluecloud.lyricmaker.Data.Config
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.Event
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.io.FileOutputStream

class SettingView(private val config: Config,private val args: ControlArgs) :VBox(){
    var runLock = SimpleIntegerProperty()

    init {
        children.run {
            //快进
            add(HBox(50.0).apply {
                children.addAll(
                    HBox(5.0).apply {
                        children.addAll(
                            Label("快进按钮："),
                            CloudTextFiled(45.0,30.0).apply {
                                baseFiled.run {
                                    text = config.fastForwardCode.getName()
                                    setOnMouseClicked { selectAll() }
                                    addEventFilter(KeyEvent.KEY_TYPED,Event::consume)
                                    setOnKeyPressed {
                                        text = it.code.getName()
                                    }
                                }
                            }
                        )
                        alignment = Pos.CENTER
                    },
                    HBox(5.0).apply {
                        children.addAll(
                            Label("快进时长："),
                            CloudTextFiled(45.0,30.0).apply {
                                baseFiled.run {
                                    text = "1000"
                                    setOnMouseClicked { selectAll() }
                                    textProperty().addListener { _, old, newValue ->
                                        repeat(newValue.length) {
                                            if (newValue[it] !in '0'..'9') text = old
                                        }
                                    }
                                }
                            }
                        )
                        alignment = Pos.CENTER
                    }
                )
            })

            //后退
            add(HBox(50.0).apply {
                children.addAll(
                    HBox(5.0).apply {
                        children.addAll(
                            Label("后退按钮："),
                            CloudTextFiled(45.0,30.0).apply {
                                baseFiled.run {
                                    text = config.backKeyCode.getName()
                                    setOnMouseClicked { selectAll() }
                                    addEventFilter(KeyEvent.KEY_TYPED,Event::consume)
                                    setOnKeyPressed {
                                        text = it.code.getName()
                                    }
                                }
                            }
                        )
                        alignment = Pos.CENTER
                    },
                    HBox(5.0).apply {
                        children.addAll(
                            Label("后退时长："),
                            CloudTextFiled(45.0,30.0).apply {
                                baseFiled.run {
                                    text = "1000"
                                    setOnMouseClicked { selectAll() }
                                    textProperty().addListener { _, old, newValue ->
                                        repeat(newValue.length) {
                                            if (newValue[it] !in '0'..'9') text = old
                                        }
                                    }
                                }
                            }
                        )
                        alignment = Pos.CENTER
                    },
                )
            })

            //暂停
            add(HBox(5.0).apply {
                children.addAll(
                    Label("暂停按钮："),
                    CloudTextFiled(45.0,30.0).apply {
                        baseFiled.run {
                            text = config.pauseCode.getName()
                            setOnMouseClicked { selectAll() }
                            addEventFilter(KeyEvent.KEY_TYPED,Event::consume)
                            setOnKeyPressed {
                                text = it.code.getName()
                            }
                        }
                    }
                )
                alignment = Pos.CENTER
            })
        }


        runLock.addListener { _, _, newValue ->
            if (newValue == 0) {
                //none test!
                var saving:Config? = null
                if (this.config.fastTime != (((this.children[0] as HBox).children[1] as HBox).children[1] as CloudTextFiled).baseFiled.text.toLong()) {
                    saving = Config()
                    saving.fastTime = (((this.children[0] as HBox).children[1] as HBox).children[1] as CloudTextFiled).baseFiled.text.toLong()
                }
                if (this.config.backTime != (((this.children[1] as HBox).children[1] as HBox).children[1] as CloudTextFiled).baseFiled.text.toLong()) {
                    if (saving == null) saving = Config()
                    saving.backTime = (((this.children[1] as HBox).children[1] as HBox).children[1] as CloudTextFiled).baseFiled.text.toLong()
                }
                if (this.config.fastForwardCode.getName() != (((this.children[0] as HBox).children[0] as HBox).children[1] as CloudTextFiled).baseFiled.text) {
                    if (saving == null) saving = Config()
                    saving.fastForwardCode = KeyCode.valueOf((((this.children[0] as HBox).children[0] as HBox).children[1] as CloudTextFiled).baseFiled.text)
                }
                if (this.config.backKeyCode.getName() != (((this.children[1] as HBox).children[0] as HBox).children[1] as CloudTextFiled).baseFiled.text) {
                    if (saving == null) saving = Config()
                    saving.backKeyCode = KeyCode.valueOf((((this.children[1] as HBox).children[0] as HBox).children[1] as CloudTextFiled).baseFiled.text)
                }
                if (this.config.pauseCode.getName() != (((this.children[2] as HBox).children[1] as CloudTextFiled).baseFiled.text)) {
                    if (saving == null) saving = Config()
                    saving.pauseCode = KeyCode.valueOf((((this.children[2] as HBox).children[1] as CloudTextFiled).baseFiled.text))
                }

                saving?.run saving@{
                    FileOutputStream(File("config.proto")).run {
                        write(ProtoBuf.encodeToByteArray(saving))
                        close()
                    }
                    CloudNotice(NoticeType.Right,"设置已保存，将在应用重启后生效。", stage = args.stage).show()
                }
            } else if (newValue == 1) {
                File("config.proto").run {
                    if (exists()) {
                        delete()
                        CloudNotice(NoticeType.Right,"重置成功，设置将在应用重启后生效。", stage = args.stage).show()
                    }
                }
            }
        }

        padding = Insets(10.0)
    }

}