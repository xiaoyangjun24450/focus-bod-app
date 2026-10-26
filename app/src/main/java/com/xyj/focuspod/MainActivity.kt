package com.xyj.focuspod

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import com.xyj.focuspod.app.AppContainer
import com.xyj.focuspod.model.StudyPage
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.screen.FocusPodApp

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer
    private lateinit var studyFlow: com.xyj.focuspod.flow.StudyFlow
    private val currentState = mutableStateOf(StudySessionState())
    private val observer: (StudySessionState) -> Unit = { state ->
        currentState.value = state
    }
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // StudyFlow surfaces permission failures in the relevant page if permission is denied.
    }
    private var cameFromBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this, application)
        studyFlow = appContainer.studyFlow
        // 三页主流程始终是浅色底：强制深色系统栏图标，避免白字压在浅底上。
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        studyFlow.observe(observer)
        setContent {
            val state = currentState.value
            // 批卷页横屏全屏取景，其余页面保持倒置竖屏（手机反着放进仓里）。
            LaunchedEffect(state.page) {
                val grading = state.page == StudyPage.GRADING
                requestedOrientation = if (grading) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                }
                // 批卷页背景是深色实拍画面，系统栏图标要用浅色；其余页面是浅色底。
                val barStyle = if (grading) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
            FocusPodApp(
                state = state,
                flow = studyFlow
            )
        }
        requestRuntimePermissionsIfNeeded()
        val previewName = if (BuildConfig.DEBUG) intent?.getStringExtra("preview") else null
        val previewPage = previewName?.let { name ->
            runCatching { StudyPage.valueOf(name) }.getOrNull()
        }
        if (previewPage != null) {
            studyFlow.previewPage(previewPage)
        } else {
            studyFlow.start()
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 从后台回到前台时，若还停在启动页/计划页，重放一次启动页，
        // 避免「点开图标直接跳到选择页」。锁仓后的学习流程不打断。
        if (cameFromBackground) {
            cameFromBackground = false
            val page = currentState.value.page
            if (page == StudyPage.START || page == StudyPage.PLAN_SELECT) {
                studyFlow.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        cameFromBackground = true
    }

    override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun onDestroy() {
        studyFlow.release()
        studyFlow.removeObserver(observer)
        super.onDestroy()
    }

    private fun requestRuntimePermissionsIfNeeded() {
        val missingPermissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        ).filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
