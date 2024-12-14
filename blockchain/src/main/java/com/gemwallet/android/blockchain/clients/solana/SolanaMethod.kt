package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.rpc.model.JSONRpcMethod

enum class SolanaMethod(val value: String) : JSONRpcMethod {
    GetBalance("getBalance"),
    GetTokenBalance("getTokenAccountBalance"),
    GetTokenAccountByOwner("getTokenAccountsByOwner"),
    RentExemption("getMinimumBalanceForRentExemption"),
    GetLatestBlockhash("getLatestBlockhash"),
    GetPriorityFee("getRecentPrioritizationFees"),
    SendTransaction("sendTransaction"),
    GetTransaction("getTransaction"),
    GetValidators("getVoteAccounts"),
    GetDelegations("getProgramAccounts"),
    GetEpoch("getEpochInfo"),
    GetAccountInfo("getAccountInfo"),
    GetHealth("getHealth"),
    GetSlot("getSlot"),
    GetGenesisHash("getGenesisHash")
    ;

    override fun value(): String = value
}