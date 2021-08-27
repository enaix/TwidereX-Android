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
package com.twidere.twiderex.viewmodel.search

import androidx.paging.cachedIn
import androidx.paging.map
import com.twidere.services.microblog.SearchService
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.ext.asStateIn
import com.twidere.twiderex.paging.mediator.paging.pager
import com.twidere.twiderex.paging.mediator.search.SearchStatusMediator
import com.twidere.twiderex.repository.AccountRepository
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class SearchTweetsViewModel(
    val database: CacheDatabase,
    private val accountRepository: AccountRepository,
    keyword: String,
) : ViewModel() {
    private val account by lazy {
        accountRepository.activeAccount.asStateIn(viewModelScope, null)
    }

    val source by lazy {
        account.flatMapLatest {
            it?.let { account ->
                SearchStatusMediator(
                    keyword,
                    database,
                    account.accountKey,
                    account.service as SearchService
                ).pager().flow.map { it.map { it.status } }.cachedIn(viewModelScope)
            } ?: emptyFlow()
        }.cachedIn(viewModelScope)
    }
}
