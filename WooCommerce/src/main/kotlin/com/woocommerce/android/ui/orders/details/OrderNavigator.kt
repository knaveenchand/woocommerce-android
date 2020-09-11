package com.woocommerce.android.ui.orders.details

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.extensions.navigateSafely
import com.woocommerce.android.ui.orders.details.OrderNavigationTarget.ViewOrderStatusSelector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderNavigator @Inject constructor() {
    fun navigate(fragment: Fragment, target: OrderNavigationTarget) {
        when (target) {
            is ViewOrderStatusSelector -> {
                val action = OrderDetailFragmentNewDirections
                    .actionOrderDetailFragmentToOrderStatusSelectorDialog(
                        target.currentStatus, target.orderStatusList
                    )
                fragment.findNavController().navigateSafely(action)
            }
        }
    }
}
