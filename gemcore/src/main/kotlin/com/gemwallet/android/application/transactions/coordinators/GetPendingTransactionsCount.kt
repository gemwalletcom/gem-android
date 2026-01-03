package com.gemwallet.android.application.transactions.coordinators

import kotlinx.coroutines.flow.Flow

interface GetPendingTransactionsCount {

    fun getPendingTransactionsCount(): Flow<Int?>
}