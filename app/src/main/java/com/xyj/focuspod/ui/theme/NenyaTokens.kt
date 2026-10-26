package com.xyj.focuspod.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 「嫩芽」设计语言令牌，取自 Figma「给MY的UI / 三页主流程」。
 * 仅三页主流程使用；其余页面继续使用 Focus* 旧令牌。
 */
object NenyaColor {
    val Bg = Color(0xFFF7FAF2)
    val Ink = Color(0xFF233B2B)
    val Deep = Color(0xFF315D36)
    val Muted = Color(0xFF62705E)
    val Border = Color(0xFFD6DFCB)
    val Soft = Color(0xFFE6EEDC)
    val Pale = Color(0xFFEDF6DE)
    val Accent = Color(0xFFBFEA70)
    val Surface = Color(0xFFFFFFFF)
    val DisabledSurface = Color(0xFFE0E5DA)
    val Danger = Color(0xFFB4472B)
    val CheckboxOutline = Color(0xFF49454F)
}

object NenyaText {
    val Display = TextStyle(fontSize = 32.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
    val PageTitle = TextStyle(fontSize = 28.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
    val SectionTitle = TextStyle(fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
    val PlanName = TextStyle(fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
    val Body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val BodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
    val Compact = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val Small = TextStyle(fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
    val Tiny = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    val TinyMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
}

/** 三页共用的横向内容边距。 */
val NenyaGutter = 24
