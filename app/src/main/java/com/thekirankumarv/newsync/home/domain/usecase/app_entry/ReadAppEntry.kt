package com.thekirankumarv.newsync.home.domain.usecase.app_entry

import com.thekirankumarv.newsync.home.domain.LocalUserManger
import kotlinx.coroutines.flow.Flow

class ReadAppEntry(
    private val localUserManger: LocalUserManger
) {

    operator fun invoke(): Flow<Boolean> {
        return localUserManger.readAppEntry()
    }

}