package ink.bluecloud.lyricmaker.MainView.Node


import CloudNodes.CloudButton.CloudButton
import CloudNodes.CloudTextFiled.CloudTextFiled
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class FileChooser : VBox(){
    val music = SimpleObjectProperty<File>()
    val lyric = SimpleObjectProperty<File>()
//    val music = SimpleObjectProperty<File>(File("C:\\Users\\Anivie\\Desktop\\d.mp3"))
//    val lyric = SimpleObjectProperty<File>(File("C:\\Users\\Anivie\\Desktop\\d.txt"))
    init {
        val chooser = FileChooser()
        val musicBox = getFileChooser(chooser, "歌曲地址:", "选择音频文件",true)
        val lyricBox = getFileChooser(chooser, "歌词地址:", "选择歌词文件",false)

        spacing = 10.0
        padding = Insets(0.0,20.0,0.0,20.0)
        children.addAll(musicBox,lyricBox)
    }

    private fun getFileChooser(chooser: FileChooser, title: String, openerTitle: String, isMusic:Boolean) : HBox{
        return HBox(10.0).apply {
            val label = Label(title).apply {
                textFill = Color.web("#999999")
                font = Font(15.0)
            }
            val filed = CloudTextFiled(width = 300.0, height =  30.0).apply {
                baseFiled.isEditable = false
                if (isMusic){
                    music.addListener { _, _, newValue -> baseFiled.text = newValue.absolutePath }
                }else {
                    lyric.addListener { _, _, newValue -> baseFiled.text = newValue.absolutePath }
                }
            }
            val button = CloudButton(text = "打开文件").apply {
                onMouseClicked = EventHandler {
                    chooser.title = openerTitle
                    chooser.initialDirectory = File(System.getProperty("user.home"))
                    if (isMusic){
                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Music Files", "*.mp3"))
                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Music Files", "*.wav"))
//                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Music Files", "*.*"))
                        music.set(chooser.showOpenDialog(Stage()).let { it ?: return@EventHandler })
                        val tmp = File(music.get().absolutePath.substringBefore('.') + ".txt")
                        if (tmp.exists()) lyric.set(tmp)
                    }else {
                        chooser.extensionFilters.clear()
                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Lyric Files", "*.txt"))
//                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Lyric Files", "*.lrc"))
//                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Lyric Files", "*.docx"))
                        lyric.set(chooser.showOpenDialog(Stage()).let { it ?: return@EventHandler })
                        var tmp = File(lyric.get().absolutePath.substringBefore('.') + ".mp3")
                        if (tmp.exists()) music.set(tmp)

                        tmp = File(lyric.get().absolutePath.substringBefore('.') + ".wav")
                        if (tmp.exists()) music.set(tmp)
                    }
                }
            }
            alignment = Pos.CENTER
            children.addAll(label,filed,button)
        }
    }


}