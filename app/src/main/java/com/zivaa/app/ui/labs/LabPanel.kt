package com.zivaa.app.ui.labs

data class LabTestItem(
    val title: String,
    val subtitle: String
)

data class LabPanel(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val testsCount: String,
    val hoursToReport: String,
    val price: String,
    val fastingInfo: String?,
    val includedTests: List<LabTestItem>
)

object LabPanels {
    val panels = listOf(
        LabPanel(
            id = "diabetes",
            category = "SUGAR & DIABETES",
            title = "Diabetes panel",
            description = "A focused look at how well your sugar is controlled, morning and after meals.",
            testsCount = "4",
            hoursToReport = "24h",
            price = "₹700",
            fastingInfo = "Fasting needed — eat nothing for 10 hours before. Water is fine. That's why we come early.",
            includedTests = listOf(
                LabTestItem("HbA1c", "The three-month sugar average."),
                LabTestItem("Fasting blood sugar", "Sugar first thing in the morning."),
                LabTestItem("Post-meal sugar", "How sugar behaves after eating."),
                LabTestItem("Urine sugar", "A simple extra confirmation.")
            )
        ),
        LabPanel(
            id = "heart",
            category = "HEART & CHOLESTEROL",
            title = "Heart & cholesterol panel",
            description = "A clear read on the cholesterol and blood fats that affect the heart.",
            testsCount = "5",
            hoursToReport = "24h",
            price = "₹600",
            fastingInfo = "Fasting needed — eat nothing for 10 hours before. Water is fine. That's why we come early.",
            includedTests = listOf(
                LabTestItem("Total cholesterol", "The overall number."),
                LabTestItem("LDL", "The 'bad' cholesterol to keep low."),
                LabTestItem("HDL", "The 'good' cholesterol to keep up."),
                LabTestItem("Triglycerides", "A blood fat linked to diet.")
            )
        ),
        LabPanel(
            id = "full_body",
            category = "FULL BODY",
            title = "Full body check-up",
            description = "A thorough, head-to-toe check — sugar, heart, kidney, liver, thyroid, vitamins and blood count.",
            testsCount = "60+",
            hoursToReport = "36h",
            price = "₹2,400",
            fastingInfo = "Fasting needed — eat nothing for 10 hours before. Water is fine. That's why we come early.",
            includedTests = listOf(
                LabTestItem("Diabetes & heart", "Sugar, HbA1c and full lipid profile."),
                LabTestItem("Kidney & liver", "How both organs are working."),
                LabTestItem("Thyroid (TSH)", "The body's energy regulator."),
                LabTestItem("Vitamins & blood count", "Vitamin D, B12, iron and CBC.")
            )
        ),
        LabPanel(
            id = "organs",
            category = "KIDNEY, LIVER & THYROID",
            title = "Organ function panel",
            description = "A gentle check that the kidney, liver and thyroid are all doing their job.",
            testsCount = "12",
            hoursToReport = "24h",
            price = "₹900",
            fastingInfo = null,
            includedTests = listOf(
                LabTestItem("Kidney function (KFT)", "Creatinine, urea and more."),
                LabTestItem("Liver function (LFT)", "How the liver is coping."),
                LabTestItem("Thyroid (TSH)", "Energy and metabolism.")
            )
        ),
        LabPanel(
            id = "vitamins",
            category = "TIREDNESS & VITAMINS",
            title = "Vitamins & energy panel",
            description = "For the tiredness and the low vitamin D — checks the common gaps.",
            testsCount = "6",
            hoursToReport = "24h",
            price = "₹1,100",
            fastingInfo = null,
            includedTests = listOf(
                LabTestItem("Vitamin D", "For bones and energy."),
                LabTestItem("Vitamin B12", "For nerves and stamina."),
                LabTestItem("Iron studies", "For strength and colour."),
                LabTestItem("Complete blood count", "A broad look at the blood.")
            )
        )
    )

    fun getPanelById(id: String): LabPanel? {
        return panels.find { it.id == id }
    }
}
