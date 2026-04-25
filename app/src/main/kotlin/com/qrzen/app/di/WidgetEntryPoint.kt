package com.qrzen.app.di

import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.BlockEventDao
import com.qrzen.app.data.db.TimeBlockDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun appBlockDao(): AppBlockDao
    fun blockEventDao(): BlockEventDao
    fun timeBlockDao(): TimeBlockDao
}
