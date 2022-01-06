package ink.bluecloud.lyricmaker.MainView.Node

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Callback
import javafx.util.Duration
import java.io.File
import java.nio.file.Files
import java.util.*

class TimeMonitor(lyric:File, private val player: MediaPlayer) :HBox() {
    val lyricList: ObservableList<String> = FXCollections.observableArrayList(Files.readAllLines(lyric.toPath()))
    val timeLineList: ObservableList<String> = FXCollections.observableArrayList<String>().apply {
        repeat(lyricList.size){ this += "[00:00.00]" }
    }

    val timeLineGuardian = LinkedList<Duration>()

    init {
        //is preview?
        userData = false

        children.run {
            add(ListView(timeLineList).apply {
                stylesheets.add("/css/my.css")
                isEditable = true
            })

            add(ListView(lyricList).apply {
                cellFactory = Callback<ListView<String>, ListCell<String>>(){ LyricCell(lyricList) }

                //自动滚动
                viewScroller()

                stylesheets.add("/css/my.css")
                isEditable = true
                prefWidth = 600.0
            })
        }
        (children[0] as ListView<*>).cellFactory = Callback<ListView<String>, ListCell<String>>(){ TimeLineCell(timeLineList,children[1] as ListView<String>) }

        padding = Insets(10.0,0.0,10.0,0.0)
    }

    private fun ListView<String>.viewScroller() {
        val midItem = 9
        selectionModel.selectedIndexProperty().addListener { _, old, new ->
            if ((player.status == MediaPlayer.Status.PLAYING)) {
                if (new.toInt() >= midItem) scrollTo(new.toInt() - midItem)
                if (new.toInt() >= midItem) (this@TimeMonitor.children[0] as ListView<*>).scrollTo(new.toInt() - midItem)
            }

            timeLineList.run {
                if (this@TimeMonitor.userData as Boolean) return@addListener
                if (new.toInt() == 0 || (player.status != MediaPlayer.Status.PLAYING)) return@addListener

                if (new.toInt() > old.toInt()) {
                    timeLineList[new.toInt()] = getTime()
                    timeLineGuardian.add(player.currentTime)
                    removeAt(new.toInt())
                    add(new.toInt(), getTime())
                    (this@TimeMonitor.children[0] as ListView<*>).selectionModel.select(new.toInt())
                } else {

                    if (new.toInt() >= 2) {
                        timeLineGuardian[timeLineGuardian.size - 2].run {
                            player.seek(this)
                            timeLineGuardian.remove(timeLineGuardian.last())
                        }
                        (this@TimeMonitor.children[0] as ListView<*>).selectionModel.select(new.toInt())
                    } else {
                        player.seek(Duration.ZERO)
                        timeLineGuardian.clear()
                    }

                }
            }

        }
    }

    class TimeLineCell(private val timeLineList: ObservableList<String>, private val lyricView: ListView<String>): ListCell<String>() {
        private val label = Label(item).apply { style = "-fx-font-size: 20px;-fx-text-fill: #5E5E5E" }
        private val hBox = HBox(label).apply { alignment = Pos.CENTER }

        init {
            selectedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    label.apply {
                        font = Font(25.0)
                        textFill = Color.web("#00D5FF")
                    }
                } else {
                    label.apply {
                        font = Font(20.0)
                        textFill = Color.web("#5E5E5E")
                    }
                }
            }
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            label.text = item
            graphic = hBox
        }

        override fun startEdit() {
            super.startEdit()
            val temp = graphic
            graphic = HBox().apply {
                children.addAll(
                    Text("【"),
                    TextField(timeLineList[index].substring(1..2)).apply { prefWidth = 37.0 },//1
                    Text("："),
                    TextField(timeLineList[index].substring(4..5)).apply { prefWidth = 37.0 },//3
                    Text("."),
                    TextField(timeLineList[index].substring(7..9)).apply { prefWidth = 45.0 },//5
                    Text("】")
                )
                alignment = Pos.CENTER
            }
            (graphic as HBox).children[1].setOnKeyPressed {
                if (it.code == KeyCode.ENTER) {
                    (graphic as HBox).children[3].requestFocus()
                    ((graphic as HBox).children[3] as TextField).selectAll()
                }
                it.consume()
            }

            (graphic as HBox).children[3].setOnKeyPressed {
                if (it.code == KeyCode.ENTER) {
                    (graphic as HBox).children[5].requestFocus()
                    ((graphic as HBox).children[5] as TextField).selectAll()
                }
                it.consume()
            }

            (graphic as HBox).children[5].setOnKeyPressed { event ->
                if (event.code == KeyCode.ENTER) {
                    commitEdit("[${
                        ((graphic as HBox).children[1] as TextField).text.let {
                            if (it.length > 2) {
                                it.substring(0..1)
                            } else if (it.length == 1){
                                "0$it"
                            } else {
                                it
                            }
                        }
                    }:${
                        ((graphic as HBox).children[3] as TextField).text.let {
                            if (it.length > 2) {
                                it.substring(0..1)
                            } else if (it.length == 1){
                                "0$it"
                            } else {
                                it
                            }
                        }
                    }.${
                        ((graphic as HBox).children[5] as TextField).text.let {
                            if (it.length > 3) {
                                it.substring(0..2)
                            } else if (it.length == 2){
                                "0$it"
                            } else if (it.length == 1) {
                                "00$it"
                            } else {
                                it
                            }
                        }
                    }]")
                    graphic = temp
                    lyricView.requestFocus()
                    lyricView.selectionModel.select(index)
                }
                event.consume()
            }


            (graphic as HBox).children[1].requestFocus()
            ((graphic as HBox).children[1] as TextField).selectAll()
        }
    }

    class LyricCell(private val lyricList: ObservableList<String>): ListCell<String>() {
        private val label = Label(item).apply { style = "-fx-font-size: 20px;-fx-text-fill: #5E5E5E" }
        private val hBox = HBox(label).apply { alignment = Pos.CENTER }

        init {
            selectedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    label.apply {
                        font = Font(25.0)
                        textFill = Color.web("#00D5FF")
                    }
                } else {
                    label.apply {
                        font = Font(20.0)
                        textFill = Color.web("#5E5E5E")
                    }
                }
            }
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            label.text = item
            graphic = hBox
        }

        override fun startEdit() {
            super.startEdit()
            graphic = TextField(item).apply {
                setOnKeyPressed {
                    it.consume()
                    if (it.code == KeyCode.ENTER){
                        if (text != lyricList[index]) {
                            commitEdit(text)
                        }
                    }
                }
            }
            graphic.requestFocus()
        }
    }

    private fun getTime() : String {
//        println("minute=${player.currentTime.toMinutes()}, second=${player.currentTime.toSeconds()} ,mills=${player.currentTime.toMillis()}")
/*        val minute = player.currentTime.toMinutes().toInt().let {
            if (it < 10) {
                "0$it"
            } else {
                it
            }
        }
        val second = (player.currentTime.toSeconds() % 60).toInt().let {
            if (it < 10) {
                "0$it"
            } else {
                it
            }
        }

        val millis = (
                (((player.currentTime.toMillis() - (((player.currentTime.toMinutes().toInt() * 60000) + ((player.currentTime.toSeconds() % 60).toInt() * 1000)))) / 10)
                ).toInt()).let {
            if (it < 10) {
                "0${it}"
            }else {
                it
            }
        }
        return "[minute:second.millis]"
 */
        return "[${//minute
            player.currentTime.toMinutes().toInt().let {
                if (it < 10) {
                    "0$it"
                } else {
                    it
                }
            }
        }:${//second
            (player.currentTime.toSeconds() % 60).toInt().let {
                if (it < 10) {
                    "0$it"
                } else {
                    it
                } 
            }
        }.${//mills 
            ((player.currentTime.toMillis() - ((player.currentTime.toMinutes().toInt() * 60000) + ((player.currentTime.toSeconds() % 60).toInt() * 1000))) / 10).toInt().let {
                    if (it < 10) {
                        "0${it}"
                    }else {
                        it
                    }
            }
        }]"
    }
}