package com.gemwallet.android.data.services.gemapi.http

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) {
            return null
        }
        val upperBounds = getParameterUpperBound(0, returnType)
        return if (upperBounds is ParameterizedType && upperBounds.rawType == Result::class.java) {
            object : CallAdapter<Any, Call<Result<*>>> {
                override fun responseType(): Type = getParameterUpperBound(0, upperBounds)

                override fun adapt(call: Call<Any>): Call<Result<*>> = ResultCall(call) as Call<Result<*>>

            }
        } else {
            null
        }
    }
}