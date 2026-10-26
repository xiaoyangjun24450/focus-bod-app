package com.xyj.focuspod.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xyj.focuspod.model.GradeResult
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.model.StudyStage
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaPod
import com.xyj.focuspod.ui.component.StageBar
import com.xyj.focuspod.ui.component.scoreText
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/** 页面 7 · 批卷结果页（Figma 112:1434）。 */
@Composable
fun GradeResultScreen(state: StudySessionState) {
    val result = state.lastGradeResult
    DesignCanvas {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            StageBar(stageLabel = "本题结果", remaining = state.remainingCount)
            Spacer(modifier = Modifier.height(38.dp))
            NenyaPod(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "第 ${state.questionNumber} 题完成",
                style = NenyaText.SectionTitle,
                color = NenyaColor.Deep,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = scoreText(result?.score ?: 0.0),
                    style = TextStyle(fontSize = 88.sp, lineHeight = 104.sp, fontWeight = FontWeight.Bold),
                    color = NenyaColor.Deep
                )
                Text(
                    text = " 分",
                    style = TextStyle(fontSize = 26.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium),
                    color = NenyaColor.Muted
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = verdictOf(result),
                style = TextStyle(fontSize = 28.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
                color = NenyaColor.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = feedbackOf(result),
                style = TextStyle(fontSize = 22.sp, lineHeight = 34.sp),
                color = NenyaColor.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NenyaColor.Pale)
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${state.resultCountdownSeconds} 秒后${countdownTargetOf(state)}",
                    style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
                    color = NenyaColor.Deep
                )
                Text(
                    text = nextStepOf(state),
                    style = TextStyle(fontSize = 22.sp, lineHeight = 34.sp),
                    color = NenyaColor.Ink
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "正在语音播报本题结果",
                style = NenyaText.SectionTitle,
                color = NenyaColor.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
        NenyaAlert(message = state.alertMessage)
    }
}

private fun verdictOf(result: GradeResult?): String {
    if (result == null) return "等待批改结果"
    val ratio = if (result.maxScore > 0) result.score / result.maxScore else 0.0
    return when {
        ratio >= 0.999 -> "全对，保持这个状态"
        ratio >= 0.8 -> "思路正确，再细心一点"
        ratio >= 0.5 -> "方向对了，细节再补一补"
        else -> "这题需要再练一次"
    }
}

private fun feedbackOf(result: GradeResult?): String {
    val feedback = result?.feedback.orEmpty()
    val suggestion = result?.suggestion.orEmpty()
    return when {
        feedback.isBlank() && suggestion.isBlank() -> "等待批改反馈"
        suggestion.isBlank() -> feedback
        feedback.isBlank() -> suggestion
        else -> "$feedback\n$suggestion"
    }
}

private fun countdownTargetOf(state: StudySessionState): String = when {
    state.remainingCount > 1 -> "进入下一题"
    state.stage == StudyStage.EXAMPLE -> "进入考试"
    else -> "查看考试成绩"
}

private fun nextStepOf(state: StudySessionState): String = when {
    state.remainingCount > 1 -> "下一题：第 ${state.questionNumber + 1} 题"
    state.stage == StudyStage.EXAMPLE -> "例题讲解完成，接下来进入考试"
    else -> "考试完成，接下来看考试成绩"
}
