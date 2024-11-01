package com.gemwallet.android.data.services.gemapi.http

import okhttp3.Request
import okio.IOException
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ResultCall<T>(val delegate: Call<T>) : Call<Result<T>> {
    override fun execute(): Response<Result<T>> = try {
        val response = delegate.execute() // In try for catch IO errors
        handleResponse(response)
    } catch (err: Throwable) {
        Response.success(Result.failure(err))
    }

    override fun enqueue(callback: Callback<Result<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(this@ResultCall, handleResponse(response))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    val errorMessage = when (t) {
                        is IOException -> "No internet connection"
                        is HttpException -> "Server went wrong"
                        else -> t.localizedMessage
                    }
                    callback.onResponse(this@ResultCall,
                        Response.success(Result.failure(Exception(errorMessage, t)))
                    )
                }
            }
        )
    }

    override fun clone(): Call<Result<T>> = ResultCall(delegate.clone())

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

    private fun handleResponse(response: Response<T>): Response<Result<T>> {
        return if (response.isSuccessful) {
            val result = try {
                val body = response.body() ?: throw IOException()
                Result.success(body)
            } catch (err: Throwable) {
                Result.failure(err) // TODO: Add CallException(httpCode, err)
            }
            Response.success(response.code(), result)
        } else {
            Response.success(Result.failure(HttpException(response)))
        }
    }
}