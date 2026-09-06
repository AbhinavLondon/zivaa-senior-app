package com.zivaa.app.ui.medicine

import androidx.compose.ui.graphics.Color

data class MedicineCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconGlyph: String,
    val tone: Color
)

data class MedicineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: Int,
    val iconGlyph: String,
    val tone: Color,
    val categoryId: String // "pain_fever", "cold_cough", "digestion", etc.
)

data class BasketItem(
    val medicine: MedicineItem,
    var quantity: Int
)

data class DeliveryOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: Int
)

object MedicineMockData {
    val categories = listOf(
        MedicineCategory("everyday", "Everyday medicines", "Fever, cold, acidity, vitamins, first aid.", "link", Color(0xFFB56A49)),
        MedicineCategory("homeopathy", "Homeopathy medicines", "Gentle, natural remedies — by name or concern.", "leaf", Color(0xFF4A6B53)),
        MedicineCategory("ayurvedic", "Ayurvedic medicines", "Time-tested herbal remedies and tonics.", "sunny", Color(0xFFD39A52))
    )

    val everydayMedicines = listOf(
        MedicineItem("m1", "Paracetamol 500mg", "Crocin · fever & body ache", 35, "flare", Color(0xFFB56A49), "pain_fever"),
        MedicineItem("m2", "Ibuprofen 400mg", "Brufen · pain & swelling", 48, "show_chart", Color(0xFFB56A49), "pain_fever"),
        MedicineItem("m3", "Cough syrup", "Benadryl · dry cough", 120, "medication_liquid", Color(0xFF4A6B53), "cold_cough"),
        MedicineItem("m4", "Cetirizine 10mg", "Sneezing, runny nose, allergy", 28, "medication_liquid", Color(0xFF4A6B53), "cold_cough"),
        MedicineItem("m5", "Antacid gel", "Digene · acidity & gas", 85, "water_drop", Color(0xFFD39A52), "digestion"),
        MedicineItem("m6", "ORS sachets", "For loose motions, rehydration", 15, "water_drop", Color(0xFFD39A52), "digestion"),
        MedicineItem("m7", "Vitamin C", "Daily immunity", 60, "eco", Color(0xFF4A6B53), "vitamins"),
        MedicineItem("m8", "Antiseptic liquid", "Dettol · cuts & wounds", 95, "shield", Color(0xFFB56A49), "first_aid")
    )

    val homeopathyMedicines = listOf(
        MedicineItem("h1", "Arnica Montana 30C", "Bruises & body ache", 90, "eco", Color(0xFF4A6B53), "pain_fever"),
        MedicineItem("h2", "Nux Vomica 30C", "Acidity & indigestion", 85, "water_drop", Color(0xFFD39A52), "digestion"),
        MedicineItem("h3", "Belladonna 30C", "Fever & throbbing headache", 85, "flare", Color(0xFFB56A49), "pain_fever"),
        MedicineItem("h4", "Allium Cepa 30C", "Cold, sneezing & runny nose", 80, "eco", Color(0xFF4A6B53), "cold_cough"),
        MedicineItem("h5", "Calendula ointment", "Cuts, grazes & sore skin", 130, "shield", Color(0xFF4A6B53), "first_aid")
    )

    val ayurvedicMedicines = listOf(
        MedicineItem("a1", "Ashwagandha tablets", "Strength, stress & sleep", 220, "nightlight", Color(0xFFD39A52), "vitamins"),
        MedicineItem("a2", "Triphala churna", "Digestion & gentle cleanse", 140, "water_drop", Color(0xFF4A6B53), "digestion"),
        MedicineItem("a3", "Chyawanprash", "Daily immunity & vigour", 295, "flare", Color(0xFFD39A52), "vitamins"),
        MedicineItem("a4", "Tulsi drops", "Cough, cold & throat", 95, "eco", Color(0xFF4A6B53), "cold_cough"),
        MedicineItem("a5", "Brahmi tablets", "Memory & calm focus", 175, "mood", Color(0xFF4A6B53), "vitamins")
    )

    val deliveryOptions = listOf(
        DeliveryOption("2h", "Within 2 hours", "Express · arrives by 11:30 AM", 95),
        DeliveryOption("evening", "By this evening", "Free · between 5 — 8 PM", 0),
        DeliveryOption("tomorrow", "Tomorrow morning", "Free · between 8 — 11 AM", 0)
    )
}
