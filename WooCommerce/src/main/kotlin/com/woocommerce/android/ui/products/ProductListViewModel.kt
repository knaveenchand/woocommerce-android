package com.woocommerce.android.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woocommerce.android.R
import com.woocommerce.android.annotations.OpenClassOnDebug
import com.woocommerce.android.di.UI_THREAD
import com.woocommerce.android.model.Product
import com.woocommerce.android.tools.NetworkStatus
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@OpenClassOnDebug
class ProductListViewModel @Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val productRepository: ProductListRepository,
    private val networkStatus: NetworkStatus
) : ScopedViewModel(mainDispatcher) {
    private var canLoadMore = true
    private var isLoadingProducts = false

    val productList = MutableLiveData<List<Product>>()

    private val _isSkeletonShown = MutableLiveData<Boolean>()
    val isSkeletonShown: LiveData<Boolean> = _isSkeletonShown

    private val _showSnackbarMessage = SingleLiveEvent<Int>()
    val showSnackbarMessage: LiveData<Int> = _showSnackbarMessage

    private val _isLoadingMore = MutableLiveData<Boolean>()
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    fun start(searchQuery: String? = null) {
        if (productList.value.isNullOrEmpty()) {
            loadProducts(searchQuery = searchQuery)
        }
    }

    override fun onCleared() {
        super.onCleared()
        productRepository.onCleanup()
    }

    fun loadProducts(searchQuery: String? = null, loadMore: Boolean = false) {
        if (isLoadingProducts) {
            WooLog.d(WooLog.T.PRODUCTS, "already loading products")
            return
        }

        if (loadMore && !productRepository.canLoadMoreProducts) {
            WooLog.d(WooLog.T.PRODUCTS, "can't load more products")
            return
        }

        launch {
            isLoadingProducts = true
            _isLoadingMore.value = loadMore

            if (searchQuery != null && !loadMore) {
                _isSkeletonShown.value = true
            } else if (!loadMore) {
                // if this is the initial load, first get the products from the db and if there are any show
                // them immediately, otherwise make sure the skeleton shows
                val productsInDb = productRepository.getProductList()
                if (productsInDb.isEmpty()) {
                    _isSkeletonShown.value = true
                } else {
                    productList.value = productsInDb
                }
            }

            fetchProductList(searchQuery, loadMore)
        }
    }

    fun refreshProducts(searchQuery: String? = null) {
        _isRefreshing.value = true
        loadProducts(searchQuery = searchQuery)
    }

    private suspend fun fetchProductList(searchQuery: String? = null, loadMore: Boolean = false) {
        if (networkStatus.isConnected()) {
            if (searchQuery.isNullOrEmpty()) {
                productList.value = productRepository.fetchProductList(loadMore)
            } else {
                val fetchedProducts = productRepository.searchProductList(searchQuery, loadMore)
                if (searchQuery == productRepository.lastSearchQuery) {
                    if (loadMore) {
                        addProducts(fetchedProducts)
                    } else {
                        productList.value = fetchedProducts
                    }
                } else {
                    WooLog.d(WooLog.T.PRODUCTS, "Search query changed")
                }
            }
            canLoadMore = productRepository.canLoadMoreProducts
        } else {
            _showSnackbarMessage.value = R.string.offline_error
        }

        _isSkeletonShown.value = false
        _isLoadingMore.value = false
        _isRefreshing.value = false
        isLoadingProducts = false
    }

    private fun addProducts(products: List<Product>) {
        if (productList.value.isNullOrEmpty()) {
            productList.value = products
        } else {
            with(ArrayList<Product>()) {
                addAll(productList.value!!)
                addAll(products)
                productList.value = this
            }
        }
    }
}
