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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xyj.focuspod.R
import com.xyj.focuspod.model.DoorStatus
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaPod
import com.xyj.focuspod.ui.component.StageBar
import com.xyj.focuspod.ui.component.SummaryRow
import com.xyj.focuspod.ui.component.scoreText
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/** 页面 8 · 考试成绩页（Figma 112:1486）。 */
@Composable
fun DoneScreen(state: StudySessionState) {
    val passed = state.examPassed == true
    DesignCanvas {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            StageBar(stageLabel = "考试成绩", remaining = 0)
            Spacer(modifier = Modifier.height(24.dp))
            NenyaPod(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(108.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "全部完成",
                style = NenyaText.SectionTitle,
                color = NenyaColor.Deep,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = scoreText(state.totalExamScore),
                    style = TextStyle(fontSize = 88.sp, lineHeight = 104.sp, fontWeight = FontWeight.Bold),
                    color = NenyaColor.Deep,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = " 分",
                    style = TextStyle(fontSize = 26.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium),
                    color = NenyaColor.Muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (passed) "考试达标，真棒！" else "考试不合格",
                style = TextStyle(fontSize = 28.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
                color = if (passed) NenyaColor.Deep else NenyaColor.Danger,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NenyaColor.Surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryRow(label = "达标分数", value = passScoreText(state))
                SummaryRow(label = "完成题数", value = "${state.completedQuestionCount} 题")
                SummaryRow(label = "未满分题", value = "${state.mistakeCount} 题 · 已记录")
            }
            Spacer(modifier = Modifier.height(28.dp))
            DoorCard(state = state, passed = passed)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "正在播报成绩与开门结果",
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

@Composable
private fun DoorCard(state: StudySessionState, passed: Boolean) {
    val failed = passed && state.doorStatus == DoorStatus.FAILED
    val color = if (!passed || failed) NenyaColor.Danger else NenyaColor.Deep
    val icon = when {
        !passed -> R.drawable.ic_close
        state.doorStatus == DoorStatus.OPENED -> R.drawable.ic_check
        state.doorStatus == DoorStatus.FAILED -> R.drawable.ic_close
        else -> R.drawable.ic_pending
    }
    val title = when {
        !passed -> "考试不合格"
        state.doorStatus == DoorStatus.OPENED -> "开门成功"
        state.doorStatus == DoorStatus.FAILED -> "开门失败"
        else -> "正在开门"
    }
    val detail = when {
        !passed -> "10 秒后开始错题辅导"
        state.doorStatus == DoorStatus.OPENED -> "可以取出手机啦"
        state.doorStatus == DoorStatus.FAILED -> "请联系老师或工作人员"
        else -> "请稍候，正在打开手机仓"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NenyaColor.Pale)
            .padding(vertical = 26.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Text(
            text = detail,
            style = TextStyle(fontSize = 22.sp, lineHeight = 34.sp),
            color = NenyaColor.Ink
        )
    }
}

private fun passScoreText(state: StudySessionState): String {
    return if (state.planScores.size <= 1) {
        "${state.selectedPlan?.passScore ?: 0} 分"
    } else {
        "各计划分别判定"
    }
}
