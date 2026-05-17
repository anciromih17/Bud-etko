package si.um.feri.budzetko.viewmodel

import si.um.feri.budzetko.data.entity.CategoryEntity

data class CategoryListItem(
    val category: CategoryEntity,
    val monthlyLimit: Double?
)
