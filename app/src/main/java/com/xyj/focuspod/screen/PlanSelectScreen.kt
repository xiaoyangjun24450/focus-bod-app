package com.xyj.focuspod.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xyj.focuspod.R
import com.xyj.focuspod.flow.StudyFlow
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaNavBar
import com.xyj.focuspod.ui.component.NenyaPlanCard
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/**
 * 页面 2 · 学习计划选择（Figma 66:338）。
 * 整个学习流程中唯一的触控页：可多选计划 → 合并成一次学习会话。
 */
@Composable
fun PlanSelectScreen(
    state: StudySessionState,
    flow: StudyFlow
) {
    var selectedIds by remember(state.availablePlans) {
        mutableStateOf(
            state.availablePlans.firstOrNull()?.let { setOf(it.id) } ?: emptySet()
        )
    }
    val selectedPlans = state.availablePlans.filter { it.id in selectedIds }
    val totalMinutes = selectedPlans.sumOf { it.estimatedMinutes }

    DesignCanvas {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NenyaColor.Bg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                Spacer(modifier = Modifier.height(56.dp))
                Text(
                    text = "选择学习计划",
                    style = NenyaText.PageTitle,
                    color = NenyaColor.Ink
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.studentName,
                        style = NenyaText.Compact,
                        color = NenyaColor.Muted,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wifi),
                            contentDescription = null,
                            tint = NenyaColor.Deep,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (state.networkOnline) "网络已连接" else "网络已断开",
                            style = NenyaText.Tiny,
                            color = NenyaColor.Deep
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SearchField()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "老师布置的计划",
                        style = NenyaText.BodyMedium,
                        color = NenyaColor.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "可多选",
                        style = NenyaText.Small,
                        color = NenyaColor.Muted
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (state.availablePlans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无学习计划，请联系老师",
                            style = NenyaText.Body,
                            color = NenyaColor.Muted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.availablePlans) { plan ->
                            NenyaPlanCard(
                                plan = plan,
                                selected = plan.id in selectedIds,
                                onClick = {
                                    selectedIds = if (plan.id in selectedIds) {
                                        selectedIds - plan.id
                                    } else {
                                        selectedIds + plan.id
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedPlans.isEmpty()) "未选择计划" else "已选 ${selectedPlans.size} 项计划",
                        style = NenyaText.Compact,
                        color = NenyaColor.Deep,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "预计 $totalMinutes 分钟",
                        style = NenyaText.Compact,
                        color = NenyaColor.Muted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            StartPlanButton(enabled = selectedPlans.isNotEmpty()) {
                flow.selectPlans(selectedPlans)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NenyaColor.Surface)
            ) {
                NenyaNavBar()
            }
            }
        }

        NenyaAlert(message = state.alertMessage)
    }
}

@Composable
private fun SearchField() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(NenyaColor.Surface)
            .border(1.dp, NenyaColor.Border, RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = NenyaColor.Deep,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "搜索题库、科目或知识点",
            style = NenyaText.Compact,
            color = NenyaColor.Muted
        )
    }
}

@Composable
private fun StartPlanButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) NenyaColor.Deep else NenyaColor.DisabledSurface)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "开始本计划",
            style = NenyaText.BodyMedium,
            color = if (enabled) NenyaColor.Surface else NenyaColor.Muted
        )
        Spacer(modifier = Modifier.size(12.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = if (enabled) NenyaColor.Surface else NenyaColor.Muted,
            modifier = Modifier.size(20.dp)
        )
    }
}
