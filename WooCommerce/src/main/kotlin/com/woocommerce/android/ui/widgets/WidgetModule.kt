package com.woocommerce.android.ui.widgets

import com.woocommerce.android.di.ActivityScope
import com.woocommerce.android.di.FragmentScope
import com.woocommerce.android.ui.base.UIMessageResolver
import com.woocommerce.android.ui.widgets.WidgetModule.TodayWidgetConfigureActivityModule
import com.woocommerce.android.ui.widgets.WidgetModule.TodayWidgetConfigureFragmentModule
import com.woocommerce.android.ui.widgets.stats.today.TodayWidgetConfigureFragment
import com.woocommerce.android.ui.widgets.stats.today.TodayWidgetConfigureModule
import com.woocommerce.android.ui.widgets.stats.today.TodayWidgetUIMessageResolver
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    TodayWidgetConfigureActivityModule::class,
    TodayWidgetConfigureFragmentModule::class
])
object WidgetModule {
    @Module
    abstract class TodayWidgetConfigureActivityModule {
        @ActivityScope
        @Binds
        abstract fun provideUiMessageResolver(todayUIMessageResolver: TodayWidgetUIMessageResolver): UIMessageResolver
    }

    @Module
    abstract class TodayWidgetConfigureFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [TodayWidgetConfigureModule::class])
        internal abstract fun todayWidgetConfigureFragment(): TodayWidgetConfigureFragment
    }
}