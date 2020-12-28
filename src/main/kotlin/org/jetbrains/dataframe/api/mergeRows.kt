package org.jetbrains.dataframe

import org.jetbrains.dataframe.api.columns.ColumnData
import org.jetbrains.dataframe.api.columns.GroupedColumn
import org.jetbrains.dataframe.api.columns.TableColumn
import kotlin.reflect.KType

inline fun <T, reified C> DataFrame<T>.mergeRows(noinline selector: ColumnsSelector<T, C>) = mergeRows(this, selector, getType<C>())

fun <T, C> mergeRows(df: DataFrame<T>, selector: ColumnsSelector<T, C>, type: KType): DataFrame<T> {

    val listType = List::class.createType(type)
    return df.groupBy { except(selector) }.modify {
        val updated = update(selector).suggestTypes(List::class to listType).with2 { row, column ->
            if(row.index > 0) null
            else when(column.kind()) {
                ColumnKind.Data -> column.toList()
                ColumnKind.Group -> column.asGrouped().df
                ColumnKind.Table -> column.asTable().values.union()
            }
        }
        updated[0..0]
    }.ungroup()
}

fun <T, C> MergeClause<T, C, *>.mergeRows(): DataFrame<T> {

    val removeResult = df.doRemove(selector)
    val removeRoot = removeResult.removeRoot ?: return df

    val grouped = df.groupBy { except(selector) }

    val columnsToInsert = removeRoot.allWithColumns().map { node ->
        val column = node.data.column!!
        val newName = column.name
        val newColumn = when(column){
            is GroupedColumn<*> -> {
                val data = grouped.groups.asIterable().map { it.get(column).df }
                ColumnData.createTable(newName, data, column.df)
            }
            is TableColumn<*> -> {
                val data = grouped.groups.asIterable().map { it[column].toList().union() }
                ColumnData.createTable(newName, data, column.df)
            }
            else -> {
                val data = grouped.groups.asIterable().map { it[column].toList() }
                ColumnData.create(newName, data, List::class.createType(column.type))
            }
        }
        ColumnToInsert(node.pathFromRoot(), node, newColumn)
    }
    val result = insertColumns(grouped.keys, columnsToInsert)
    return result
}