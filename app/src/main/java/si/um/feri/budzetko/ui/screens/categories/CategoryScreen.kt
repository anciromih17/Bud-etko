package si.um.feri.budzetko.ui.screens.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.emoji2.emojipicker.EmojiPickerView
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoBackground
import si.um.feri.budzetko.ui.theme.BudzetkoBorder
import si.um.feri.budzetko.ui.theme.BudzetkoInk
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.CategoryListItem
import si.um.feri.budzetko.viewmodel.CategoryUiState
import si.um.feri.budzetko.viewmodel.CategoryViewModel

private val CardSurface = Color(0xFFFFFFFF)
private val PrimaryAccent = BudzetkoPurple
private val SecondaryAccent = Color(0xFFF4F0FF)
private val LimeAccent = BudzetkoLime
private val SoftBorder = BudzetkoBorder
private val Ink = BudzetkoInk
private val MutedInk = Color(0xFF6D6774)
private val Danger = Color(0xFFB3261E)
private val CategoryAccentColors = listOf(
    Color(0xFFFFE96A),
    Color(0xFFD8F25D),
    Color(0xFFCFC6F4),
    Color(0xFFFFC7D6),
    Color(0xFFCFE9F7),
    Color(0xFF8B6BFF),
    Color(0xFFFFB864),
    Color(0xFFC9F4D7)
)

@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddExpenseClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    CategoryContent(
        uiState = uiState,
        onAddClick = viewModel::openCreateDialog,
        onEditClick = viewModel::openEditDialog,
        onDeleteClick = viewModel::requestDeleteCategory,
        onConfirmDelete = viewModel::confirmDeleteCategory,
        onCancelDelete = viewModel::cancelDeleteCategory,
        onNameChange = viewModel::onNameChange,
        onLimitInputChange = viewModel::onLimitInputChange,
        onLimitSliderChange = viewModel::onLimitSliderChange,
        onColorSelected = viewModel::onColorSelected,
        onBudgetRoleSelected = viewModel::onBudgetRoleSelected,
        onEmojiPickerOpen = viewModel::openEmojiPicker,
        onEmojiPickerClose = viewModel::closeEmojiPicker,
        onEmojiSelected = viewModel::onEmojiSelected,
        onSuggestLimit = viewModel::suggestCurrentCategoryLimit,
        onSaveClick = viewModel::saveCategory,
        onDismissDialog = viewModel::closeDialog,
        onSettingsClick = onSettingsClick,
        onProfileClick = onProfileClick,
        onAddExpenseClick = onAddExpenseClick,
        onTransactionsClick = onTransactionsClick,
        modifier = modifier
    )
}

@Composable
private fun CategoryContent(
    uiState: CategoryUiState,
    onAddClick: () -> Unit,
    onEditClick: (CategoryListItem) -> Unit,
    onDeleteClick: (CategoryEntity) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onLimitInputChange: (String) -> Unit,
    onLimitSliderChange: (Float) -> Unit,
    onColorSelected: (Int) -> Unit,
    onBudgetRoleSelected: (CategoryBudgetRole) -> Unit,
    onEmojiPickerOpen: () -> Unit,
    onEmojiPickerClose: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    onSuggestLimit: () -> Unit,
    onSaveClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BudzetkoBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onBudgetClick = onTransactionsClick,
                onAddExpenseClick = onAddExpenseClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BudzetkoBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, top = 34.dp, end = 24.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CategoryHeader(onProfileClick = onProfileClick)
            }

            if (uiState.categoryItems.isEmpty()) {
                item {
                    EmptyCategoryCard(onAddClick = onAddClick)
                }
            } else {
                items(
                    items = uiState.categoryItems,
                    key = { it.category.id }
                ) { item ->
                    CategoryCard(
                        item = item,
                        onEditClick = { onEditClick(item) },
                        onDeleteClick = { onDeleteClick(item.category) }
                    )
                }
            }

            item {
                CreateNewButton(onClick = onAddClick)
            }
        }
    }

    if (uiState.isDialogOpen) {
        CategoryDialog(
            uiState = uiState,
            onNameChange = onNameChange,
            onLimitInputChange = onLimitInputChange,
            onLimitSliderChange = onLimitSliderChange,
            onColorSelected = onColorSelected,
            onBudgetRoleSelected = onBudgetRoleSelected,
            onEmojiPickerOpen = onEmojiPickerOpen,
            onSuggestLimit = onSuggestLimit,
            onSaveClick = onSaveClick,
            onDismiss = onDismissDialog
        )
    }

    if (uiState.isEmojiPickerOpen) {
        EmojiPickerDialog(
            onEmojiSelected = onEmojiSelected,
            onDismiss = onEmojiPickerClose
        )
    }

    uiState.categoryPendingDelete?.let { category ->
        DeleteCategoryDialog(
            categoryName = category.name,
            errorMessage = uiState.errorMessage,
            onConfirm = onConfirmDelete,
            onDismiss = onCancelDelete
        )
    }
}

@Composable
private fun CategoryHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.categories_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = stringResource(R.string.categories_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = PrimaryAccent.copy(alpha = 0.10f),
                    spotColor = PrimaryAccent.copy(alpha = 0.12f)
                )
                .clip(CircleShape)
                .background(Ink)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.profile),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CategoryCard(
    item: CategoryListItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val category = item.category
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, SoftBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryLeadingIcon(category = category)

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = item.monthlyLimit?.let { limit ->
                        stringResource(R.string.monthly_limit_value, limit.formatMoney())
                    } ?: stringResource(R.string.monthly_limit_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MutedInk
                )
            }

            Row(
                modifier = Modifier.width(68.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryActionButton(
                    icon = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.edit_category),
                    tint = Ink,
                    background = Color.Transparent,
                    onClick = onEditClick
                )
                CategoryActionButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_category),
                    tint = Danger,
                    background = Color.Transparent,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun CategoryActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(30.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun CategoryLeadingIcon(category: CategoryEntity) {
    val accentColor = categoryAccentColor(category.colorIndex)
    val emoji = category.emoji

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        if (emoji.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Ink)
            )
        } else {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun CreateNewButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Ink.copy(alpha = 0.10f),
                spotColor = Ink.copy(alpha = 0.16f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Ink,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.create_new),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyCategoryCard(onAddClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_categories_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = stringResource(R.string.empty_categories_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
            CreateNewButton(onClick = onAddClick)
        }
    }
}

private fun categoryAccentColor(colorIndex: Int): Color {
    val index = colorIndex.coerceAtLeast(0) % CategoryAccentColors.size
    return CategoryAccentColors[index]
}

private fun Double.formatMoney(): String = "%.2f".format(this)

@Composable
private fun CategoryDialog(
    uiState: CategoryUiState,
    onNameChange: (String) -> Unit,
    onLimitInputChange: (String) -> Unit,
    onLimitSliderChange: (Float) -> Unit,
    onColorSelected: (Int) -> Unit,
    onBudgetRoleSelected: (CategoryBudgetRole) -> Unit,
    onEmojiPickerOpen: () -> Unit,
    onSuggestLimit: () -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = uiState.editingCategory != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                shape = RoundedCornerShape(30.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, SoftBorder),
                shadowElevation = 12.dp
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(if (isEditing) R.string.edit_category else R.string.new_category),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink
                            )
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CardSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = Ink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.category_name),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            OutlinedTextField(
                                value = uiState.nameInput,
                                onValueChange = onNameChange,
                                placeholder = { Text(text = stringResource(R.string.category_name_hint)) },
                                singleLine = true,
                                isError = uiState.errorMessage != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            uiState.errorMessage?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    item {
                        CategoryStylePicker(
                            selectedEmoji = uiState.selectedEmoji,
                            selectedColorIndex = uiState.selectedColorIndex,
                            onColorSelected = onColorSelected,
                            onEmojiPickerOpen = onEmojiPickerOpen
                        )
                    }

                    item {
                        CategoryBudgetRolePicker(
                            selectedBudgetRole = uiState.selectedBudgetRole,
                            onBudgetRoleSelected = onBudgetRoleSelected
                        )
                    }

                    item {
                        CategoryLimitPreview(
                            limitInput = uiState.limitInput,
                            sliderValue = uiState.limitSliderValue,
                            maxLimit = uiState.currentMonthIncome?.toFloat() ?: 2_000f,
                            onSuggestLimit = onSuggestLimit,
                            onLimitInputChange = onLimitInputChange,
                            onLimitSliderChange = onLimitSliderChange
                        )
                    }

                    item {
                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Ink,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(if (isEditing) R.string.save else R.string.create),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetRolePicker(
    selectedBudgetRole: CategoryBudgetRole,
    onBudgetRoleSelected: (CategoryBudgetRole) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.category_budget_role),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryBudgetRole.entries.forEach { role ->
                FilterChip(
                    selected = selectedBudgetRole == role,
                    onClick = { onBudgetRoleSelected(role) },
                    label = { Text(text = stringResource(role.labelRes())) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeAccent.copy(alpha = 0.55f),
                        selectedLabelColor = PrimaryAccent
                    )
                )
            }
        }
    }
}

private fun CategoryBudgetRole.labelRes(): Int {
    return when (this) {
        CategoryBudgetRole.NEEDS -> R.string.budget_role_needs
        CategoryBudgetRole.WANTS -> R.string.budget_role_wants
        CategoryBudgetRole.SAVINGS -> R.string.budget_role_savings
        CategoryBudgetRole.OTHER -> R.string.budget_role_other
    }
}

@Composable
private fun CategoryStylePicker(
    selectedEmoji: String?,
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    onEmojiPickerOpen: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.choose_emoji),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(66.dp)
                .clip(CircleShape)
                .clickable(onClick = onEmojiPickerOpen)
                .background(SecondaryAccent)
                .border(BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.34f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selectedEmoji.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.choose_category_icon),
                    tint = PrimaryAccent,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Text(
                    text = selectedEmoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryColorRow(
                startIndex = 0,
                selectedEmoji = selectedEmoji,
                selectedColorIndex = selectedColorIndex,
                onColorSelected = onColorSelected
            )
            CategoryColorRow(
                startIndex = 4,
                selectedEmoji = selectedEmoji,
                selectedColorIndex = selectedColorIndex,
                onColorSelected = onColorSelected
            )
        }
    }
}

@Composable
private fun EmojiPickerDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
                .padding(horizontal = 18.dp, vertical = 34.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 430.dp, max = 560.dp),
                shape = RoundedCornerShape(28.dp),
                color = CardSurface,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.emoji_picker_title),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = Ink
                            )
                        }
                    }

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp),
                        factory = { context ->
                            EmojiPickerView(context, null, 0).apply {
                                emojiGridColumns = 8
                                setOnEmojiPickedListener { emojiItem ->
                                    onEmojiSelected(emojiItem.emoji)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryColorRow(
    startIndex: Int,
    selectedEmoji: String?,
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(4) { offset ->
            val index = startIndex + offset
            val color = CategoryAccentColors[index % CategoryAccentColors.size]
            val isSelected = selectedEmoji.isNullOrBlank() && selectedColorIndex == index
            IconButton(
                onClick = { onColorSelected(index) },
                modifier = Modifier.size(38.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 31.dp else 27.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            BorderStroke(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Ink else Color.White
                            ),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun CategoryLimitPreview(
    limitInput: String,
    sliderValue: Float,
    maxLimit: Float,
    onSuggestLimit: () -> Unit,
    onLimitInputChange: (String) -> Unit,
    onLimitSliderChange: (Float) -> Unit
) {
    val sliderMax = maxLimit.coerceAtLeast(1f)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.category_limit),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = limitInput,
                onValueChange = onLimitInputChange,
                suffix = { Text(text = "€") },
                singleLine = true,
                modifier = Modifier.width(116.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onSuggestLimit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeAccent,
                    contentColor = Ink
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.suggest_limit),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Slider(
            value = sliderValue.coerceIn(0f, sliderMax),
            onValueChange = onLimitSliderChange,
            valueRange = 0f..sliderMax
        )
    }
}

@Composable
private fun DeleteCategoryDialog(
    categoryName: String,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(
                text = stringResource(R.string.delete_category_title),
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.delete_category_message, categoryName),
                    color = MutedInk
                )
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger,
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryContentPreview() {
    BudzetkoTheme {
        CategoryContent(
            uiState = CategoryUiState(
                categoryItems = listOf(
                    CategoryListItem(CategoryEntity(id = 1, name = "Hrana", userId = "demo-user"), 600.0),
                    CategoryListItem(CategoryEntity(id = 2, name = "Stroški", userId = "demo-user"), 420.0),
                    CategoryListItem(CategoryEntity(id = 3, name = "Prihranki", userId = "demo-user"), 240.0),
                    CategoryListItem(CategoryEntity(id = 4, name = "Drugo", userId = "demo-user"), null)
                )
            ),
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onNameChange = {},
            onLimitInputChange = {},
            onLimitSliderChange = {},
            onColorSelected = {},
            onBudgetRoleSelected = {},
            onEmojiPickerOpen = {},
            onEmojiPickerClose = {},
            onEmojiSelected = {},
            onSuggestLimit = {},
            onSaveClick = {},
            onDismissDialog = {},
            onSettingsClick = {},
            onProfileClick = {},
            onAddExpenseClick = {},
            onTransactionsClick = {}
        )
    }
}
