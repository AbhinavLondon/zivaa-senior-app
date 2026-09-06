package com.zivaa.app.ui.medicine

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrderMedicineViewModel : ViewModel() {
    private val _basketItems = MutableStateFlow<List<BasketItem>>(emptyList())
    val basketItems: StateFlow<List<BasketItem>> = _basketItems.asStateFlow()

    private val _selectedDeliveryOptionId = MutableStateFlow<String?>(null)
    val selectedDeliveryOptionId: StateFlow<String?> = _selectedDeliveryOptionId.asStateFlow()

    fun addToBasket(medicine: MedicineItem) {
        _basketItems.update { current ->
            val existing = current.find { it.medicine.id == medicine.id }
            if (existing != null) {
                current.map {
                    if (it.medicine.id == medicine.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + BasketItem(medicine, 1)
            }
        }
    }

    fun updateQuantity(medicineId: String, newQuantity: Int) {
        _basketItems.update { current ->
            if (newQuantity <= 0) {
                current.filter { it.medicine.id != medicineId }
            } else {
                current.map {
                    if (it.medicine.id == medicineId) it.copy(quantity = newQuantity) else it
                }
            }
        }
    }

    fun selectDeliveryOption(optionId: String) {
        _selectedDeliveryOptionId.value = optionId
    }

    fun getTotalItemsPrice(): Int {
        return _basketItems.value.sumOf { it.medicine.price * it.quantity }
    }

    fun getDeliveryPrice(): Int {
        val selectedId = _selectedDeliveryOptionId.value ?: return 0
        return MedicineMockData.deliveryOptions.find { it.id == selectedId }?.price ?: 0
    }

    fun getTotalPrice(): Int {
        return getTotalItemsPrice() + getDeliveryPrice()
    }
    
    fun clearBasket() {
        _basketItems.value = emptyList()
        _selectedDeliveryOptionId.value = null
    }
}
