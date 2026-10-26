package com.xyj.focuspod.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xyj.focuspod.R
import com.xyj.focuspod.model.DeviceCheckItem
import com.xyj.focuspod.model.DeviceCheckStatus
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaCheckRow
import com.xyj.focuspod.ui.component.NenyaPod
import com.xyj.focuspod.ui.component.NenyaProgressSegments
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/**
 * 页面 3 · 设备自检（Figma 67:154）。
 * 锁仓后不提供触控：自检进度、逐项结果、语音播报全部自动推进。
 */
@Composable
fun DeviceSelfCheckScreen(state: StudySessionState) {
    val checks = state.deviceChecks
    val passedCount = checks.count { it.status == DeviceCheckStatus.PASSED }
    val total = if (checks.isEmpty()) 5 else checks.size
    val checking = checks.firstOrNull { it.status == DeviceCheckStatus.CHECKING }
    val allPassed = checks.isNotEmpty() && passedCount == checks.size
    val headline = when {
        allPassed -> "全部检查通过"
        checking != null -> checkingHeadline(checkLabel(checking.name))
        else -> "设备检查完成"
    }
    val activeItem = checking?.name

    DesignCanvas {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NenyaColor.Bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "设备自检中",
                        style = NenyaText.PageTitle,
                        color = NenyaColor.Ink
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "请勿触摸手机",
                        style = NenyaText.SectionTitle,
                        color = NenyaColor.Deep
                    )
                }
                NenyaPod(modifier = Modifier.size(72.dp))
            }
            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NenyaColor.Soft)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = headline,
                    style = NenyaText.SectionTitle,
                    color = NenyaColor.Ink
                )
                Text(
                    text = "$passedCount / $total 项已通过",
                    style = NenyaText.Compact,
                    color = NenyaColor.Deep
                )
                Box(modifier = Modifier.height(2.dp))
                NenyaProgressSegments(checks = checks)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NenyaColor.Surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (checks.isEmpty()) {
                    repeat(5) { index ->
                        NenyaCheckRow(
                            item = DeviceCheckItem(name = defaultCheckName(index), status = DeviceCheckStatus.CHECKING),
                            note = noteFor(defaultCheckName(index)),
                            active = index == 0
                        )
                    }
                } else {
                    checks.forEach { item ->
                        NenyaCheckRow(
                            item = item,
                            note = noteFor(item.name),
                            active = item.name == activeItem
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NenyaColor.Surface)
                    .border(1.dp, NenyaColor.Border, RoundedCornerShape(24.dp))
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_voice),
                        contentDescription = null,
                        tint = NenyaColor.Deep,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (state.voiceCaption.isBlank()) "语音待命" else "语音播报中",
                        style = NenyaText.BodyMedium,
                        color = NenyaColor.Deep
                    )
                }
                Text(
                    text = state.voiceCaption.ifBlank { "请确认手机仓 USB 已连接" },
                    style = NenyaText.Body,
                    color = NenyaColor.Ink
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_waveform),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp, 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "全部通过后，将自动进入辅导",
                style = NenyaText.Tiny,
                color = NenyaColor.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        NenyaAlert(message = state.alertMessage)
        }
    }
}

private fun checkLabel(name: String): String = if (name.contains("USB")) "USB" else name

private fun checkingHeadline(label: String): String =
    if (label.all { it.code < 128 }) "正在检查 $label 连接" else "正在检查${label}连接"

private fun defaultCheckName(index: Int): String = listOf(
    "相机",
    "麦克风",
    "网络",
    "姿态传感器",
    "USB 手机仓"
)[index.coerceIn(0, 4)]

private fun noteFor(name: String): String = when {
    name.contains("相机") -> "用于扫描试卷"
    name.contains("麦克风") -> "语音输入正常"
    name.contains("网络") -> "连接稳定"
    name.contains("姿态") -> "支持竖放与横放"
    name.contains("USB") -> "正在确认连接"
    else -> ""
}
