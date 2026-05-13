     1	package com.qrzen.app.di
     2	
     3	import com.qrzen.app.data.db.AppBlockDao
     4	import com.qrzen.app.data.db.BlockEventDao
     5	import com.qrzen.app.data.db.BlockFolderDao
     6	import com.qrzen.app.data.db.TimeBlockDao
     7	import dagger.hilt.EntryPoint
     8	import dagger.hilt.InstallIn
     9	import dagger.hilt.components.SingletonComponent
    10	
    11	@EntryPoint
    12	@InstallIn(SingletonComponent::class)
    13	interface WidgetEntryPoint {
    14	    fun appBlockDao(): AppBlockDao
    15	    fun blockEventDao(): BlockEventDao
    16	    fun blockFolderDao(): BlockFolderDao
    17	    fun timeBlockDao(): TimeBlockDao
    18	}
    19	