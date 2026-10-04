package com.example.memp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.memp.data.local.entity.MemoEntity
import com.example.memp.ui.viewmodel.MemoViewModel
import com.example.memp.util.MemoAiParser
import com.example.memp.util.ReminderScheduler
import com.example.memp.util.SpeechToTextManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MemoFilterTab(val label: String) {
    ALL("전체"),
    PINNED("고정 메모"),
    FAVORITE("중요 메모")
}

val CATEGORY_LIST = listOf("일반", "건강", "장보기", "모임", "금융")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoListScreen(
    viewModel: MemoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val memos by viewModel.memos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var selectedFilter by remember { mutableStateOf(MemoFilterTab.ALL) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingMemo by remember { mutableStateOf<MemoEntity?>(null) }

    val filteredMemos = remember(memos, selectedFilter) {
        when (selectedFilter) {
            MemoFilterTab.ALL -> memos
            MemoFilterTab.PINNED -> memos.filter { it.isPinned }
            MemoFilterTab.FAVORITE -> memos.filter { it.isFavorite }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "맘모스", 
                            fontSize = 30.sp, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "총 ${memos.size}개의 메모가 있어요", 
                            fontSize = 16.sp, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingMemo = null
                    showBottomSheet = true
                },
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = "새 메모 작성",
                        modifier = Modifier.size(32.dp)
                    ) 
                },
                text = { 
                    Text(
                        text = "새 메모", 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.padding(12.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 미니멀 검색 바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                placeholder = { 
                    Text("찾으실 메모를 입력하세요...", fontSize = 20.sp, color = MaterialTheme.colorScheme.outline) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "검색", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear, 
                                contentDescription = "검색 지우기",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                textStyle = TextStyle(fontSize = 22.sp),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 필터 칩 목록
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MemoFilterTab.entries.toTypedArray()) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { 
                            Text(
                                text = filter.label, 
                                fontSize = 18.sp, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.height(44.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 메모 목록 및 Empty State
            if (filteredMemos.isEmpty()) {
                EmptyMemoView(
                    filter = selectedFilter,
                    hasSearchQuery = searchQuery.isNotEmpty()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredMemos, key = { it.id }) { memo ->
                        MemoCardItem(
                            memo = memo,
                            onClick = {
                                editingMemo = memo
                                showBottomSheet = true
                            },
                            onTogglePin = { viewModel.togglePin(memo) },
                            onToggleFavorite = { viewModel.toggleFavorite(memo) },
                            onDelete = { viewModel.deleteMemo(memo.id) }
                        )
                    }
                }
            }
        }
    }

    // 모던 바텀 시트 (STT, AI 분석, AlarmManager 단계별 알림 연동)
    if (showBottomSheet) {
        MemoEditBottomSheet(
            memo = editingMemo,
            onDismiss = { showBottomSheet = false },
            onSave = { title, content, category, targetTime ->
                if (editingMemo == null) {
                    viewModel.addMemo(title = title, content = content, category = category) { insertedId ->
                        if (targetTime != null) {
                            ReminderScheduler.scheduleStepwiseReminders(
                                context = context,
                                memoId = insertedId,
                                title = title,
                                contentText = content,
                                targetTimeMillis = targetTime
                            )
                            Toast.makeText(context, "스마트 사전 알림이 설정되었습니다! ⏰", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    viewModel.updateMemo(
                        editingMemo!!.copy(
                            title = title,
                            content = content,
                            category = category
                        )
                    )
                    if (targetTime != null) {
                        ReminderScheduler.scheduleStepwiseReminders(
                            context = context,
                            memoId = editingMemo!!.id,
                            title = title,
                            contentText = content,
                            targetTimeMillis = targetTime
                        )
                        Toast.makeText(context, "스마트 알림이 갱신되었습니다! ⏰", Toast.LENGTH_SHORT).show()
                    }
                }
                showBottomSheet = false
            }
        )
    }
}

@Composable
fun MemoCardItem(
    memo: MemoEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(memo.updatedAt) {
        SimpleDateFormat("yyyy년 M월 d일 a h:mm", Locale.KOREAN).format(Date(memo.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (memo.isPinned) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) 
            else 
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (memo.isPinned) 4.dp else 2.dp
        ),
        border = if (memo.isPinned) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) else null
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (memo.isPinned) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📌 상단 고정됨",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = memo.category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (memo.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "상단 고정",
                            modifier = Modifier.size(28.dp),
                            tint = if (memo.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (memo.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "중요 표시",
                            modifier = Modifier.size(28.dp),
                            tint = if (memo.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "메모 삭제",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AutoSizeText(
                text = memo.title.ifEmpty { "제목 없음" },
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = memo.content,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formattedDate,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    var textSize by remember { mutableStateOf(26.sp) }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontWeight = fontWeight,
        fontSize = textSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && textSize > 20.sp) {
                textSize *= 0.9f
            }
        }
    )
}

@Composable
fun EmptyMemoView(
    filter: MemoFilterTab,
    hasSearchQuery: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val emptyMessage = when {
                hasSearchQuery -> "검색 결과와 일치하는 메모가 없습니다."
                filter == MemoFilterTab.PINNED -> "상단 고정된 메모가 없습니다."
                filter == MemoFilterTab.FAVORITE -> "중요 표시된 메모가 없습니다."
                else -> "작성된 메모가 없습니다.\n아래 '+ 새 메모' 버튼을 눌러보세요!"
            }
            
            Text(
                text = emptyMessage,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 32.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoEditBottomSheet(
    memo: MemoEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long?) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(memo?.title ?: "") }
    var content by remember { mutableStateOf(memo?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(memo?.category ?: "일반") }
    var reminderString by remember { mutableStateOf<String?>(null) }
    var targetTimeMillis by remember { mutableStateOf<Long?>(null) }

    val sttManager = remember { SpeechToTextManager(context) }
    val isListening by sttManager.isListening.collectAsState()
    val speechResult by sttManager.speechResult.collectAsState()
    val errorMessage by sttManager.errorMessage.collectAsState()

    // 오디오 녹음 권한 요청 런치
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sttManager.startListening()
        } else {
            Toast.makeText(context, "음성 인식을 위해 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 알림 권한 요청 런치 (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "알림 권한을 허용하지 않으면 사전 알림을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // STT 음성 인식 결과 감지 및 AI 자동 분석 파싱
    LaunchedEffect(speechResult) {
        if (speechResult.isNotBlank()) {
            val aiResult = MemoAiParser.parse(speechResult)
            title = aiResult.title
            content = aiResult.contentText
            selectedCategory = aiResult.category
            reminderString = aiResult.reminderDisplayString
            targetTimeMillis = aiResult.targetTimeMillis
            Toast.makeText(context, "AI가 음성을 메모로 정돈했어요! ✨", Toast.LENGTH_SHORT).show()
            sttManager.clearResult()
        }
    }

    // 에러 메세지 토스트
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 마이크 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    ModalBottomSheet(
        onDismissRequest = {
            sttManager.stopListening()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (memo == null) "새 메모 작성" else "메모 수정하기",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(onClick = {
                    sttManager.stopListening()
                    onDismiss()
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.Clear, 
                        contentDescription = "닫기",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 대형 STT 음성 입력 버튼
            Surface(
                onClick = {
                    if (isListening) {
                        sttManager.stopListening()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            sttManager.startListening()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = if (isListening) Color(0xFFE53935) else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                border = if (isListening) BorderStroke(2.dp, Color.Red) else null
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "말로 쓰기",
                        modifier = Modifier
                            .size(36.dp)
                            .scale(if (isListening) micScale else 1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isListening) "🔴 말씀하세요... 듣고 있어요" else "🎤 말로 편하게 말하기",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // AI 추출 알림 시간 배지
            AnimatedVisibility(
                visible = reminderString != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                reminderString?.let { timeText ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⏰ AI 감지 알림 예정: $timeText",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 카테고리 선택 칩 그룹
            Text(text = "카테고리 선택", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORY_LIST) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(text = category, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 제목 입력
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목", fontSize = 20.sp) },
                placeholder = { Text("메모 제목을 입력하세요", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { title = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "지우기")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 내용 입력
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용 및 할 일", fontSize = 20.sp) },
                placeholder = { Text("내용을 입력하세요...", fontSize = 20.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                textStyle = TextStyle(fontSize = 22.sp, lineHeight = 32.sp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 저장 버튼
            Button(
                onClick = { 
                    sttManager.stopListening()
                    onSave(title, content, selectedCategory, targetTimeMillis) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = title.isNotBlank() || content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (memo == null) "메모 저장하기" else "수정 완료",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}