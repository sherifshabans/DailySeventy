package com.elsharif.dailyseventy.presentation.travel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R

// ══════════════════════════════════════════════════════════════════════════════
//  ISLAMIC RULINGS CARD  –  أحكام المسافر (المذهب الشافعي)
//  مستخلص من الدرسين ٣٩ و٤٠ – عمدة السالك وعدة الناسك
//  معهد الإمام النووي للتفقه الشافعي
// ══════════════════════════════════════════════════════════════════════════════

// ─── Data using StringRes ─────────────────────────────────────────────────────

private data class IslamicRuling(
    val id: String,
    val icon: String,
    val titleRes: Int,
    val summaryRes: Int,
    val color: Color,
    val details: List<RulingDetail>
)

private data class RulingDetail(
    val labelRes: Int,
    val detailRes: Int,
    val isNote: Boolean = false
)

private val TRAVELER_RULINGS = listOf(

    // ─────────────────────────────────────────────────────────────────
    // ١. شروط جواز القصر (٨ شروط)
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "qasr_conditions",
        icon = "📏",
        titleRes = R.string.rulings_qasr_conditions_title,
        summaryRes = R.string.rulings_qasr_conditions_summary,
        color = Color(0xFF1565C0),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_condition_1,
                detailRes = R.string.rulings_condition_1_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_2,
                detailRes = R.string.rulings_condition_2_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_3,
                detailRes = R.string.rulings_condition_3_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_4,
                detailRes = R.string.rulings_condition_4_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_5,
                detailRes = R.string.rulings_condition_5_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_6,
                detailRes = R.string.rulings_condition_6_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_7,
                detailRes = R.string.rulings_condition_7_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_condition_8,
                detailRes = R.string.rulings_condition_8_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_conditions_required_completion,
                detailRes = R.string.rulings_conditions_required_completion_detail,
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٢. القصر أم الإتمام؟
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "qasr_better",
        icon = "⚖️",
        titleRes = R.string.rulings_qasr_better_title,
        summaryRes = R.string.rulings_qasr_better_summary,
        color = Color(0xFF2E7D32),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_qasr_default,
                detailRes = R.string.rulings_qasr_default_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_qasr_better_4,
                detailRes = R.string.rulings_qasr_better_4_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_qasr_obligatory_one,
                detailRes = R.string.rulings_qasr_obligatory_one_detail
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٣. متى ينتهي السفر؟
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "end_of_travel",
        icon = "🏁",
        titleRes = R.string.rulings_end_title,
        summaryRes = R.string.rulings_end_summary,
        color = Color(0xFFC62828),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_end_scenario1,
                detailRes = R.string.rulings_end_scenario1_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_end_scenario2,
                detailRes = R.string.rulings_end_scenario2_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_end_scenario3,
                detailRes = R.string.rulings_end_scenario3_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_end_exception,
                detailRes = R.string.rulings_end_exception_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_end_day_rule,
                detailRes = R.string.rulings_end_day_rule_detail,
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٤. أقسام العصاة في السفر
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "usat",
        icon = "⚠️",
        titleRes = R.string.rulings_sinners_title,
        summaryRes = R.string.rulings_sinners_summary,
        color = Color(0xFFE65100),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_sinners_category1,
                detailRes = R.string.rulings_sinners_category1_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_sinners_category2,
                detailRes = R.string.rulings_sinners_category2_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_sinners_category3,
                detailRes = R.string.rulings_sinners_category3_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_sinners_note,
                detailRes = R.string.rulings_sinners_note_detail,
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٥. شروط جمع التقديم
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "taqdeem",
        icon = "⬆️",
        titleRes = R.string.rulings_taqdeem_title,
        summaryRes = R.string.rulings_taqdeem_summary,
        color = Color(0xFF6A1B9A),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_condition1,
                detailRes = R.string.rulings_taqdeem_condition1_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_condition2,
                detailRes = R.string.rulings_taqdeem_condition2_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_condition3,
                detailRes = R.string.rulings_taqdeem_condition3_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_condition4,
                detailRes = R.string.rulings_taqdeem_condition4_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_caveats,
                detailRes = R.string.rulings_taqdeem_caveats_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_taqdeem_sunnah_note,
                detailRes = R.string.rulings_taqdeem_sunnah_note_detail,
                isNote = true
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٦. شروط جمع التأخير
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "takheer",
        icon = "⬇️",
        titleRes = R.string.rulings_takheer_title,
        summaryRes = R.string.rulings_takheer_summary,
        color = Color(0xFF00695C),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_takheer_condition1,
                detailRes = R.string.rulings_takheer_condition1_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_takheer_condition2,
                detailRes = R.string.rulings_takheer_condition2_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_takheer_diff_from_taqdeem,
                detailRes = R.string.rulings_takheer_diff_from_taqdeem_detail
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٧. مقارنة التقديم والتأخير
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "diff",
        icon = "↔️",
        titleRes = R.string.rulings_comparison_title,
        summaryRes = R.string.rulings_comparison_summary,
        color = Color(0xFF37474F),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_comparison_time_of_intention,
                detailRes = R.string.rulings_comparison_time_of_intention_taqdeem
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_time_of_intention,
                detailRes = R.string.rulings_comparison_time_of_intention_takheer
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_duration_of_excuse,
                detailRes = R.string.rulings_comparison_duration_of_excuse_taqdeem
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_duration_of_excuse,
                detailRes = R.string.rulings_comparison_duration_of_excuse_takheer
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_consecutiveness_order,
                detailRes = R.string.rulings_comparison_consecutiveness_order_taqdeem
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_consecutiveness_order,
                detailRes = R.string.rulings_comparison_consecutiveness_order_takheer
            ),
            RulingDetail(
                labelRes = R.string.rulings_comparison_which_better,
                detailRes = R.string.rulings_comparison_which_better_detail
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٨. فطر رمضان
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "fitr",
        icon = "🌙",
        titleRes = R.string.rulings_fitr_title,
        summaryRes = R.string.rulings_fitr_summary,
        color = Color(0xFF6A1B9A),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_fitr_permits,
                detailRes = R.string.rulings_fitr_permits_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_fitr_makeup,
                detailRes = R.string.rulings_fitr_makeup_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_fitr_start_then_travel,
                detailRes = R.string.rulings_fitr_start_then_travel_detail
            )
        )
    ),

    // ─────────────────────────────────────────────────────────────────
    // ٩. المسح على الخفين
    // ─────────────────────────────────────────────────────────────────
    IslamicRuling(
        id = "mash",
        icon = "💧",
        titleRes = R.string.rulings_mash_title,
        summaryRes = R.string.rulings_mash_summary,
        color = Color(0xFF00695C),
        details = listOf(
            RulingDetail(
                labelRes = R.string.rulings_mash_duration,
                detailRes = R.string.rulings_mash_duration_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_mash_start,
                detailRes = R.string.rulings_mash_start_detail
            ),
            RulingDetail(
                labelRes = R.string.rulings_mash_invalidators,
                detailRes = R.string.rulings_mash_invalidators_detail
            )
        )
    )
)

// ══════════════════════════════════════════════════════════════════════════════
//  COMPOSABLES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun IslamicRulingsCard() {
    var expandedSection by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {

            // ── Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(0.12f), CircleShape),
                    Alignment.Center
                ) { Text("📚", fontSize = 20.sp) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        stringResource(R.string.rulings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.rulings_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // ── Rulings list
            TRAVELER_RULINGS.forEachIndexed { index, ruling ->
                RulingItem(
                    ruling = ruling,
                    expanded = expandedSection == ruling.id,
                    onToggle = {
                        expandedSection =
                            if (expandedSection == ruling.id) null else ruling.id
                    }
                )
                if (index < TRAVELER_RULINGS.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun RulingItem(
    ruling: IslamicRuling,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        // ── Row header
        Surface(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(ruling.color.copy(0.10f), RoundedCornerShape(10.dp)),
                    Alignment.Center
                ) { Text(ruling.icon, fontSize = 18.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(ruling.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ruling.color
                    )
                    Text(
                        stringResource(ruling.summaryRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ruling.color.copy(0.7f)
                )
            }
        }

        // ── Expanded body
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                ruling.details.forEach { item ->
                    if (item.isNote) {
                        // Note box (different styling)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ruling.color.copy(0.06f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    stringResource(item.labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ruling.color
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(item.detailRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Normal detail item (expandable)
                        ExpandableDetailItem(item = item, accentColor = ruling.color)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExpandableDetailItem(item: RulingDetail, accentColor: Color) {
    var open by remember { mutableStateOf(false) }

    Surface(
        onClick = { open = !open },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(if (open) 0.09f else 0.04f)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(item.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = accentColor.copy(0.6f)
                )
            }

            AnimatedVisibility(
                visible = open,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = accentColor.copy(0.15f))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(item.detailRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}