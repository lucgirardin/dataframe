package org.jetbrains.dataframe

import org.jetbrains.dataframe.api.columns.DataColumn

fun <T> DataFrame<T>.print() = println(this)
fun <T> DataColumn<T>.print() = println(this)