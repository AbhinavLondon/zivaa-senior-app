package com.zivaa.app.util.pdf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.zivaa.app.ui.labs.report.LabReportState
import com.zivaa.app.ui.labs.summary.BiomarkerUiModel
import com.zivaa.app.ui.labs.summary.LabSummaryState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LabReportPdfGenerator {

    private const val TAG = "LabReportPdfGenerator"

    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 559f
    private const val USABLE_WIDTH = MARGIN_RIGHT - MARGIN_LEFT // 523f
    private const val MARGIN_TOP = 36f
    private const val MARGIN_BOTTOM = 806f
    private const val FOOTER_HEIGHT = 54f
    private const val CONTENT_BOTTOM = MARGIN_BOTTOM - FOOTER_HEIGHT // 752f

    // Zivaa Brand Palette
    private val COLOR_BRAND_DARK = Color.parseColor("#1B3322")     // Deep Forest Sage
    private val COLOR_BRAND_SAGE = Color.parseColor("#34533C")     // Primary Zivaa Sage
    private val COLOR_BRAND_BG = Color.parseColor("#F9FAF8")       // Clean Warm Background
    private val COLOR_SOFT_SAGE = Color.parseColor("#EBF3EE")      // Light Sage Callout
    private val COLOR_SAGE_BORDER = Color.parseColor("#CADBD0")    // Delicate Sage Border
    private val COLOR_INK = Color.parseColor("#111827")            // Primary Text Ink
    private val COLOR_BODY = Color.parseColor("#374151")           // Body Secondary
    private val COLOR_META = Color.parseColor("#6B7280")           // Muted Meta Text
    private val COLOR_BORDER = Color.parseColor("#E5E7EB")         // Card Border
    private val COLOR_IN_RANGE = Color.parseColor("#2D7A4D")       // Healthy Green
    private val COLOR_IN_RANGE_BG = Color.parseColor("#ECFDF3")    // Light Green Badge
    private val COLOR_OUT_RANGE = Color.parseColor("#C25E38")      // Out of Range Terracotta
    private val COLOR_OUT_RANGE_BG = Color.parseColor("#FFF4ED")   // Light Amber Badge

    private const val DISCLAIMER_TEXT =
        "Notice: This document is an AI-interpreted clinical synthesis of diagnostic laboratory observations designed for patient convenience. AI has been used to interpret actual lab report. This document does not constitute an official diagnostic medical report. Please share these findings with your qualified physician for formal clinical diagnosis and medical decisions."

    data class ReportHeaderInfo(
        val documentTitle: String,
        val subtitle: String,
        val patientName: String,
        val performer: String,
        val date: String,
        val reportId: String
    )

    // ==========================================
    // 1. CATEGORY BIOMARKER SUMMARY PDF
    // ==========================================
    fun generateAndShareCategoryPdf(
        context: Context,
        state: LabSummaryState,
        selectedFilter: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cleanTitle = cleanMarkdownForPdf(state.title).ifBlank { "Category Summary" }
                val safeFileName = cleanTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
                val file = File(context.cacheDir, "Zivaa_${safeFileName}_Summary.pdf")
                file.parentFile?.mkdirs()

                val headerInfo = ReportHeaderInfo(
                    documentTitle = cleanTitle,
                    subtitle = "Zivaa Clinical Biomarker Overview",
                    patientName = cleanMarkdownForPdf(state.patientName).ifBlank { "Patient" },
                    performer = cleanMarkdownForPdf(state.performer).ifBlank { "Diagnostic Laboratory" },
                    date = if (state.formattedDate.isNotBlank()) state.formattedDate else SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()),
                    reportId = state.reportId.take(8).uppercase(Locale.US)
                )

                val filteredBiomarkers = when (selectedFilter) {
                    "good" -> state.biomarkers.filter { it.badgeTone == "good" }
                    "watch", "bad" -> state.biomarkers.filter { it.badgeTone != "good" }
                    else -> state.biomarkers
                }

                val pdfDocument = PdfDocument()
                val writer = PdfCanvasWriter(pdfDocument, headerInfo)

                writer.startNewPage()
                writer.drawHeader()

                // Stat highlights row on Page 1
                writer.drawStatBoxes(
                    goodCount = state.goodCount,
                    outCount = state.notSoGoodCount + state.badCount
                )

                // Plain English category summary (if available, flows if long)
                val summary = cleanMarkdownForPdf(state.categorySummary)
                if (summary.isNotBlank()) {
                    writer.drawSummaryCallout(
                        title = "ZIVAA AI CLINICAL SUMMARY",
                        content = summary
                    )
                }

                // Section Title
                writer.drawSectionTitle("Biomarker Details (${filteredBiomarkers.size} analyzed)")

                // Render each biomarker card or graceful empty state
                if (filteredBiomarkers.isEmpty()) {
                    writer.drawEmptyNotice("No biomarkers match the selected criteria for this report.")
                } else {
                    for (biomarker in filteredBiomarkers) {
                        writer.drawBiomarkerCard(biomarker)
                    }
                }

                writer.finish()

                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    sharePdfFile(context, file, "${headerInfo.documentTitle} - Zivaa Summary")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to generate category PDF", t)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Could not share PDF: ${t.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ==========================================
    // 2. COMPREHENSIVE FULL REPORT PDF
    // ==========================================
    fun generateAndShareFullReportPdf(
        context: Context,
        state: LabReportState,
        categorizedBiomarkers: Map<String, List<BiomarkerUiModel>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cleanDocTitle = cleanMarkdownForPdf(state.reportTitle).ifBlank { "Lab Report Overview" }
                val file = File(context.cacheDir, "Zivaa_Comprehensive_Lab_Report.pdf")
                file.parentFile?.mkdirs()

                val headerInfo = ReportHeaderInfo(
                    documentTitle = cleanDocTitle,
                    subtitle = "Comprehensive Diagnostic Lab Report Overview",
                    patientName = cleanMarkdownForPdf(state.patientName).ifBlank { "Patient" },
                    performer = cleanMarkdownForPdf(state.performer).ifBlank { "Diagnostic Laboratory" },
                    date = if (state.formattedDate.isNotBlank()) state.formattedDate else SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()),
                    reportId = state.reportId.take(8).uppercase(Locale.US)
                )

                val pdfDocument = PdfDocument()
                val writer = PdfCanvasWriter(pdfDocument, headerInfo)

                writer.startNewPage()
                writer.drawHeader()

                // Stat highlights placed directly under header on Page 1
                writer.drawStatBoxes(
                    goodCount = state.inRangeCount,
                    outCount = state.outOfRangeCount
                )

                // Executive Full Report Summary (flows cleanly across pages if long)
                val hero = cleanMarkdownForPdf(state.heroText)
                if (hero.isNotBlank()) {
                    writer.drawSummaryCallout(
                        title = "ZIVAA CLINICAL EXECUTIVE SUMMARY",
                        content = hero
                    )
                }

                val totalBiomarkers = categorizedBiomarkers.values.sumOf { it.size }
                if (totalBiomarkers == 0) {
                    writer.drawEmptyNotice("No specific biomarker observations were found in this diagnostic report.")
                } else {
                    // Organize and draw all categories
                    for ((categoryName, biomarkers) in categorizedBiomarkers) {
                        if (biomarkers.isEmpty()) continue

                        val outCount = biomarkers.count { it.badgeTone != "good" }
                        val inCount = biomarkers.size - outCount
                        val categorySub = if (outCount > 0) "$outCount OUT OF RANGE • $inCount IN RANGE" else "ALL $inCount IN RANGE"

                        writer.drawCategorySectionHeader(categoryName, categorySub)

                        for (biomarker in biomarkers) {
                            writer.drawBiomarkerCard(biomarker)
                        }
                    }
                }

                writer.finish()

                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    sharePdfFile(context, file, "${headerInfo.documentTitle} - Complete Lab Report")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to generate full report PDF", t)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Could not share PDF: ${t.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ==========================================
    // PDF CANVAS WRITER HELPER
    // ==========================================
    private class PdfCanvasWriter(
        private val document: PdfDocument,
        private val headerInfo: ReportHeaderInfo
    ) {
        private var currentPage: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var pageNumber = 0
        private var currentY = MARGIN_TOP

        // Dynamic property getter so we NEVER hold a stale Canvas pointer across page breaks
        private val activeCanvas: Canvas
            get() = canvas ?: throw IllegalStateException("Canvas is null. Page has not been started.")

        fun startNewPage() {
            if (currentPage != null) {
                drawFooter(canvas!!, pageNumber)
                document.finishPage(currentPage)
            }
            pageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            currentPage = document.startPage(pageInfo)
            canvas = currentPage!!.canvas

            // Page Background
            val bgPaint = Paint().apply { color = COLOR_BRAND_BG }
            activeCanvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

            currentY = MARGIN_TOP

            // If not page 1, draw running mini header
            if (pageNumber > 1) {
                drawRunningHeader()
            }
        }

        private fun ensureSpace(heightNeeded: Float) {
            val topY = if (pageNumber > 1) (MARGIN_TOP + 34f) else MARGIN_TOP
            // Only create a new page if we have already drawn something on this page
            if (currentY > topY && (currentY + heightNeeded) > CONTENT_BOTTOM) {
                startNewPage()
            }
        }

        fun drawHeader() {
            val bannerHeight = 98f

            // Top Header Banner
            val bannerRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerHeight)
            val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BRAND_DARK }
            activeCanvas.drawRoundRect(bannerRect, 12f, 12f, bannerPaint)

            // Brand Label (+30%: 9f -> 12f)
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#A8C5B2")
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.08f
            }
            activeCanvas.drawText("ZIVAA HEALTH · CLINICAL REPORT OVERVIEW", MARGIN_LEFT + 18f, currentY + 25f, brandPaint)

            // Title (+30%: 15f -> 19.5f)
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 19.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val titleText = if (headerInfo.documentTitle.length > 38) headerInfo.documentTitle.take(36) + "…" else headerInfo.documentTitle
            activeCanvas.drawText(titleText, MARGIN_LEFT + 18f, currentY + 52f, titlePaint)

            // Subtitle metadata line (+30%: 9.5f -> 12.5f)
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D1E0D7")
                textSize = 12.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val metaText = "Patient: ${headerInfo.patientName}   •   Lab: ${headerInfo.performer}   •   Date: ${headerInfo.date}"
            activeCanvas.drawText(metaText, MARGIN_LEFT + 18f, currentY + 78f, metaPaint)

            currentY += bannerHeight + 14f
        }

        private fun drawRunningHeader() {
            val runningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_META
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            activeCanvas.drawText("Zivaa Health • ${headerInfo.documentTitle} • Patient: ${headerInfo.patientName}", MARGIN_LEFT, currentY + 14f, runningPaint)

            val linePaint = Paint().apply {
                color = COLOR_BORDER
                strokeWidth = 0.75f
            }
            activeCanvas.drawLine(MARGIN_LEFT, currentY + 22f, MARGIN_RIGHT, currentY + 22f, linePaint)
            currentY += 34f
        }

        /**
         * Draws a clinical summary callout. If the summary is very long, it calculates
         * available space on the current page, draws as much text as cleanly fits,
         * and continues the remainder onto the next page with a "(CONTINUED)" heading.
         */
        fun drawSummaryCallout(title: String, content: String, isContinuation: Boolean = false) {
            val cleanContent = cleanMarkdownForPdf(content)
            if (cleanContent.isBlank()) return

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_INK
                textSize = 12.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }

            val contentWidth = (USABLE_WIDTH - 32f).toInt()
            val fullLayout = StaticLayout.Builder.obtain(cleanContent, 0, cleanContent.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f, 1.15f)
                .build()

            val topChrome = 34f // header tag + top padding
            val bottomChrome = 14f // bottom padding inside callout box
            val minContentHeightNeeded = topChrome + 24f + bottomChrome // header + at least 1 line + bottom padding

            // If remaining space on current page cannot even fit the header and 1 line, start a new page first
            if ((currentY + minContentHeightNeeded) > CONTENT_BOTTOM) {
                startNewPage()
            }

            val availableHeight = CONTENT_BOTTOM - currentY
            val availableTextHeight = availableHeight - (topChrome + bottomChrome)

            if (fullLayout.height <= availableTextHeight) {
                // Entire text fits on current page
                val boxHeight = topChrome + fullLayout.height + bottomChrome
                val bgRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + boxHeight)
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_SOFT_SAGE }
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_SAGE_BORDER
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                activeCanvas.drawRoundRect(bgRect, 10f, 10f, bgPaint)
                activeCanvas.drawRoundRect(bgRect, 10f, 10f, borderPaint)

                val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_BRAND_SAGE
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.05f
                }
                val displayTitle = if (isContinuation) "$title (CONTINUED)" else title
                activeCanvas.drawText(displayTitle, MARGIN_LEFT + 16f, currentY + 22f, tagPaint)

                activeCanvas.save()
                activeCanvas.translate(MARGIN_LEFT + 16f, currentY + 32f)
                fullLayout.draw(activeCanvas)
                activeCanvas.restore()

                currentY += boxHeight + 14f
            } else {
                // Find split line that fits within availableTextHeight
                var splitLine = 0
                for (line in 0 until fullLayout.lineCount) {
                    if (fullLayout.getLineBottom(line) <= availableTextHeight) {
                        splitLine = line + 1
                    } else {
                        break
                    }
                }
                // Ensure at least 1 line is included
                if (splitLine < 1) {
                    splitLine = 1
                }

                val splitCharIndex = fullLayout.getLineStart(splitLine)
                val thisPageText = cleanContent.substring(0, splitCharIndex).trimEnd()
                val remainingText = cleanContent.substring(splitCharIndex).trimStart()

                val thisPageLayout = StaticLayout.Builder.obtain(thisPageText, 0, thisPageText.length, textPaint, contentWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(3f, 1.15f)
                    .build()

                val boxHeight = topChrome + thisPageLayout.height + bottomChrome
                val bgRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + boxHeight)
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_SOFT_SAGE }
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_SAGE_BORDER
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                activeCanvas.drawRoundRect(bgRect, 10f, 10f, bgPaint)
                activeCanvas.drawRoundRect(bgRect, 10f, 10f, borderPaint)

                val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_BRAND_SAGE
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.05f
                }
                val displayTitle = if (isContinuation) "$title (CONTINUED)" else title
                activeCanvas.drawText(displayTitle, MARGIN_LEFT + 16f, currentY + 22f, tagPaint)

                activeCanvas.save()
                activeCanvas.translate(MARGIN_LEFT + 16f, currentY + 32f)
                thisPageLayout.draw(activeCanvas)
                activeCanvas.restore()

                currentY += boxHeight + 14f

                if (remainingText.isNotBlank()) {
                    startNewPage()
                    drawSummaryCallout(title, remainingText, isContinuation = true)
                }
            }
        }

        fun drawStatBoxes(goodCount: Int, outCount: Int) {
            val height = 48f
            ensureSpace(height + 14f)

            val gap = 12f
            val pillWidth = (USABLE_WIDTH - gap) / 2f

            // Left Pill: In Range
            val leftRect = RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + pillWidth, currentY + height)
            val leftBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_IN_RANGE_BG }
            val leftBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#A6F4C5")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            activeCanvas.drawRoundRect(leftRect, 9f, 9f, leftBgPaint)
            activeCanvas.drawRoundRect(leftRect, 9f, 9f, leftBorderPaint)

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_IN_RANGE }
            activeCanvas.drawCircle(MARGIN_LEFT + 18f, currentY + height / 2f, 4f, dotPaint)

            // In-range number (+30%: 12f -> 16f)
            val leftNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_IN_RANGE
                textSize = 16f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            // In-range label (+30%: 8.5f -> 11f)
            val leftLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_IN_RANGE
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.04f
            }
            activeCanvas.drawText("$goodCount", MARGIN_LEFT + 28f, currentY + 29f, leftNumPaint)
            activeCanvas.drawText("IN RANGE (LOOKING GOOD)", MARGIN_LEFT + 52f, currentY + 29f, leftLabelPaint)

            // Right Pill: Out of Range
            val rightLeft = MARGIN_LEFT + pillWidth + gap
            val rightRect = RectF(rightLeft, currentY, rightLeft + pillWidth, currentY + height)
            val rightBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_OUT_RANGE_BG }
            val rightBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FED7AA")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            activeCanvas.drawRoundRect(rightRect, 9f, 9f, rightBgPaint)
            activeCanvas.drawRoundRect(rightRect, 9f, 9f, rightBorderPaint)

            val rightDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_OUT_RANGE }
            activeCanvas.drawCircle(rightLeft + 18f, currentY + height / 2f, 4f, rightDotPaint)

            // Out-of-range number (+30%: 12f -> 16f)
            val rightNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_OUT_RANGE
                textSize = 16f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            // Out-of-range label (+30%: 8.5f -> 11f)
            val rightLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_OUT_RANGE
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.04f
            }
            activeCanvas.drawText("$outCount", rightLeft + 28f, currentY + 29f, rightNumPaint)
            activeCanvas.drawText("OUT OF RANGE (ATTENTION)", rightLeft + 52f, currentY + 29f, rightLabelPaint)

            currentY += height + 14f
        }

        fun drawSectionTitle(title: String) {
            ensureSpace(28f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_INK
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            activeCanvas.drawText(title, MARGIN_LEFT, currentY + 16f, paint)
            currentY += 24f
        }

        fun drawCategorySectionHeader(categoryName: String, statusText: String) {
            // Check for at least 80f so that category header is never orphaned at the bottom of a page
            ensureSpace(80f)

            // Clean Category Banner
            val bannerRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 30f)
            val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E6EDE8") }
            activeCanvas.drawRoundRect(bannerRect, 6f, 6f, bannerPaint)

            // Category title (+30%: 10f -> 13f)
            val catTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BRAND_DARK
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.04f
            }
            activeCanvas.drawText(categoryName.uppercase(Locale.US), MARGIN_LEFT + 14f, currentY + 20f, catTitlePaint)

            // Category status (+30%: 8.5f -> 11f)
            val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BRAND_SAGE
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val statusWidth = statusPaint.measureText(statusText)
            activeCanvas.drawText(statusText, MARGIN_RIGHT - 14f - statusWidth, currentY + 20f, statusPaint)

            currentY += 38f
        }

        fun drawBiomarkerCard(biomarker: BiomarkerUiModel) {
            // Compute insight layout (+30%: 8.5f -> 11f)
            var insightLayout: StaticLayout? = null
            var insightBoxHeight = 0f
            val cleanInsight = cleanMarkdownForPdf(biomarker.insight)
            if (cleanInsight.isNotBlank()) {
                val insightPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_BODY
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                val insightWidth = (USABLE_WIDTH - 28f).toInt()
                insightLayout = StaticLayout.Builder.obtain(cleanInsight, 0, cleanInsight.length, insightPaint, insightWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(2.5f, 1.15f)
                    .build()
                insightBoxHeight = insightLayout.height + 16f
            }

            val hasBar = biomarker.hasReferenceRange && biomarker.progress != null
            val barHeight = if (hasBar) 28f else 0f
            val insightHeight = if (insightLayout != null) (insightBoxHeight + 8f) else 0f
            val cardHeight = 44f + barHeight + insightHeight

            ensureSpace(cardHeight + 10f)

            // Card background & hairline border
            val cardRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + cardHeight)
            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BORDER
                style = Paint.Style.STROKE
                strokeWidth = 0.85f
            }
            activeCanvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
            activeCanvas.drawRoundRect(cardRect, 8f, 8f, borderPaint)

            val contentX = MARGIN_LEFT + 14f

            // Tag geometry
            val isGood = biomarker.badgeTone == "good"
            val toneColor = if (isGood) COLOR_IN_RANGE else COLOR_OUT_RANGE
            val toneBgColor = if (isGood) COLOR_IN_RANGE_BG else COLOR_OUT_RANGE_BG

            val tagText = if (isGood) "WITHIN RANGE" else "OUT OF RANGE"
            val tagTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = toneColor
                textSize = 10f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            val tagWidth = tagTextPaint.measureText(tagText) + 20f
            val tagHeight = 20f
            val tagRight = MARGIN_RIGHT - 14f
            val tagLeft = tagRight - tagWidth
            val tagTop = currentY + 11f
            val tagBottom = tagTop + tagHeight
            val tagCenterY = (tagTop + tagBottom) / 2f

            val tagRect = RectF(tagLeft, tagTop, tagRight, tagBottom)
            val tagBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = toneBgColor }
            val tagBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = toneColor.let { Color.argb(60, Color.red(it), Color.green(it), Color.blue(it)) }
                style = Paint.Style.STROKE
                strokeWidth = 0.85f
            }
            activeCanvas.drawRoundRect(tagRect, 5f, 5f, tagBgPaint)
            activeCanvas.drawRoundRect(tagRect, 5f, 5f, tagBorderPaint)

            // Dot and text inside tag
            val tagDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = toneColor }
            activeCanvas.drawCircle(tagLeft + 8f, tagCenterY, 3f, tagDotPaint)
            activeCanvas.drawText(tagText, tagLeft + 15f, tagCenterY + 3.5f, tagTextPaint)

            // Common text baseline for top row aligned with tag center
            val textBaselineY = tagCenterY + 4.5f

            // Biomarker Name (+30%: 10.5f -> 14f)
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_INK
                textSize = 14f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val headline = biomarker.headline.ifBlank { "Biomarker" }
            val truncatedHeadline = if (headline.length > 36) headline.take(34) + "…" else headline
            activeCanvas.drawText(truncatedHeadline, contentX, textBaselineY, namePaint)

            // Value & Unit placed to the left of the tag
            val valueText = biomarker.value.ifBlank { "--" }
            val unitText = biomarker.unit
            val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_INK
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_META
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val valStr = "$valueText "
            val valWidth = valPaint.measureText(valStr)
            val unitWidth = unitPaint.measureText(unitText)
            val valRight = tagLeft - 14f
            val valStartX = valRight - (valWidth + unitWidth)

            activeCanvas.drawText(valStr, valStartX, textBaselineY, valPaint)
            activeCanvas.drawText(unitText, valStartX + valWidth, textBaselineY, unitPaint)

            var localY = tagBottom + 6f

            // Graphical Progress Bar (if reference range exists)
            if (hasBar) {
                localY += 12f

                // Reference Range text on left (+30%: 7.5f -> 10f)
                val rangeLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = COLOR_META
                    textSize = 10f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                val rangeStr = if (biomarker.rangeText.isNotBlank()) "Target: ${biomarker.rangeText}" else "Target Range"
                activeCanvas.drawText(rangeStr, contentX, localY, rangeLabelPaint)

                // Progress Track on the right
                val trackWidth = 145f
                val trackHeight = 6f
                val trackRight = MARGIN_RIGHT - 14f
                val trackLeft = trackRight - trackWidth
                val trackTop = localY - 7f
                val trackRect = RectF(trackLeft, trackTop, trackRight, trackTop + trackHeight)

                val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E5E7EB") }
                activeCanvas.drawRoundRect(trackRect, 3f, 3f, trackPaint)

                // Healthy Range Interval in Green
                val startProgress = biomarker.rangeStartProgress ?: 0.2f
                val endProgress = biomarker.rangeEndProgress ?: 0.8f
                val greenLeft = trackLeft + (trackWidth * startProgress).coerceIn(0f, trackWidth)
                val greenRight = trackLeft + (trackWidth * endProgress).coerceIn(0f, trackWidth)
                if (greenRight > greenLeft) {
                    val greenRect = RectF(greenLeft, trackTop, greenRight, trackTop + trackHeight)
                    val greenBandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#86EFAC") }
                    activeCanvas.drawRect(greenRect, greenBandPaint)
                }

                // Patient thumb marker
                val p = biomarker.progress ?: 0.5f
                val thumbX = (trackLeft + trackWidth * p).coerceIn(trackLeft + 4f, trackRight - 4f)
                val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = toneColor }
                val thumbBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                activeCanvas.drawCircle(thumbX, trackTop + trackHeight / 2f, 4.5f, thumbPaint)
                activeCanvas.drawCircle(thumbX, trackTop + trackHeight / 2f, 4.5f, thumbBorderPaint)
            }

            // Clinical Insight Box
            if (insightLayout != null) {
                localY += 10f
                val insightRect = RectF(contentX, localY, MARGIN_RIGHT - 14f, localY + insightBoxHeight)
                val insightBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F4F6F4") }
                activeCanvas.drawRoundRect(insightRect, 6f, 6f, insightBgPaint)

                activeCanvas.save()
                activeCanvas.translate(contentX + 10f, localY + 8f)
                insightLayout.draw(activeCanvas)
                activeCanvas.restore()
            }

            currentY += cardHeight + 10f
        }

        fun drawEmptyNotice(message: String) {
            ensureSpace(56f)
            val boxRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 46f)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BORDER
                style = Paint.Style.STROKE
                strokeWidth = 0.85f
            }
            activeCanvas.drawRoundRect(boxRect, 8f, 8f, bgPaint)
            activeCanvas.drawRoundRect(boxRect, 8f, 8f, borderPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_META
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            activeCanvas.drawText(message, MARGIN_LEFT + 16f, currentY + 28f, textPaint)
            currentY += 56f
        }

        private fun drawFooter(c: Canvas, pageNum: Int) {
            val footerY = MARGIN_BOTTOM - FOOTER_HEIGHT

            // Top divider
            val dividerPaint = Paint().apply {
                color = COLOR_BORDER
                strokeWidth = 0.75f
            }
            c.drawLine(MARGIN_LEFT, footerY, MARGIN_RIGHT, footerY, dividerPaint)

            // Medical & AI disclaimer text (+30%: 7f -> 9f)
            val disclaimerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_META
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val discLayout = StaticLayout.Builder.obtain(DISCLAIMER_TEXT, 0, DISCLAIMER_TEXT.length, disclaimerPaint, (USABLE_WIDTH - 70f).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()

            c.save()
            c.translate(MARGIN_LEFT, footerY + 6f)
            discLayout.draw(c)
            c.restore()

            // Page Number on bottom right (+30%: 8.5f -> 11f)
            val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BODY
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val pageStr = "Page $pageNum"
            val pageW = pagePaint.measureText(pageStr)
            c.drawText(pageStr, MARGIN_RIGHT - pageW, footerY + 22f, pagePaint)
        }

        fun finish() {
            if (currentPage != null) {
                drawFooter(canvas!!, pageNumber)
                document.finishPage(currentPage)
                currentPage = null
                canvas = null
            }
        }
    }

    private fun cleanMarkdownForPdf(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw
            .replace("**", "")
            .replace("##", "")
            .replace("#", "")
            .replace("`", "")
            .replace("*", "")
            .trim()
    }

    private fun sharePdfFile(context: Context, file: File, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "$subject\nGenerated securely via Zivaa Senior App.")
            clipData = ClipData.newRawUri("Lab Report PDF", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Lab Summary").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }
}
