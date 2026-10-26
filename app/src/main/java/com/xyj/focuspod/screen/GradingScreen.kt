package com.xyj.focuspod.screen

import androidx.compose.runtime.Composable
import com.xyj.focuspod.flow.StudyFlow
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaGradingLandscape
import com.xyj.focuspod.ui.component.stageText

/** 页面 6 · 批卷：横屏全屏取景，学生按右侧快门拍照。 */
@Composable
fun GradingScreen(state: StudySessionState, flow: StudyFlow) {
    DesignCanvas(width = 892f, height = 412f) {
        NenyaGradingLandscape(
            step = state.gradingStep,
            stageLabel = stageText(state.stage),
            remaining = state.remainingCount,
            onPreviewReady = flow::bindGradingCameraPreview,
            onCapture = flow::captureNow
        )
        NenyaAlert(message = state.alertMessage)
    }
}
