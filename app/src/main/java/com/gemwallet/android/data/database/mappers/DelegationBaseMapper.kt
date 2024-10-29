package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbDelegationBase
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.DelegationBase
import java.util.UUID

class DelegationBaseMapper : Mapper<DbDelegationBase, DelegationBase, Nothing, String> {
    override fun asDomain(entity: DbDelegationBase, options: (() -> Nothing)?): DelegationBase {
        throw IllegalAccessException()
    }

    override fun asEntity(domain: DelegationBase, options: (() -> String)?): DbDelegationBase {
        return DbDelegationBase(
            id = UUID.randomUUID().toString(),
            address = options?.invoke() ?: "",
            delegationId = domain.delegationId,
            validatorId = domain.validatorId,
            assetId = domain.assetId.toIdentifier(),
            state = domain.state,
            balance = domain.balance,
            completionDate = domain.completionDate,
            rewards = domain.rewards,
            shares = domain.shares,
        )
    }
}