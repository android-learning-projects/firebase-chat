package com.example.chatapp.utils

import kotlinx.coroutines.*

abstract class AsyncTaskCoroutine<I, O> {
    var result: O? = null
    //private var result: O
    open fun onPreExecute() {}

    open fun onPostExecute(result: O?) {}
    abstract fun doInBackground(vararg params: I): O

    @DelicateCoroutinesApi
    fun <T> execute(vararg input: I) {
        GlobalScope.launch(Dispatchers.Main) {
            onPreExecute()
            callAsync(*input)
        }
    }

    private suspend fun callAsync(vararg input: I) {
        GlobalScope.async(Dispatchers.IO) {
            result = doInBackground(*input)
        }.await()
        GlobalScope.launch(Dispatchers.Main) {

            onPostExecute(result)


        }
    }
}