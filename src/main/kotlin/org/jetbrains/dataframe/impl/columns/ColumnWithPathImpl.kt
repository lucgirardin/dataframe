package org.jetbrains.dataframe.impl.columns

import org.jetbrains.dataframe.ColumnPath
import org.jetbrains.dataframe.DataFrameBase
import org.jetbrains.dataframe.addPath
import org.jetbrains.dataframe.api.columns.DataColumn
import org.jetbrains.dataframe.api.columns.ColumnWithPath

internal class ColumnWithPathImpl<T> internal constructor(override val data: DataColumn<T>, override val path: ColumnPath,
                                                          override val df: DataFrameBase<*>
) : ColumnWithPath<T> {
    override val parent by lazy {
        if (path.isNotEmpty()) path.dropLast(1).let { df[it].addPath(it, df) } else null
    }
}