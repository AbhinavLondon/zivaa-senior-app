import re

with open("app/src/main/java/com/zivaa/app/presentation/dashboard/DashboardScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace activeHeroIndex with activeHeroPeriod
content = content.replace("var activeHeroIndex by remember { mutableIntStateOf(0) }", 'var activeHeroPeriod by remember { mutableStateOf("morning") }')
content = content.replace("LaunchedEffect(activeHeroIndex) {", "LaunchedEffect(activeHeroPeriod) {")
content = content.replace("if (activeHeroIndex == 1) {", 'if (activeHeroPeriod == "afternoon") {')
content = content.replace("} else if (activeHeroIndex == 2) {", '} else if (activeHeroPeriod == "evening") {')
content = content.replace("activeHeroIndex = 2", 'activeHeroPeriod = "evening"')
content = content.replace("activeHeroIndex = 1", 'activeHeroPeriod = "afternoon"')

content = content.replace("""                    ) ?: HeroCard(
                        viewModel = viewModel,
                        activeIndex = activeHeroIndex,
                        onIndexChange = { activeHeroIndex = it }
                    )""", """                    ) ?: HeroCard(
                        viewModel = viewModel,
                        activePeriod = activeHeroPeriod,
                        onPeriodChange = { activeHeroPeriod = it }
                    )""")


start_marker = "@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)\n@Composable\nfun HeroCard("
end_marker = "@Composable\nfun SunCloudAnimation("

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    old_hero_card = content[start_idx:end_idx]
    
    new_hero_card = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroCard(
    viewModel: DashboardViewModel,
    activePeriod: String = "morning",
    onPeriodChange: (String) -> Unit = {}
) {
    val availablePeriods = buildList {
        add("morning")
        if (viewModel.middaySummaryText != null) add("afternoon")
        if (viewModel.eveningSummaryText != null) add("evening")
    }
    val totalDots = availablePeriods.size
    
    val initialPage = maxOf(0, availablePeriods.indexOf(activePeriod))
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalDots }
    )

    LaunchedEffect(activePeriod, availablePeriods) {
        val targetPage = availablePeriods.indexOf(activePeriod)
        if (targetPage != -1 && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage, availablePeriods) {
        val currentPeriod = availablePeriods.getOrNull(pagerState.currentPage)
        if (currentPeriod != null && currentPeriod != activePeriod) {
            onPeriodChange(currentPeriod)
        }
    }

    val pageOffset = pagerState.currentPageOffsetFraction
    val page = pagerState.currentPage

    val surfaceHeroColor = ZivaaTheme.colors.surfaceHero
    val sageColor = ZivaaTheme.colors.sage

    fun colorForPage(p: Int) = when (availablePeriods.getOrNull(p)) {
        "afternoon" -> Color(0xFFA16B40) // Afternoon warm clay
        "evening" -> Color(0xFF2C3E50) // Evening dark blue/grey
        else -> surfaceHeroColor // Morning sage
    }

    fun shadowColorForPage(p: Int) = when (availablePeriods.getOrNull(p)) {
        "afternoon" -> Color(0xFFA16B40).copy(alpha = 0.16f)
        "evening" -> Color(0xFF2C3E50).copy(alpha = 0.16f)
        else -> sageColor.copy(alpha = 0.16f)
    }

    val targetPageOffset = if (pageOffset > 0) 1 else if (pageOffset < 0) -1 else 0
    val currentBg = colorForPage(page)
    val nextBg = colorForPage(page + targetPageOffset)
    val fraction = kotlin.math.abs(pageOffset)
    
    val backgroundColor = androidx.compose.ui.graphics.lerp(currentBg, nextBg, fraction)
    
    val currentShadow = shadowColorForPage(page)
    val nextShadow = shadowColorForPage(page + targetPageOffset)
    val ambientShadowColor = androidx.compose.ui.graphics.lerp(currentShadow, nextShadow, fraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ambientShadowColor,
                spotColor = ambientShadowColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .animateContentSize()
    ) {
        // Radial Gradient background effect
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-50).dp)
                .size(200.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ZivaaTheme.colors.sageInk.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 200f
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                val period = availablePeriods.getOrElse(pageIndex) { "morning" }
                HeroCardContent(
                    viewModel = viewModel,
                    period = period
                )
            }
            
            // Pagination Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
            ) {
                for (i in 0 until totalDots) {
                    val weight = when (i) {
                        page -> 1f - fraction
                        page + targetPageOffset -> fraction
                        else -> 0f
                    }
                    val dotColor = androidx.compose.ui.graphics.lerp(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.9f),
                        weight
                    )
                    val dotWidth = androidx.compose.ui.unit.lerp(
                        6.dp, 
                        18.dp, 
                        weight
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(999.dp))
                            .background(dotColor)
                            .clickable {
                                val clickedPeriod = availablePeriods.getOrNull(i)
                                if (clickedPeriod != null) {
                                    onPeriodChange(clickedPeriod)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCardContent(
    viewModel: DashboardViewModel,
    period: String
) {
    val colors = ZivaaTheme.colors
    
    val eyebrowText = when (period) {
        "afternoon" -> "AFTERNOON CHECK-IN"
        "evening" -> "EVENING CHECK-IN"
        else -> "ALL WELL TODAY"
    }
    
    val eyebrowDotColor = when (period) {
        "afternoon" -> Color(0xFFD6A35A) // Amber/Yellowish
        "evening" -> Color(0xFF8AA676) // Leaf
        else -> colors.leaf.copy(alpha = 0.3f)
    }

    val patientName = viewModel.patientFirstName.ifEmpty { "Patient" }
    val cleanMorningHeadline = viewModel.morningBriefingHeadline.removeSuffix(".").removeSuffix(", Ranjit").removeSuffix(" Ranjit").removeSuffix(", $patientName").removeSuffix(" $patientName").trim()
    val morningHeadline = if (cleanMorningHeadline.isNotEmpty()) cleanMorningHeadline else "A bright, active\nday"
    
    val headlineText = when (period) {
        "afternoon" -> "A gentle,\nsteady afternoon"
        "evening" -> "A peaceful,\nquiet evening"
        else -> morningHeadline
    }

    val bodyText = when (period) {
        "afternoon" -> viewModel.middaySummaryText ?: "Lunch was on time and you rested well after. The garden got its walk-through, and the 2 o'clock rest actually happened. The evening walk is next — the light is lovely right now."
        "evening" -> viewModel.eveningSummaryText ?: "The evening was calm and you had dinner on time. It's time to rest now."
        else -> if (viewModel.morningBriefingText.isNotEmpty()) viewModel.morningBriefingText else "You moved well, ate on time and stayed cheerful. You kept busy in the garden through the afternoon — nothing today needs a second thought."
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (period == "morning") {
            MorningSunAnimation()
        } else if (period == "afternoon") {
            SunCloudAnimation()
        } else if (period == "evening") {
            MoonStarAnimation()
        }
        
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)) {
            // Eyebrow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.14f))
                    .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(eyebrowDotColor) 
                            .border(4.dp, eyebrowDotColor.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                    )
                    Text(
                        text = eyebrowText,
                        style = ZivaaTheme.typography.meta,
                        color = ZivaaTheme.colors.sageInk.copy(alpha = 0.78f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("$headlineText,\n")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(patientName)
                    }
                    append(".")
                },
                style = ZivaaTheme.typography.displayLarge.copy(fontSize = 35.sp, lineHeight = (35 * 1.1).sp, letterSpacing = (-0.012).em),
                color = ZivaaTheme.colors.sageInk
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = bodyText,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    fontSize = 16.5.sp, 
                    lineHeight = (16.5 * 1.45).sp, 
                    fontWeight = FontWeight.Medium
                ),
                color = ZivaaTheme.colors.sageInk
            )
        }
    }
}
"""

    new_content = content.replace(old_hero_card, new_hero_card)
    
    with open("app/src/main/java/com/zivaa/app/presentation/dashboard/DashboardScreen.kt", "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Replaced successfully")
else:
    print("Could not find markers")
