package com.gemwallet.android.data.service.store.database.mappers

interface Mapper<E, D, EO, DO> {

    fun asDomain(entity: E, options: (() -> EO)? = null): D

    fun asEntity(domain: D, options: (() -> DO)? = null): E
}