package com.permissionmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.permissionmonitor.ui.navigation.NavGraph
import com.permissionmonitor.ui.theme.PermissionMonitorTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        var refreshTrigger by mutableStateOf(0)
            private set
        
        fun triggerRefresh() {
            refreshTrigger++
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        triggerRefresh()
    }
}
