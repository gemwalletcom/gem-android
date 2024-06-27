package com.gemwallet.android.data.database.mappers

interface Mapper<E, D> {

    fun asDomain(entity: E): D

    fun asEntity(domain: D): E
}