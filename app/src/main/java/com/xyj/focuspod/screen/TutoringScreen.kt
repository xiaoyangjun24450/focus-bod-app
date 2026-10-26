package com.xyj.focuspod.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.AiConversationCard
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.FlipHint
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.QuestionPanel
import com.xyj.focuspod.ui.component.StageBar

/** 页面 4 · 辅导页（Figma 112:1345）。 */
@Composable
fun TutoringScreen(state: StudySessionState) {
    DesignCanvas {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            StageBar(stageLabel = "题目", remaining = state.remainingCount)
            Spacer(modifier = Modifier.height(24.dp))
            QuestionPanel(
                questionNumber = state.questionNumber,
                question = state.currentQuestion
            )
            Spacer(modifier = Modifier.height(12.dp))
            AiConversationCard(messages = state.tutoringMessages)
            Spacer(modifier = Modifier.weight(1f))
            FlipHint(text = "完成后请翻转手机开始批卷")
            Spacer(modifier = Modifier.height(40.dp))
        }
        NenyaAlert(message = state.alertMessage)
    }
}
