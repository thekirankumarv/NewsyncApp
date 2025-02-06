package com.thekirankumarv.newsync.home.domain.usecase.app_entry

import com.thekirankumarv.newsync.home.domain.LocalUserManger

class SaveAppEntry(
    private val localUserManger: LocalUserManger
) {

    suspend operator fun invoke(){
        localUserManger.saveAppEntry()
    }

}