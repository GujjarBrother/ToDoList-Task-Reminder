package com.sag.todo.list.task.reminder.core.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.room.Room
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.data.db.ToDosDatabase
import com.sag.todo.list.task.reminder.data.db.ToDosDatabase.Companion.migration1to2
import com.sag.todo.list.task.reminder.core.utils.FabRateUsAndApplyAnimation
import com.sag.todo.list.task.reminder.core.utils.SignInAndSignUpCardViewsAnimation
import com.sag.todo.list.task.reminder.core.utils.SplashImageAnimation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun provideInputMethodManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ToDosDatabase::class.java, "ToDos_Tasks_Database")
            .allowMainThreadQueries().addMigrations(migration1to2).build()

    @Provides
    @Singleton
    fun provideDAO(toDosDatabase: ToDosDatabase) = toDosDatabase.dao()

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    @SplashImageAnimation
    fun provideSplashImageAnimation(@ApplicationContext context: Context): Animation =
        AnimationUtils.loadAnimation(context, R.anim.splash_image_animation)

    @Provides
    @Singleton
    @SignInAndSignUpCardViewsAnimation
    fun provideSignInAndSignUpCardViewsAnimation(@ApplicationContext context: Context): Animation =
        AnimationUtils.loadAnimation(context, R.anim.sign_in_and_sign_up_card_views_animation)

    @Provides
    @Singleton
    @FabRateUsAndApplyAnimation
    fun provideFabRateUsAndApplyAnimation(@ApplicationContext context: Context): Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_rate_us_and_apply_animation)
}