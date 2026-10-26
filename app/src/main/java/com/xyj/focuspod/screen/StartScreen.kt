package com.xyj.focuspod.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.ui.component.DesignCanvas
import com.xyj.focuspod.ui.component.NenyaAlert
import com.xyj.focuspod.ui.component.NenyaPod
import com.xyj.focuspod.ui.theme.NenyaColor
import com.xyj.focuspod.ui.theme.NenyaText

/**
 * 页面 1 · 启动页（Figma 67:94）。
 * 芽绿主视觉背景 + 品牌名 + 手机舱伙伴 + 启动进度，无任何触控。
 */
@Composable
fun StartScreen(state: StudySessionState) {
    DesignCanvas {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .background(NenyaColor.Bg)
        ) {
        // 芽绿主视觉背景：540×604，圆角 200，左上角超出屏幕 64/102
        Box(
            modifier = Modifier
                .offset(x = (-64).dp, y = (-102).dp)
                .requiredSize(540.dp, 604.dp)
                .background(NenyaColor.Accent, RoundedCornerShape(200.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(102.dp))
            Text(
                text = "学习舱",
                style = NenyaText.Display,
                color = NenyaColor.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "放下手机，让专注发芽",
                style = NenyaText.Body,
                color = NenyaColor.Deep,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(60.dp))
            NenyaPod(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.73f)
                    .widthIn(max = 300.dp)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(78.dp))
            Text(
                text = state.voiceCaption.ifBlank { "正在启动，请稍候" },
                style = NenyaText.Body,
                color = NenyaColor.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            // 进度条在 3 秒内走到设计稿的 38/120，让等待可见而不是「突然跳转」。
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                progress.animateTo(
                    targetValue = 38f / 120f,
                    animationSpec = tween(durationMillis = START_DURATION_MS, easing = LinearEasing)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(120.dp, 6.dp)
                    .background(NenyaColor.Soft, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(6.dp)
                        .background(NenyaColor.Deep, RoundedCornerShape(3.dp))
                )
            }
        }

        NenyaAlert(message = state.alertMessage)
        }
    }
}

/** 与 StudyFlow.START_DELAY_MS 对齐的启动页停留时长。 */
private const val START_DURATION_MS = 3_000
