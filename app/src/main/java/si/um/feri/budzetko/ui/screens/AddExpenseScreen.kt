package si.um.feri.budzetko.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import si.um.feri.budzetko.AppLanguage
import si.um.feri.budzetko.AppLanguageStore
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.currency.LocalAppCurrency
import si.um.feri.budzetko.currency.MoneyFormatter
import si.um.feri.budzetko.currency.currentCurrencySymbol
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSoftAccent
import si.um.feri.budzetko.ui.theme.budzetkoSurface
import si.um.feri.budzetko.ui.theme.budzetkoCategoryColor
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel

private val ScreenBackground: Color
    @Composable get() = budzetkoBackground()
private val CardSurface: Color
    @Composable get() = budzetkoSurface()
private val PrimaryAccent = BudzetkoPurple
private val SoftAccent: Color
    @Composable get() = budzetkoSoftAccent()
private val Ink: Color
    @Composable get() = budzetkoInk()
private val MutedInk: Color
    @Composable get() = budzetkoMutedInk()
private val Danger = Color(0xFFB3261E)
private val DateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel,
    expenseToEdit: ExpenseEntity?,
    appLanguage: AppLanguage = AppLanguage.SLOVENIAN,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onRequestCameraPermission: (((Boolean) -> Unit) -> Unit) = { it(false) }
) {
    val currentContext = LocalContext.current
    val localizedContext = remember(currentContext, appLanguage) {
        AppLanguageStore.localizedContext(currentContext, appLanguage)
    }
    val strings = remember(localizedContext) {
        AddExpenseStrings.from(localizedContext)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        AddExpenseScreenContent(
            expenseViewModel = expenseViewModel,
            categoryViewModel = categoryViewModel,
            expenseToEdit = expenseToEdit,
            strings = strings,
            onClose = onClose,
            onSaved = onSaved,
            onAddCategoryClick = onAddCategoryClick,
            onRequestCameraPermission = onRequestCameraPermission
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseScreenContent(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel,
    expenseToEdit: ExpenseEntity?,
    strings: AddExpenseStrings,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onRequestCameraPermission: (((Boolean) -> Unit) -> Unit)
) {
    val categoryState by categoryViewModel.uiState.collectAsState()
    val categories = categoryState.categories
    val selectedCurrency = LocalAppCurrency.current

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(LocalDate.now().format(DateFormatter)) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var isCameraOpen by remember { mutableStateOf(false) }
    var amountCandidates by remember { mutableStateOf<List<ReceiptAmountCandidate>>(emptyList()) }
    var receiptImageSourceFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current

    fun processReceiptImage(file: File) {
        errorMessage = strings.ocrScanning
        val image = runCatching {
            InputImage.fromFilePath(context, android.net.Uri.fromFile(file))
        }.getOrElse {
            errorMessage = strings.ocrFailed
            return
        }
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                val candidates = extractReceiptAmountCandidates(recognizedText)
                receiptImageSourceFile = file

                if (candidates.isEmpty()) {
                    errorMessage = strings.ocrNoAmounts
                } else {
                    amountCandidates = candidates
                    errorMessage = null
                }
            }
            .addOnFailureListener {
                errorMessage = strings.ocrFailed
            }
            .addOnCompleteListener {
                textRecognizer.close()
            }
    }

    LaunchedEffect(expenseToEdit?.id, categories, selectedCurrency) {
        if (expenseToEdit == null) {
            amount = ""
            description = ""
            note = ""
            dateInput = LocalDate.now().format(DateFormatter)
            selectedCategoryId = selectedCategoryId ?: categories.firstOrNull()?.id
            errorMessage = null
            receiptImageSourceFile = null
        } else {
            amount = MoneyFormatter.formatPlain(expenseToEdit.amount, selectedCurrency)
            description = expenseToEdit.description
            note = ""
            dateInput = Instant.ofEpochMilli(expenseToEdit.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateFormatter)
            selectedCategoryId = expenseToEdit.categoryId
            errorMessage = null
            receiptImageSourceFile = null
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClose
                )
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(30.dp),
                color = CardSurface,
                shadowElevation = 12.dp
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item { AddExpenseHeader(strings = strings, onClose = onClose) }
                    item {
                        AddExpenseFormCard(
                            strings = strings,
                            isEditing = expenseToEdit != null,
                            amount = amount,
                            description = description,
                            note = note,
                            dateInput = dateInput,
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            errorMessage = errorMessage,
                            onAmountChange = {
                                amount = it.filter { char -> char.isDigit() || char == '.' || char == ',' }
                                errorMessage = null
                            },
                            onDescriptionChange = {
                                description = it
                                errorMessage = null
                            },
                            onNoteChange = { note = it },
                            onDateChange = {
                                dateInput = it
                                errorMessage = null
                            },
                            onDatePickerClick = {
                                isDatePickerOpen = true
                            },
                            onCategorySelected = {
                                selectedCategoryId = it
                                errorMessage = null
                            },
                            onAddCategoryClick = onAddCategoryClick,
                            onScanReceiptClick = {
                                val hasCameraPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasCameraPermission) {
                                    isCameraOpen = true
                                } else {
                                    onRequestCameraPermission { isGranted ->
                                        if (isGranted) {
                                            isCameraOpen = true
                                        } else {
                                            errorMessage = strings.ocrCameraPermissionMessage
                                        }
                                    }
                                }
                            },
                            onSaveClick = {
                                val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
                                val parsedDate = parseDate(dateInput)
                                val categoryId = selectedCategoryId

                                when {
                                    parsedAmount == null || parsedAmount <= 0.0 ->
                                        errorMessage = strings.invalidAmount

                                    description.isBlank() ->
                                        errorMessage = strings.missingDescription

                                    categoryId == null ->
                                        errorMessage = strings.missingCategory

                                    parsedDate == null ->
                                        errorMessage = strings.invalidDate

                                    else -> {
                                        val amountInEur = MoneyFormatter.toBaseEur(parsedAmount, selectedCurrency)
                                        val dateMillis = parsedDate
                                            .atStartOfDay(ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                        val fullDescription = if (note.isBlank()) {
                                            description.trim()
                                        } else {
                                            "${description.trim()}\n${note.trim()}"
                                        }
                                        val receiptImagePath = receiptImageSourceFile
                                            ?.let { persistReceiptImage(context, it) }
                                            ?: expenseToEdit?.receiptImagePath

                                        if (expenseToEdit == null) {
                                            expenseViewModel.addExpense(
                                                amount = amountInEur,
                                                date = dateMillis,
                                                description = fullDescription,
                                                categoryId = categoryId,
                                                receiptImagePath = receiptImagePath
                                            )
                                        } else {
                                            expenseViewModel.updateExpense(
                                                expense = expenseToEdit,
                                                amount = amountInEur,
                                                date = dateMillis,
                                                description = fullDescription,
                                                categoryId = categoryId,
                                                receiptImagePath = receiptImagePath
                                            )
                                        }
                                        onSaved()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isDatePickerOpen) {
        val selectedDateMillis = parseDate(dateInput)
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateInput = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateFormatter)
                            errorMessage = null
                        }
                        isDatePickerOpen = false
                    }
                ) {
                    Text(strings.choose)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (isCameraOpen) {
        ReceiptCameraDialog(
            strings = strings,
            onDismiss = { isCameraOpen = false },
            onImageCaptured = { file ->
                isCameraOpen = false
                processReceiptImage(file)
            },
            onCaptureError = {
                isCameraOpen = false
                errorMessage = strings.ocrFailed
            }
        )
    }

    if (amountCandidates.isNotEmpty()) {
        ReceiptAmountPickerDialog(
            strings = strings,
            candidates = amountCandidates,
            onAmountSelected = { candidate ->
                amount = candidate.displayAmount
                if (description.isBlank()) {
                    description = strings.receiptDescription
                }
                errorMessage = strings.ocrAmountApplied
                amountCandidates = emptyList()
            },
            onDismiss = { amountCandidates = emptyList() }
        )
    }
}

@Composable
private fun ReceiptCameraDialog(
    strings: AddExpenseStrings,
    onDismiss: () -> Unit,
    onImageCaptured: (File) -> Unit,
    onCaptureError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            shape = RoundedCornerShape(30.dp),
            color = CardSurface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.ocrCaptureTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink
                        )
                        Text(
                            text = strings.ocrCaptureHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftAccent)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = strings.close, tint = Ink)
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    factory = { viewContext ->
                        PreviewView(viewContext).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER

                            cameraProviderFuture.addListener(
                                {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also { preview ->
                                        preview.setSurfaceProvider(surfaceProvider)
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

                                    imageCapture = capture
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture
                                    )
                                },
                                ContextCompat.getMainExecutor(viewContext)
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        val file = File(
                            context.cacheDir,
                            "receipt_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    onImageCaptured(file)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    onCaptureError()
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.ocrTakePhoto, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptAmountPickerDialog(
    strings: AddExpenseStrings,
    candidates: List<ReceiptAmountCandidate>,
    onAmountSelected: (ReceiptAmountCandidate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(
                text = strings.ocrAmountsTitle,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = strings.ocrAmountsMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
                candidates.forEach { candidate ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAmountSelected(candidate) },
                        shape = RoundedCornerShape(16.dp),
                        color = SoftAccent
                    ) {
                        Text(
                            text = candidate.displayAmount,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel)
            }
        }
    )
}

@Composable
private fun AddExpenseHeader(strings: AddExpenseStrings, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.addExpenseHeaderTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = strings.addExpenseHeaderSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardSurface)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = strings.close, tint = Ink)
        }
    }
}

@Composable
private fun AddExpenseFormCard(
    strings: AddExpenseStrings,
    isEditing: Boolean,
    amount: String,
    description: String,
    note: String,
    dateInput: String,
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    errorMessage: String?,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAddCategoryClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) {
                    strings.editExpenseTitle
                } else {
                    strings.addExpenseTitle
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text(strings.amountLabel(currentCurrencySymbol())) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(strings.expenseDescriptionLabel) },
                placeholder = { Text(strings.expenseDescriptionPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = strings.categoryLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(186.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(categories.take(5), key = { it.id }) { category ->
                    CategoryTile(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
                item {
                    AddCategoryTile(strings = strings, onClick = onAddCategoryClick)
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = onDateChange,
                    label = { Text(strings.dateLabel) },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDatePickerClick
                        )
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text(strings.notesOptionalLabel) },
                placeholder = { Text(strings.notesOptionalPlaceholder) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(text = it, color = Danger, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) {
                        strings.saveChanges
                    } else {
                        strings.addExpenseTitle
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onScanReceiptClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, PrimaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent)
            ) {
                Icon(
                    Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = strings.scanReceipt,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = budzetkoCategoryColor(
        categoryId = category.id,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) categoryColor.copy(alpha = 0.58f) else SoftAccent
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (category.emoji.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF050505))
                    )
                }
            } else {
                Text(text = category.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Text(text = category.name, style = MaterialTheme.typography.bodySmall, color = Ink)
        }
    }
}

@Composable
private fun AddCategoryTile(strings: AddExpenseStrings, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SoftAccent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PrimaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            }
            Text(text = strings.addCategoryShort, style = MaterialTheme.typography.bodySmall, color = Ink)
        }
    }
}

private data class AddExpenseStrings(
    val ocrScanning: String,
    val ocrNoAmounts: String,
    val ocrFailed: String,
    val ocrAmountApplied: String,
    val ocrCameraPermissionMessage: String,
    val receiptDescription: String,
    val invalidAmount: String,
    val missingDescription: String,
    val missingCategory: String,
    val invalidDate: String,
    val choose: String,
    val cancel: String,
    val close: String,
    val ocrCaptureTitle: String,
    val ocrCaptureHint: String,
    val ocrTakePhoto: String,
    val ocrAmountsTitle: String,
    val ocrAmountsMessage: String,
    val addExpenseHeaderTitle: String,
    val addExpenseHeaderSubtitle: String,
    val addExpenseTitle: String,
    val editExpenseTitle: String,
    val amountLabelTemplate: String,
    val expenseDescriptionLabel: String,
    val expenseDescriptionPlaceholder: String,
    val categoryLabel: String,
    val dateLabel: String,
    val notesOptionalLabel: String,
    val notesOptionalPlaceholder: String,
    val saveChanges: String,
    val scanReceipt: String,
    val addCategoryShort: String
) {
    fun amountLabel(symbol: String): String = amountLabelTemplate.replace("%1\$s", symbol)

    companion object {
        fun from(context: Context): AddExpenseStrings = AddExpenseStrings(
            ocrScanning = context.getString(R.string.ocr_scanning),
            ocrNoAmounts = context.getString(R.string.ocr_no_amounts),
            ocrFailed = context.getString(R.string.ocr_failed),
            ocrAmountApplied = context.getString(R.string.ocr_amount_applied),
            ocrCameraPermissionMessage = context.getString(R.string.ocr_camera_permission_message),
            receiptDescription = context.getString(R.string.ocr_default_description),
            invalidAmount = context.getString(R.string.expense_error_invalid_amount),
            missingDescription = context.getString(R.string.expense_error_missing_description),
            missingCategory = context.getString(R.string.expense_error_missing_category),
            invalidDate = context.getString(R.string.expense_error_invalid_date),
            choose = context.getString(R.string.choose),
            cancel = context.getString(R.string.cancel),
            close = context.getString(R.string.close),
            ocrCaptureTitle = context.getString(R.string.ocr_capture_title),
            ocrCaptureHint = context.getString(R.string.ocr_capture_hint),
            ocrTakePhoto = context.getString(R.string.ocr_take_photo),
            ocrAmountsTitle = context.getString(R.string.ocr_amounts_title),
            ocrAmountsMessage = context.getString(R.string.ocr_amounts_message),
            addExpenseHeaderTitle = context.getString(R.string.add_expense_header_title),
            addExpenseHeaderSubtitle = context.getString(R.string.add_expense_header_subtitle),
            addExpenseTitle = context.getString(R.string.add_expense_title),
            editExpenseTitle = context.getString(R.string.edit_expense_title),
            amountLabelTemplate = context.resources.getText(R.string.amount_label).toString(),
            expenseDescriptionLabel = context.getString(R.string.expense_description_label),
            expenseDescriptionPlaceholder = context.getString(R.string.expense_description_placeholder),
            categoryLabel = context.getString(R.string.category_label),
            dateLabel = context.getString(R.string.date_label),
            notesOptionalLabel = context.getString(R.string.notes_optional_label),
            notesOptionalPlaceholder = context.getString(R.string.notes_optional_placeholder),
            saveChanges = context.getString(R.string.save_changes),
            scanReceipt = context.getString(R.string.scan_receipt),
            addCategoryShort = context.getString(R.string.add_category_short)
        )
    }
}

private fun parseDate(value: String): LocalDate? {
    return try {
        LocalDate.parse(value, DateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

private data class ReceiptAmountCandidate(
    val displayAmount: String,
    val value: Double
)

private fun extractReceiptAmountCandidates(text: String): List<ReceiptAmountCandidate> {
    val regex = Regex("""(?<!\d)\d{1,5}(?:[.,]\d{3})*[,.]\d{2}(?!\d)""")

    return regex.findAll(text)
        .mapNotNull { match ->
            val normalized = normalizeReceiptAmount(match.value)
            val value = normalized.toDoubleOrNull()
            value?.takeIf { it > 0.0 }?.let {
                ReceiptAmountCandidate(
                    displayAmount = "%.2f".format(java.util.Locale.US, it),
                    value = it
                )
            }
        }
        .distinctBy { it.displayAmount }
        .sortedByDescending { it.value }
        .take(8)
        .toList()
}

private fun normalizeReceiptAmount(rawAmount: String): String {
    val lastComma = rawAmount.lastIndexOf(',')
    val lastDot = rawAmount.lastIndexOf('.')
    val decimalSeparator = if (lastComma > lastDot) ',' else '.'

    return rawAmount
        .filter { it.isDigit() || it == decimalSeparator }
        .replace(decimalSeparator, '.')
}

private fun persistReceiptImage(context: Context, source: File): String? {
    val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
    val destination = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")

    return runCatching {
        source.copyTo(destination, overwrite = true)
        destination.absolutePath
    }.getOrNull()
}
