package com.xyj.focuspod.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.xyj.focuspod.R
import com.xyj.focuspod.model.DeviceCheckItem
import com.xyj.focuspod.model.DeviceCheckStatus
import com.xyj.focuspod.model.StudyPlan
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/** 「嫩芽」手机舱伙伴插画。 */
@Composable
fun NenyaPod(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.pod_companion),
        contentDescription = null,
        modifier = modifier
    )
}

/**
 * 把设计画布等比缩放到屏幕后绘制。默认竖屏 412×892；批卷页用横屏 892×412。
 * 画布铺满整屏（含状态栏与手势区），页面内部按设计稿坐标留出安全间距。
 * 只缩不放：屏幕大于设计稿时保持 1:1。
 */
@Composable
fun DesignCanvas(
    width: Float = DESIGN_WIDTH,
    height: Float = DESIGN_HEIGHT,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NenyaColor.Bg),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val scale = minOf(
                1f,
                constraints.maxWidth / (width * density.density),
                constraints.maxHeight / (height * density.density)
            )
            CompositionLocalProvider(
                LocalDensity provides Density(density.density * scale, density.fontScale)
            ) {
                Box(modifier = Modifier.size(width.dp, height.dp)) {
                    content()
                }
            }
        }
    }
}

private const val DESIGN_WIDTH = 412f
private const val DESIGN_HEIGHT = 892f

/** 计划卡片右上角的勾选态。 */
@Composable
fun NenyaCheckbox(checked: Boolean) {
    val shape = RoundedCornerShape(2.dp)
    if (checked) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(NenyaColor.Deep, shape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_check_white),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(2.dp, NenyaColor.CheckboxOutline, shape)
        )
    }
}

/** 计划卡片：选中为浅绿底 + 深绿描边，未选中为白底 + 浅描边。 */
@Composable
fun NenyaPlanCard(
    plan: StudyPlan,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val background = if (selected) NenyaColor.Pale else NenyaColor.Surface
    val borderColor = if (selected) NenyaColor.Deep else NenyaColor.Border
    val borderWidth = if (selected) 2.dp else 1.dp
    val questionCount = plan.exampleQuestions.size + plan.examQuestions.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = plan.subject,
                    style = NenyaText.Small,
                    color = NenyaColor.Muted
                )
                Text(
                    text = plan.title,
                    style = NenyaText.PlanName,
                    color = NenyaColor.Ink
                )
            }
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                NenyaCheckbox(checked = selected)
            }
        }
        Row(
            modifier = Modifier.heightIn(min = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlanMetric(text = "约 ${plan.estimatedMinutes} 分钟", modifier = Modifier.weight(1f))
            PlanMetric(text = "$questionCount 道题", modifier = Modifier.weight(1f))
            PlanMetric(
                text = "${plan.passScore} 分达标",
                color = NenyaColor.Deep,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlanMetric(
    text: String,
    color: Color = NenyaColor.Muted,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = NenyaText.BodyMedium,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}

/** 自检进度：五段式进度条。 */
@Composable
fun NenyaProgressSegments(checks: List<DeviceCheckItem>) {
    val segments = if (checks.isEmpty()) 5 else checks.size
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(segments) { index ->
            val passed = checks.getOrNull(index)?.status == DeviceCheckStatus.PASSED
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = if (passed) NenyaColor.Deep else NenyaColor.Surface,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/** 自检项目行：图标 + 名称/说明 + 结果，检查中的行整行高亮。 */
@Composable
fun NenyaCheckRow(item: DeviceCheckItem, note: String, active: Boolean) {
    val passed = item.status == DeviceCheckStatus.PASSED
    val failed = item.status == DeviceCheckStatus.FAILED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) NenyaColor.Accent else Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = iconForCheck(item.name)),
            contentDescription = null,
            tint = NenyaColor.Deep,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = item.name, style = NenyaText.BodyMedium, color = NenyaColor.Ink)
            Text(text = note, style = NenyaText.Tiny, color = NenyaColor.Muted)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (passed || failed) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = if (failed) NenyaColor.Danger else NenyaColor.Deep,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_pending),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = when {
                    passed -> "已通过"
                    failed -> "未通过"
                    else -> "检查中"
                },
                style = NenyaText.Tiny,
                color = if (failed) NenyaColor.Danger else NenyaColor.Deep
            )
        }
    }
}

private fun iconForCheck(name: String): Int = when {
    name.contains("相机") -> R.drawable.ic_camera
    name.contains("麦克风") -> R.drawable.ic_mic
    name.contains("网络") -> R.drawable.ic_wifi_signal
    name.contains("姿态") -> R.drawable.ic_posture
    name.contains("USB") -> R.drawable.ic_usb
    else -> R.drawable.ic_wifi_signal
}

/** 底部导航（设计稿中的全局导航，当前流程内为静态展示）。 */
@Composable
fun NenyaNavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(NenyaColor.Surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NavItem(icon = R.drawable.ic_nav_book, label = "学习", active = true, modifier = Modifier.weight(1f))
        NavItem(icon = R.drawable.ic_nav_grid, label = "题库", active = false, modifier = Modifier.weight(1f))
        NavItem(icon = R.drawable.ic_nav_chart, label = "记录", active = false, modifier = Modifier.weight(1f))
        NavItem(icon = R.drawable.ic_nav_user, label = "我的", active = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NavItem(icon: Int, label: String, active: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.height(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .background(
                    color = if (active) NenyaColor.Accent else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = if (active) NenyaColor.Ink else NenyaColor.Muted,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = NenyaText.Tiny,
            color = if (active) NenyaColor.Ink else NenyaColor.Muted,
            textAlign = TextAlign.Center
        )
    }
}

/** 当前页面的提示层，替代独立异常页。 */
@Composable
fun NenyaAlert(message: String?) {
    if (message.isNullOrBlank()) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x6B000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NenyaColor.Surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "提示", style = NenyaText.SectionTitle, color = NenyaColor.Ink)
            Text(text = message, style = NenyaText.Body, color = NenyaColor.Muted)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
