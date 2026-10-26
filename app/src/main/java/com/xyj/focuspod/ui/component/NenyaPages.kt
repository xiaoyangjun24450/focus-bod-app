package com.xyj.focuspod.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xyj.focuspod.R
import com.xyj.focuspod.model.Question
import com.xyj.focuspod.model.TutoringMessage
import com.xyj.focuspod.model.TutoringSpeaker
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/** 学习阶段栏：左侧阶段名，右侧剩余题数（Figma 04/05/07/08 通用）。 */
@Composable
fun StageBar(stageLabel: String, remaining: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stageLabel,
            style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            color = NenyaColor.Ink
        )
        Text(
            text = "剩余 $remaining 题",
            style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            color = NenyaColor.Deep
        )
    }
}

/** 题干卡：题号 + 大字题干。 */
@Composable
fun QuestionPanel(
    questionNumber: Int,
    question: Question?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NenyaColor.Surface)
            .border(1.dp, NenyaColor.Border, RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "第${questionNumber}题",
            style = NenyaText.SectionTitle,
            color = NenyaColor.Deep
        )
        MarkdownText(
            markdown = question?.questionMarkdown.orEmpty().ifBlank { "等待题目" },
            style = TextStyle(fontSize = 26.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium),
            color = NenyaColor.Ink
        )
    }
}

/** AI 讲解会话卡：AI 白气泡靠左，学生芽绿气泡靠右。 */
@Composable
fun AiConversationCard(messages: List<TutoringMessage>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NenyaColor.Pale)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_voice),
                contentDescription = null,
                tint = NenyaColor.Deep,
                modifier = Modifier.size(16.dp)
            )
            Text(text = "AI 讲解", style = NenyaText.Small, color = NenyaColor.Deep)
        }
        val shown = messages.takeLast(3)
        if (shown.isEmpty()) {
            Bubble(text = "正在准备讲解，请听语音提示。", fromAi = true)
        } else {
            shown.forEach { message ->
                Bubble(text = message.text, fromAi = message.speaker == TutoringSpeaker.AI)
            }
        }
    }
}

@Composable
private fun Bubble(text: String, fromAi: Boolean) {
    val fromAi2 = fromAi
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromAi2) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (fromAi2) NenyaColor.Surface else NenyaColor.Accent)
                .padding(14.dp)
        ) {
            Text(text = text, style = NenyaText.Body, color = NenyaColor.Ink)
        }
    }
}

/** 底部翻转提示。 */
@Composable
fun FlipHint(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_posture),
            contentDescription = null,
            tint = NenyaColor.Deep,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = text,
            style = NenyaText.SectionTitle,
            color = NenyaColor.Deep
        )
    }
}

/** 08 成绩页的汇总行：左标签右数值。 */
@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = 22.sp, lineHeight = 34.sp),
            color = NenyaColor.Muted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            color = NenyaColor.Ink
        )
    }
}

/** 多计划时的单行成绩：计划名 + 得分 + 达标状态。 */
@Composable
fun PlanScoreRow(title: String, score: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 22.sp, lineHeight = 34.sp),
            color = NenyaColor.Muted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = score,
            style = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            color = if (passed) NenyaColor.Deep else NenyaColor.Danger
        )
        Spacer(modifier = Modifier.size(6.dp))
        Icon(
            painter = painterResource(
                id = if (passed) R.drawable.ic_check else R.drawable.ic_close
            ),
            contentDescription = null,
            tint = if (passed) NenyaColor.Deep else NenyaColor.Danger,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** 竖直弹簧占位，把内容压到设计稿位置。 */
@Composable
fun VSpace(dp: Int) {
    Spacer(modifier = Modifier.height(dp.dp))
}
