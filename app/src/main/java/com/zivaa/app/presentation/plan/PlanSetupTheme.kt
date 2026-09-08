package com.zivaa.app.presentation.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zivaa.app.ui.theme.LocalZivaaColors

data class PlanSetupTone(val bg: Color, val fg: Color)

object PlanSetupTones {
    val Amber @Composable get() = PlanSetupTone(LocalZivaaColors.current.amber, Color.White)
    val Leaf @Composable get() = PlanSetupTone(LocalZivaaColors.current.leaf, Color.White)
    val Clay @Composable get() = PlanSetupTone(LocalZivaaColors.current.clay, Color.White)
    val Sage @Composable get() = PlanSetupTone(LocalZivaaColors.current.sage, LocalZivaaColors.current.sageInk)
    val Ink @Composable get() = PlanSetupTone(LocalZivaaColors.current.ink, LocalZivaaColors.current.bg)
}

object PlanSetupTheme {
    val Ink @Composable get() = LocalZivaaColors.current.ink
    val InkSoft @Composable get() = LocalZivaaColors.current.inkSoft
    val InkMute @Composable get() = LocalZivaaColors.current.inkMute
    val Eyebrow @Composable get() = LocalZivaaColors.current.eyebrow
    
    val Bg @Composable get() = LocalZivaaColors.current.bg
    val BgElev @Composable get() = LocalZivaaColors.current.bgElev
    val SurfaceHero @Composable get() = LocalZivaaColors.current.surfaceHero
    
    val Line @Composable get() = LocalZivaaColors.current.line
    val LineStrong @Composable get() = LocalZivaaColors.current.lineStrong
    
    val Sage @Composable get() = LocalZivaaColors.current.sage
    val SageInk @Composable get() = LocalZivaaColors.current.sageInk
    
    val Amber @Composable get() = LocalZivaaColors.current.amber
    val Leaf @Composable get() = LocalZivaaColors.current.leaf
    val Clay @Composable get() = LocalZivaaColors.current.clay
}
