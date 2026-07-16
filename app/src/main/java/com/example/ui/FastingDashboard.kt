package com.example.ui

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FastingMilestone
import com.example.data.FastingSession
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun FastingDashboard(
    viewModel: FastingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    val elapsedMillis by viewModel.currentElapsedMillis.collectAsState()
    val levelStats by viewModel.userLevelStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Tracker, 1 = Research/Milestones, 2 = History
    var showMilestoneDetail by remember { mutableStateOf<FastingMilestone?>(null) }
    var showEndSessionDialog by remember { mutableStateOf(false) }
    var showAddManualSessionDialog by remember { mutableStateOf(false) }
    var showOverallProgressDialog by remember { mutableStateOf(false) }

    // State for temporary custom times during Start/End adjustments
    var customStartTime by remember { mutableStateOf<Long?>(null) }
    var customEndTime by remember { mutableStateOf<Long?>(null) }

    // Helper to format timestamps
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = DeepSlateSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Tracker") },
                    label = { Text("Tracker", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonGreen,
                        selectedTextColor = NeonGreen,
                        unselectedIconColor = SoftGrayText,
                        unselectedTextColor = SoftGrayText,
                        indicatorColor = CardSlate
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Research") },
                    label = { Text("Science", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonBlue,
                        selectedTextColor = NeonBlue,
                        unselectedIconColor = SoftGrayText,
                        unselectedTextColor = SoftGrayText,
                        indicatorColor = CardSlate
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmGold,
                        selectedTextColor = WarmGold,
                        unselectedIconColor = SoftGrayText,
                        unselectedTextColor = SoftGrayText,
                        indicatorColor = CardSlate
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with App Title & Level Profile
            HeaderSection(levelStats = levelStats)

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content depending on Selected Tab
            androidx.compose.animation.AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { fullWidth -> if (targetState > initialState) fullWidth else -fullWidth },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ).togetherWith(androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { fullWidth -> if (targetState > initialState) -fullWidth else fullWidth },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ))
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> {
                        TrackerTab(
                            activeSession = activeSession,
                            elapsedMillis = elapsedMillis,
                            levelStats = levelStats,
                            onStartClick = {
                                customStartTime = System.currentTimeMillis()
                                viewModel.startFasting(customStartTime!!)
                            },
                            onEndClick = {
                                customStartTime = activeSession?.startTime
                                customEndTime = System.currentTimeMillis()
                                showEndSessionDialog = true
                            },
                            onAdjustStart = {
                                val currentStart = activeSession?.startTime ?: System.currentTimeMillis()
                                showDateTimePicker(context, currentStart) { newTime ->
                                    viewModel.adjustActiveStartTime(newTime)
                                }
                            },
                            onMilestoneClick = { milestone ->
                                showMilestoneDetail = milestone
                            },
                            onOverallProgressClick = {
                                showOverallProgressDialog = true
                            }
                        )
                    }
                    1 -> {
                        ScienceTab(
                            elapsedMillis = if (activeSession != null) elapsedMillis else 0L,
                            onMilestoneClick = { milestone ->
                                showMilestoneDetail = milestone
                            }
                        )
                    }
                    2 -> {
                        HistoryTab(
                            completedSessions = completedSessions,
                            onDeleteClick = { session -> viewModel.deleteSession(session) },
                            onClearAllClick = { viewModel.clearAllHistory() },
                            onAddManualClick = {
                                customStartTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(16)
                                customEndTime = System.currentTimeMillis()
                                showAddManualSessionDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS & OVERLAYS ---

    // 1. Milestone details sheet
    showMilestoneDetail?.let { milestone ->
        MilestoneDetailDialog(
            milestone = milestone,
            onDismiss = { showMilestoneDetail = null }
        )
    }

    // 1b. Overall Progress Dialog
    if (showOverallProgressDialog) {
        Dialog(onDismissRequest = { showOverallProgressDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fasting Journey",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val elapsedHours = (if (activeSession != null) elapsedMillis else 0L).toDouble() / 3600000.0
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FastingMilestone.ALL_MILESTONES) { milestone ->
                            val isReached = elapsedHours >= milestone.hourStart
                            val isCurrent = elapsedHours >= milestone.hourStart && elapsedHours < milestone.hourEnd
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isCurrent) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isReached) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isReached) NeonGreen else SoftGrayText,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = milestone.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isReached) Color.White else SoftGrayText,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${milestone.hourStart}h",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isReached) NeonGreen else SoftGrayText
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showOverallProgressDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CardSlate, contentColor = Color.White)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // 2. End Session with Optional DateTime Adjustment
    if (showEndSessionDialog) {
        Dialog(onDismissRequest = { showEndSessionDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Success Medal",
                        tint = WarmGold,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Awesome Achievement!",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You are taking control of your cellular health. Below you can optionally customize the precise start and end times for this fast.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGrayText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Time display and buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSlate, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Time Adjuster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("FAST STARTED", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
                                Text(
                                    text = "${dateFormatter.format(Date(customStartTime ?: 0))} ${timeFormatter.format(Date(customStartTime ?: 0))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDateTimePicker(context, customStartTime ?: System.currentTimeMillis()) { newTime ->
                                        customStartTime = newTime
                                    }
                                },
                                modifier = Modifier.testTag("adjust_start_btn")
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Edit Start Time", tint = NeonGreen)
                            }
                        }

                        Divider(color = BorderGray, thickness = 0.5.dp)

                        // End Time Adjuster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("FAST ENDED", style = MaterialTheme.typography.labelSmall, color = NeonBlue)
                                Text(
                                    text = "${dateFormatter.format(Date(customEndTime ?: 0))} ${timeFormatter.format(Date(customEndTime ?: 0))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDateTimePicker(context, customEndTime ?: System.currentTimeMillis()) { newTime ->
                                        customEndTime = newTime
                                    }
                                },
                                modifier = Modifier.testTag("adjust_end_btn")
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Edit End Time", tint = NeonBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val finalDuration = (customEndTime ?: System.currentTimeMillis()) - (customStartTime ?: System.currentTimeMillis())
                    val hours = finalDuration.toDouble() / (1000.0 * 60.0 * 60.0)
                    val expectedXp = viewModel.calculateXpForSession(finalDuration)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmGold.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "XP", tint = WarmGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Earned Rewards:", style = MaterialTheme.typography.bodyMedium, color = WarmGold, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "+$expectedXp Health XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEndSessionDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGrayText),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                showEndSessionDialog = false
                                viewModel.endFastingAndSave(customStartTime, customEndTime)
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_session_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = ObsidianBlack)
                        ) {
                            Text("Save Fast", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 3. Add Manual Session Dialog
    if (showAddManualSessionDialog) {
        Dialog(onDismissRequest = { showAddManualSessionDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddModerator,
                        contentDescription = "Manual Log",
                        tint = NeonBlue,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Log Past Fasting Session",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Manually record a fasting window to maintain your weekly streaks and earn Health XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGrayText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSlate, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Time Adjuster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("START DATE & TIME", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
                                Text(
                                    text = "${dateFormatter.format(Date(customStartTime ?: 0))} ${timeFormatter.format(Date(customStartTime ?: 0))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDateTimePicker(context, customStartTime ?: System.currentTimeMillis()) { newTime ->
                                        customStartTime = newTime
                                    }
                                }
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Edit Start Time", tint = NeonGreen)
                            }
                        }

                        Divider(color = BorderGray, thickness = 0.5.dp)

                        // End Time Adjuster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("END DATE & TIME", style = MaterialTheme.typography.labelSmall, color = NeonBlue)
                                Text(
                                    text = "${dateFormatter.format(Date(customEndTime ?: 0))} ${timeFormatter.format(Date(customEndTime ?: 0))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDateTimePicker(context, customEndTime ?: System.currentTimeMillis()) { newTime ->
                                        customEndTime = newTime
                                    }
                                }
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Edit End Time", tint = NeonBlue)
                            }
                        }

                        // Target hours slider or text removed

                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val duration = (customEndTime ?: System.currentTimeMillis()) - (customStartTime ?: System.currentTimeMillis())
                    val manualXp = viewModel.calculateXpForSession(duration)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeonBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, NeonBlue.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expected Rewards:", style = MaterialTheme.typography.bodyMedium, color = NeonBlue, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "+$manualXp Health XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddManualSessionDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGrayText),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (customStartTime != null && customEndTime != null) {
                                    viewModel.saveCompletedSessionDirectly(customStartTime!!, customEndTime!!)
                                }
                                showAddManualSessionDialog = false
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = ObsidianBlack)
                        ) {
                            Text("Log Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SECTIONS & COMPOSABLES ---

@Composable
fun HeaderSection(levelStats: UserLevelStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // App Name & Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fasting Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = levelStats.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Streak display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(WarmGold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Fire",
                        tint = WarmGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${levelStats.streakDays} Day Streak",
                        color = WarmGold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leveling System Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL ${levelStats.level}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen
                )
                Text(
                    text = "${levelStats.xpInCurrentLevel} / ${levelStats.xpForNextLevel} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGrayText,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = levelStats.levelProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = NeonGreen,
                trackColor = BorderGray
            )
        }
    }
}

@Composable
fun TrackerTab(
    activeSession: FastingSession?,
    elapsedMillis: Long,
    levelStats: UserLevelStats,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onAdjustStart: () -> Unit,
    onMilestoneClick: (FastingMilestone) -> Unit,
    onOverallProgressClick: () -> Unit
) {
    val isFastingActive = activeSession != null

    val elapsedHours = elapsedMillis.toDouble() / (1000.0 * 60.0 * 60.0)

    // Identify current and next milestones
    val currentMilestone = remember(elapsedHours) {
        FastingMilestone.getMilestoneForHours(elapsedHours)
    }
    val nextMilestone = remember(elapsedHours) {
        FastingMilestone.ALL_MILESTONES.firstOrNull { it.hourStart > elapsedHours }
    }

    // Determine target based on the next milestone
    val targetHours = nextMilestone?.hourStart ?: (elapsedHours.toInt() + 1).toDouble() // Just keep sweeping if no milestones left
    val prevMilestoneHours = currentMilestone?.hourStart ?: 0.0
    val progressPercent = if (targetHours > prevMilestoneHours) {
        ((elapsedHours - prevMilestoneHours) / (targetHours - prevMilestoneHours)).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgressPercent by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
        label = "progressAnimation"
    )

    // Format hours, minutes, seconds cleanly
    val formattedTime = remember(elapsedMillis) {
        val hours = elapsedMillis / 3600000
        val minutes = (elapsedMillis % 3600000) / 60000
        val seconds = (elapsedMillis % 60000) / 1000
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large Circular Countdown Timer Widget
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .clickable {
                    onOverallProgressClick()
                }
                .testTag("circular_timer_button")
        ) {
            // Neon sweep gradient ring drawn on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val center = size.center
                val radius = (size.minDimension - strokeWidth) / 2

                // Background Ring Track
                drawCircle(
                    color = BorderGray,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Active glowing ring arc
                if (isFastingActive) {
                    val sweepAngle = animatedProgressPercent * 360f
                    val arcBrush = Brush.sweepGradient(
                        colors = listOf(NeonBlue, NeonGreen, NeonBlue)
                    )
                    drawArc(
                        brush = arcBrush,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Inside text contents
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        !isFastingActive -> Icons.Default.Restaurant
                        else -> Icons.Default.LocalFireDepartment
                    },
                    contentDescription = "Status Icon",
                    tint = when {
                        !isFastingActive -> SoftGrayText
                        else -> NeonGreen
                    },
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isFastingActive) formattedTime else "00:00:00",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        !isFastingActive -> "EATING WINDOW"
                        else -> "FASTING ACTIVE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        !isFastingActive -> SoftGrayText
                        else -> NeonGreen
                    },
                    fontWeight = FontWeight.ExtraBold
                )

                if (isFastingActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextMilestone?.let { "Next: ${it.title} (${(progressPercent * 100).toInt()}%)" } ?: "Max Level Reached!",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGrayText,
                        fontWeight = FontWeight.Bold
                    )
                    if (nextMilestone != null) {
                        val remainingHours = nextMilestone.hourStart - elapsedHours
                        val remainingMillis = (remainingHours * 3600000).toLong()
                        val rHours = remainingMillis / 3600000
                        val rMins = (remainingMillis % 3600000) / 60000
                        Text(
                            text = String.format("in %02dh %02dm", rHours, rMins),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = SoftGrayText.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap for Journey",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = SoftGrayText.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Play/Pause notification banner or starting config
        androidx.compose.animation.AnimatedContent(
            targetState = isFastingActive,
            label = "FastingControlsTransition"
        ) { active ->
            if (!active) {
                // Inactive Fast configuration view
                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_fast_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Fast")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START FASTING NOW", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            } else {
                // Active Fast Controls: One-click play/pause and End/Adjust actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // End fast action
                    Button(
                        onClick = onEndClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("end_fast_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Fast")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("END SESSION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Adjust start time live link
                    Row(
                        modifier = Modifier
                            .background(CardSlate, RoundedCornerShape(8.dp))
                            .clickable { onAdjustStart() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Time", tint = SoftGrayText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val startFormatter = remember { SimpleDateFormat("EEE hh:mm a", Locale.getDefault()) }
                        Text(
                            text = "Started at ${startFormatter.format(Date(activeSession?.startTime ?: System.currentTimeMillis()))} (Adjust)",
                            color = SoftGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Milestones Gamified Section (Highlighting upcoming biological level-ups)
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE BIOLOGICAL STEP",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tap to Learn Why",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGrayText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                currentMilestone?.let { milestone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSlate)
                            .clickable { onMilestoneClick(milestone) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getMilestoneIcon(milestone.iconName),
                                contentDescription = milestone.title,
                                tint = getMilestoneColor(milestone.colorAccentType),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${milestone.hourStart}h-${milestone.hourEnd}h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getMilestoneColor(milestone.colorAccentType),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = milestone.benefitSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftGrayText
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Detail",
                            tint = SoftGrayText
                        )
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSlate, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start fasting to activate your cellular milestones!",
                            color = SoftGrayText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Next upcoming milestone indicator
                if (isFastingActive && nextMilestone != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val hoursToNext = nextMilestone.hourStart - elapsedHours
                    val formattedNextTime = if (hoursToNext > 0) {
                        val wholeHours = hoursToNext.toInt()
                        val minutes = ((hoursToNext - wholeHours) * 60).toInt()
                        if (wholeHours > 0) "${wholeHours}h ${minutes}m" else "${minutes}m"
                    } else "0m"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Next Milestone",
                            tint = NeonBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next: ${nextMilestone.title} in $formattedNextTime",
                            color = SoftGrayText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScienceTab(
    elapsedMillis: Long,
    onMilestoneClick: (FastingMilestone) -> Unit
) {
    val elapsedHours = elapsedMillis.toDouble() / (1000.0 * 60.0 * 60.0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FASTING TIMELINE & HEALTH BENEFITS",
            style = MaterialTheme.typography.labelMedium,
            color = NeonBlue,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Scientific Research Upfront",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Track your body's cellular transitions in real time. Tap any stage to explore detailed physiological mechanisms and medical study citations.",
            style = MaterialTheme.typography.bodySmall,
            color = SoftGrayText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Render the 10 Milestones with gamified status
        FastingMilestone.ALL_MILESTONES.forEach { milestone ->
            val isUnlocked = elapsedHours >= milestone.hourEnd
            val isActive = elapsedHours >= milestone.hourStart && elapsedHours < milestone.hourEnd

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) CardSlate else DeepSlateSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isActive -> getMilestoneColor(milestone.colorAccentType)
                        isUnlocked -> getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.3f)
                        else -> BorderGray
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onMilestoneClick(milestone) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Status / Hour Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(52.dp)
                    ) {
                        Text(
                            text = "${milestone.hourStart.toInt()}h",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (isUnlocked || isActive) Color.White else SoftGrayText
                        )
                        Text(
                            text = "START",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftGrayText
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked || isActive) Color.White else SoftGrayText
                        )
                        Text(
                            text = milestone.benefitSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActive) Color.White.copy(alpha = 0.8f) else SoftGrayText,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right status lock icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.2f)
                                    isUnlocked -> getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.1f)
                                    else -> CardSlate
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isUnlocked -> Icons.Default.CheckCircle
                                isActive -> Icons.Default.Autorenew
                                else -> Icons.Default.Lock
                            },
                            contentDescription = "Status",
                            tint = when {
                                isUnlocked -> getMilestoneColor(milestone.colorAccentType)
                                isActive -> getMilestoneColor(milestone.colorAccentType)
                                else -> SoftGrayText
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTab(
    completedSessions: List<FastingSession>,
    onDeleteClick: (FastingSession) -> Unit,
    onClearAllClick: () -> Unit,
    onAddManualClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, MMM dd 'at' hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FASTING LOGS & RECORDS",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGold,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Row {
                IconButton(onClick = onAddManualClick) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Past Fast", tint = NeonGreen)
                }
                if (completedSessions.isNotEmpty()) {
                    IconButton(onClick = onClearAllClick) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = SoftGrayText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NeonBlue.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = "Cloud Sync", tint = NeonBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Auto-Sync Enabled", style = MaterialTheme.typography.titleSmall, color = NeonBlue, fontWeight = FontWeight.Bold)
                    Text(
                        "Your fasting progress and levels are securely synced across all your devices using Google Drive Auto Backup (Free). Just make sure you are logged into your Google Account.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGrayText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (completedSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Empty History", tint = SoftGrayText, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No fasting records found",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed fasting windows will be recorded here to calculate XP rewards and levels.",
                        color = SoftGrayText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            completedSessions.forEach { session ->
                val durationHours = session.getElapsedMillis(System.currentTimeMillis()).toDouble() / (1000.0 * 60.0 * 60.0)

                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                    border = BorderStroke(1.dp, BorderGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Side - Duration Stats
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%.1fh", durationHours),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Ended: ${formatter.format(Date(session.endTime ?: 0))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftGrayText
                            )
                        }

                        // Right Side - XP Earned and Action
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+${session.xpEarned} XP",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarmGold
                            )

                            IconButton(onClick = { onDeleteClick(session) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = SoftGrayText.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneDetailDialog(
    milestone: FastingMilestone,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getMilestoneIcon(milestone.iconName),
                            contentDescription = milestone.title,
                            tint = getMilestoneColor(milestone.colorAccentType),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(getMilestoneColor(milestone.colorAccentType).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${milestone.hourStart}h - ${milestone.hourEnd}h",
                            color = getMilestoneColor(milestone.colorAccentType),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = milestone.benefitSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getMilestoneColor(milestone.colorAccentType),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = BorderGray, thickness = 0.5.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Biological Changes Section
                Text(
                    text = "BODY TRANSITIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = milestone.biologicalExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scientific Details Section
                Text(
                    text = "CELLULAR MECHANISMS",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonBlue,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = milestone.scientificDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scientific Citations & Sources Section
                Text(
                    text = "CLINICAL RESEARCH & SOURCES",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGold,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                milestone.sources.forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Citation",
                            tint = WarmGold,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = source,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGrayText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_detail_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CardSlate, contentColor = Color.White)
                ) {
                    Text("Got it, thank you!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ICON & COLOR UTILITY CORRELATIONS ---

fun getMilestoneColor(colorType: String): Color {
    return when (colorType) {
        "green" -> NeonGreen
        "blue" -> NeonBlue
        "orange" -> AmberOrange
        "gold" -> WarmGold
        else -> NeonGreen
    }
}

@Composable
fun getMilestoneIcon(iconName: String): ImageVector {
    return when (iconName) {
        "TrendingDown" -> Icons.Default.TrendingDown
        "Percent" -> Icons.Default.Percent
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "Speed" -> Icons.Default.Speed
        "Psychology" -> Icons.Default.Psychology
        "CleaningServices" -> Icons.Default.CleaningServices
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Shield" -> Icons.Default.Shield
        "Vaccines" -> Icons.Default.Vaccines
        else -> Icons.Default.Star
    }
}

/**
 * Custom DateTime Picker trigger wrapping standard system components
 */
fun showDateTimePicker(
    context: Context,
    initialTimestamp: Long,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimestamp }

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}
