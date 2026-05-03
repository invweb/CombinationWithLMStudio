package com.application.combinationwithlmstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.application.combinationwithlmstudio.ui.screens.ChatScreen
import com.application.combinationwithlmstudio.ui.theme.CombinationWithLMStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CombinationWithLMStudioTheme {
                ChatScreen()
            }
        }
    }
}