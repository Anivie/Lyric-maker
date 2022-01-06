package ink.bluecloud.lyricmaker.Data

import javafx.scene.input.KeyCode
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    var backKeyCode: KeyCode = KeyCode.A,
    var fastForwardCode: KeyCode = KeyCode.D,
    var pauseCode: KeyCode = KeyCode.P,

    var backTime:Long = 1000,
    var fastTime:Long = 1000
)