package moe.ktandth.songmeld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import moe.ktandth.songmeld.ui.SongMeldApp
import moe.ktandth.songmeld.ui.theme.SongMeldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SongMeldTheme {
                SongMeldApp()
            }
        }
    }
}
