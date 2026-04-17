package com.elsharif.dailyseventy.presentation.mohamedlovers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ExperimentalMaterial3Api
import com.elsharif.dailyseventy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MohamedLoversScreen(
    onBackClick: () -> Unit,
    viewModel: MohamedLoversViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.flushPendingSession()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.flushPendingSession()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf { it.isNotBlank() }?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.screen_mohamed_lovers)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MohamedLoversFridayNote(state.bannerMessage)
            }

            item {
                MohamedLoversStatusCard(
                    statusMessage = state.statusMessage,
                    networkTimeLabel = state.networkTimeLabel,
                    alias = state.alias,
                )
            }

            item {
                MohamedLoversCounterCard(
                    syncedTotal = state.syncedTotal,
                    sessionClicks = state.sessionClicks,
                    canCount = state.canCount,
                    isSavingSession = state.isSavingSession,
                    onCountClick = viewModel::onCountClick,
                )
            }

            if (state.isWinner && state.winnerCode.isNotBlank()) {
                item {
                    MohamedLoversWinnerCodeCard(
                        winnerCode = state.winnerCode,
                        onCopy = { winnerCode ->
                            clipboardManager.setText(AnnotatedString(winnerCode))
                            Toast.makeText(context, "تم نسخ كود الفوز.", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }

            item {
                MohamedLoversLeaderboardCard(
                    currentRank = state.currentRank,
                    leaderboard = state.topFive,
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MohamedLoversFridayNote(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun MohamedLoversStatusCard(
    statusMessage: String,
    networkTimeLabel: String,
    alias: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "حالة المسابقة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = statusMessage, style = MaterialTheme.typography.bodyMedium)
            if (networkTimeLabel.isNotBlank()) {
                Text(
                    text = "وقت الشبكة: $networkTimeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "اسمك في الترتيب: $alias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MohamedLoversCounterCard(
    syncedTotal: Int,
    sessionClicks: Int,
    canCount: Boolean,
    isSavingSession: Boolean,
    onCountClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = "إجمالي هذا الجهاز",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${syncedTotal + sessionClicks}",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "غير المرفوع بعد: $sessionClicks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "يتم رفع الضغطات إلى Firebase عند إغلاق الشاشة أو خروج التطبيق.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Button(
                onClick = onCountClick,
                enabled = canCount && !isSavingSession,
            ) {
                if (isSavingSession) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "اضغط لمحبة النبي +1")
                }
            }
        }
    }
}

@Composable
private fun MohamedLoversWinnerCodeCard(
    winnerCode: String,
    onCopy: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "كود الفوز",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (winnerCode.isBlank()) {
                    "لو تم اختيارك كفائز سيظهر هنا كود الاستلام لتأخذه إلى صاحب التطبيق."
                } else {
                    winnerCode
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (winnerCode.isNotBlank()) {
                IconButton(onClick = { onCopy(winnerCode) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun MohamedLoversLeaderboardCard(
    currentRank: Int?,
    leaderboard: List<MohamedLoversLeaderboardEntry>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "أفضل 5 محبين",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = currentRank?.let { "ترتيبك الحالي: #$it" } ?: "ترتيبك سيظهر بعد مزامنة أول جلسة.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (leaderboard.isEmpty()) {
                Text(
                    text = "لا توجد نتائج بعد. ابدأ أول جلسة وسيظهر الترتيب هنا.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Column
            }

            leaderboard.forEachIndexed { index, entry ->
                if (index > 0) {
                    Divider()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "#${entry.rank} ${entry.alias}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                        )
                        Text(
                            text = "الإجمالي: ${entry.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (entry.isCurrentUser) {
                        Text(
                            text = "أنت",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(999.dp),
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
