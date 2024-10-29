package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbDelegationValidator
import com.wallet.core.primitives.DelegationValidator

class DelegationValidatorMapper : Mapper<DbDelegationValidator, DelegationValidator, Nothing, Nothing> {
    override fun asDomain(entity: DbDelegationValidator, options: (() -> Nothing)?): DelegationValidator {
        return DelegationValidator(
            id = entity.id,
            chain = entity.chain,
            name = entity.name,
            isActive = entity.isActive,
            commision = entity.commission,
            apr = entity.apr,
        )

    }

    override fun asEntity(domain: DelegationValidator, options: (() -> Nothing)?): DbDelegationValidator {
        return DbDelegationValidator(
            id = domain.id,
            chain = domain.chain,
            name = domain.name,
            isActive = domain.isActive,
            commission = domain.commision,
            apr = domain.apr,
        )
    }
}