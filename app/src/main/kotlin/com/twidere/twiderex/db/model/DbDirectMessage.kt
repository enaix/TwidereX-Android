/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.db.model.DbDirectMessage.Companion.saveToDb
import com.twidere.twiderex.model.MicroBlogKey

@Entity(
    tableName = "direct_message",
    indices = [Index(value = ["accountKey", "conversationKey", "messageKey"], unique = true)],
)
data class DbDirectMessage(
    @PrimaryKey
    val _id: String,
    val accountKey: MicroBlogKey,
    val sortId: Long,
    // message
    val conversationKey: MicroBlogKey,
    val messageId: String,
    val messageKey: MicroBlogKey,
    // include hash tag in this parameter
    val htmlText: String,
    val createdTimestamp: Long,
    val messageType: String,
    val senderAccountKey: MicroBlogKey,
    val recipientAccountKey: MicroBlogKey,
) {
    val isInComeDM: Boolean
        get() = accountKey == recipientAccountKey

    val conversationUserKey: MicroBlogKey
        get() = if (accountKey == senderAccountKey) recipientAccountKey else senderAccountKey

    companion object {
        suspend fun List<DbDirectMessage>.saveToDb(cacheDatabase: CacheDatabase) {
            cacheDatabase.directMessageDao().insertAll(this)
        }
    }
}

data class DbDirectMessageWithMedia(
    @Embedded
    val message: DbDirectMessage,

    @Relation(parentColumn = "messageKey", entityColumn = "statusKey", entity = DbMedia::class)
    val media: List<DbMedia>,

    @Relation(parentColumn = "messageKey", entityColumn = "statusKey", entity = DbUrlEntity::class)
    val urlEntity: List<DbUrlEntity>,

    @Relation(parentColumn = "senderAccountKey", entityColumn = "userKey", entity = DbUser::class)
    val sender: DbUser
) {
    companion object {
        suspend fun List<DbDirectMessageWithMedia>.saveToDb(cacheDatabase: CacheDatabase) {
            map {
                it.message
            }.saveToDb(cacheDatabase)

            cacheDatabase.mediaDao().insertAll(
                map {
                    it.media
                }.flatten()
            )

            cacheDatabase.urlEntityDao().insertAll(
                map {
                    it.urlEntity
                }.flatten()
            )

            cacheDatabase.userDao().insertAll(
                map { it.sender }
            )
        }
    }
}
