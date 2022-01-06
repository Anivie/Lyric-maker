package LyricParser


import java.io.File
import java.nio.file.Files
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class LyricParser() {
    private var timeLine = arrayOf<String>()
    private var lyric = arrayOf<String>()

    /**
     * 使用歌词文件
     */
    constructor(file:File, adaptiveAccuracy:Boolean = true, highPrecision:Boolean = false) : this(){
        val list = Files.readAllLines(file.toPath())

        if (adaptiveAccuracy) {
            //自适应精度(默认)
            timeLine = list.map { it.substring(1,it.indexOf(']') ) }.filter { isTimeLine(it) }.toTypedArray()
            lyric = list.map { it.substring(it.indexOf(']') + 1) }.toTypedArray()
        } else {
            //自定义:高精度
            if (highPrecision) {
                timeLine = list.map { it.substring(1, 10) }
                    .filter { isTimeLine(it) }
                    .toTypedArray()

                lyric = list.map { it.substring( 11) }
                    .toTypedArray()
            } else {
                //自定义:低精度(默认)
                timeLine = list.map { it.substring(1, 9) }
                    .filter { isTimeLine(it) }
                    .toTypedArray()

                lyric = list.map { it.substring(10) }
                    .toTypedArray()
            }
        }
    }

    /**
     * 直接使用歌词
     */
    constructor(lyrics:String, adaptiveAccuracy:Boolean = true, highPrecision:Boolean = false) : this(){
        //添加对单行歌词的支持
        val list = lyrics.split('\r').let { it ->
            if (it.size != 1) {
                return@let it
            } else {
                it[0].let { me ->
                    me.replace("[", "\r[")
                    me.split('\r')
                }
            }
        }

        if (adaptiveAccuracy) {
            //自适应精度(默认)
            timeLine = list.map { it.substring(1,it.indexOf(']') ) }.filter { isTimeLine(it) }.toTypedArray()
            lyric = list.map { it.substring(it.indexOf(']') + 1) }.toTypedArray()
        } else {
            //自定义:高精度
            if (highPrecision) {
                timeLine = list.map { it.substring(1, 10) }
                    .filter { isTimeLine(it) }
                    .toTypedArray()

                lyric = list.map { it.substring( 11) }
                    .toTypedArray()
            } else {
                //自定义:低精度(默认)
                timeLine = list.map { it.substring(1, 9) }
                    .filter { isTimeLine(it) }
                    .toTypedArray()

                lyric = list.map { it.substring(10) }
                    .toTypedArray()
            }
        }
    }

    fun getTimeLineWithDuration(): Array<Duration> {
        return getTimeLineWithArray().mapTo(ArrayList()) { it.toDuration(DurationUnit.MILLISECONDS) }.toTypedArray()
    }

    fun getTimeLineWithArray(): Array<Int> {
        return timeLine.mapTo(ArrayList()) {
            (it.substring(0, 2).toInt() * 60000) + (it.substring(3, 5).toInt() * 1000) + (it.substring(6).toInt())
        }.toTypedArray()
    }

    fun getLyric(): Array<String> {
        return lyric
    }

    private fun isTimeLine(target: String): Boolean {
        target.toCharArray().forEach {
            if (it in 'A'..'Z' || it in 'a'..'z') {
                return false
            }
        }
        return true
    }

}