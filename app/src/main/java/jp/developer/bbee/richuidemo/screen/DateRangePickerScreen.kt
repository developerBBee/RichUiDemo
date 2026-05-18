package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import java.util.Calendar
import java.util.Locale

private val DAY_LABELS = listOf("日", "月", "火", "水", "木", "金", "土")

data class CalendarDate(val year: Int, val month: Int, val day: Int) : Comparable<CalendarDate> {
    override fun compareTo(other: CalendarDate): Int = when {
        year != other.year -> year - other.year
        month != other.month -> month - other.month
        else -> day - other.day
    }
}

private fun CalendarDate.toDisplayString() =
    String.format(Locale.JAPAN, "%d年%02d月%02d日", year, month, day)

private fun today(): CalendarDate {
    val cal = Calendar.getInstance()
    return CalendarDate(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
    )
}

private fun daysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun firstDayOfWeekOffset(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)
}

private fun prevMonth(year: Int, month: Int) =
    if (month == 1) year - 1 to 12 else year to month - 1

private fun nextMonth(year: Int, month: Int) =
    if (month == 12) year + 1 to 1 else year to month + 1

private enum class SelectionPhase { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerScreen(onBack: () -> Unit) {
    val todayDate = remember { today() }

    var displayYear by remember { mutableStateOf(todayDate.year) }
    var displayMonth by remember { mutableStateOf(todayDate.month) }

    var fromDate by remember { mutableStateOf<CalendarDate?>(null) }
    var toDate by remember { mutableStateOf<CalendarDate?>(null) }
    var phase by remember { mutableStateOf(SelectionPhase.FROM) }
    var confirmedRange by remember { mutableStateOf<Pair<CalendarDate, CalendarDate>?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "日付範囲選択",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DateRangeHeader(
                fromDate = fromDate,
                toDate = toDate,
                phase = phase,
                onFromClick = { phase = SelectionPhase.FROM },
                onToClick = { phase = SelectionPhase.TO },
            )

            CalendarCard(
                year = displayYear,
                month = displayMonth,
                today = todayDate,
                fromDate = fromDate,
                toDate = toDate,
                onPrevMonth = {
                    val (y, m) = prevMonth(displayYear, displayMonth)
                    displayYear = y; displayMonth = m
                },
                onNextMonth = {
                    val (y, m) = nextMonth(displayYear, displayMonth)
                    displayYear = y; displayMonth = m
                },
                onDayClick = { date ->
                    when (phase) {
                        SelectionPhase.FROM -> {
                            fromDate = date
                            toDate = null
                            phase = SelectionPhase.TO
                        }
                        SelectionPhase.TO -> {
                            if (fromDate != null && date < fromDate!!) {
                                toDate = fromDate
                                fromDate = date
                            } else {
                                toDate = date
                            }
                            phase = SelectionPhase.FROM
                        }
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = {
                        fromDate = null
                        toDate = null
                        confirmedRange = null
                        phase = SelectionPhase.FROM
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("リセット")
                }
                Button(
                    onClick = {
                        if (fromDate != null && toDate != null) {
                            confirmedRange = fromDate!! to toDate!!
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = fromDate != null && toDate != null,
                ) {
                    Text("確定")
                }
            }

            confirmedRange?.let { (from, to) ->
                ConfirmedRangeCard(from = from, to = to)
            }
        }
    }
}

@Composable
private fun DateRangeHeader(
    fromDate: CalendarDate?,
    toDate: CalendarDate?,
    phase: SelectionPhase,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateChip(
            modifier = Modifier.weight(1f),
            label = "FROM",
            date = fromDate,
            isActive = phase == SelectionPhase.FROM,
            pulseAlpha = pulse,
            onClick = onFromClick,
        )
        DateChip(
            modifier = Modifier.weight(1f),
            label = "TO",
            date = toDate,
            isActive = phase == SelectionPhase.TO,
            pulseAlpha = pulse,
            onClick = onToClick,
        )
    }
}

@Composable
private fun DateChip(
    modifier: Modifier = Modifier,
    label: String,
    date: CalendarDate?,
    isActive: Boolean,
    pulseAlpha: Float,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "border",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "bg",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = date?.toDisplayString() ?: "未選択",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (date != null) FontWeight.SemiBold else FontWeight.Normal,
            color = if (date != null)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun CalendarCard(
    year: Int,
    month: Int,
    today: CalendarDate,
    fromDate: CalendarDate?,
    toDate: CalendarDate?,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (CalendarDate) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MonthNavigationRow(
                year = year,
                month = month,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DayOfWeekRow()
            Spacer(modifier = Modifier.height(4.dp))
            CalendarGrid(
                year = year,
                month = month,
                today = today,
                fromDate = fromDate,
                toDate = toDate,
                onDayClick = onDayClick,
            )
        }
    }
}

@Composable
private fun MonthNavigationRow(year: Int, month: Int, onPrevMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前月",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = String.format(Locale.JAPAN, "%d年 %02d月", year, month),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "次月",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DayOfWeekRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DAY_LABELS.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = when (index) {
                    0 -> MaterialTheme.colorScheme.error
                    6 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    today: CalendarDate,
    fromDate: CalendarDate?,
    toDate: CalendarDate?,
    onDayClick: (CalendarDate) -> Unit,
) {
    val daysInMonth = daysInMonth(year, month)
    val offset = firstDayOfWeekOffset(year, month)

    val cells = buildList<CalendarDate?> {
        repeat(offset) { add(null) }
        repeat(daysInMonth) { add(CalendarDate(year, month, it + 1)) }
        while (size % 7 != 0) add(null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        DayCell(
                            date = date,
                            today = today,
                            fromDate = fromDate,
                            toDate = toDate,
                            onClick = onDayClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: CalendarDate?,
    today: CalendarDate,
    fromDate: CalendarDate?,
    toDate: CalendarDate?,
    onClick: (CalendarDate) -> Unit,
) {
    if (date == null) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val isStart = fromDate != null && date == fromDate
    val isEnd = toDate != null && date == toDate
    val hasRange = fromDate != null && toDate != null && fromDate != toDate
    val isInRange = hasRange && date > fromDate!! && date < toDate!!
    val isToday = date == today
    val dayOfWeek = remember(date) {
        val cal = Calendar.getInstance()
        cal.set(date.year, date.month - 1, date.day)
        cal.get(Calendar.DAY_OF_WEEK)
    }
    val isSunday = dayOfWeek == Calendar.SUNDAY
    val isSaturday = dayOfWeek == Calendar.SATURDAY

    val primary = MaterialTheme.colorScheme.primary
    val rangeColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        // Range strip background (half-strips for start/end, full for in-range)
        if (hasRange) {
            when {
                isStart -> Row(modifier = Modifier.fillMaxSize().padding(vertical = 6.dp)) {
                    Spacer(modifier = Modifier.weight(0.5f))
                    Box(modifier = Modifier.weight(0.5f).fillMaxSize().background(rangeColor))
                }
                isEnd -> Row(modifier = Modifier.fillMaxSize().padding(vertical = 6.dp)) {
                    Box(modifier = Modifier.weight(0.5f).fillMaxSize().background(rangeColor))
                    Spacer(modifier = Modifier.weight(0.5f))
                }
                isInRange -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp)
                        .background(rangeColor),
                )
            }
        }

        // Selection circle
        val circleColor by animateColorAsState(
            targetValue = when {
                isStart || isEnd -> primary
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            },
            animationSpec = tween(200),
            label = "circle_${date.day}",
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleColor),
        )

        // Day number text
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isStart || isEnd || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                isSunday -> MaterialTheme.colorScheme.error
                isSaturday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

private fun julianDayNumber(date: CalendarDate): Int {
    val a = (14 - date.month) / 12
    val y = date.year + 4800 - a
    val m = date.month + 12 * a - 3
    return date.day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
}

@Composable
private fun ConfirmedRangeCard(from: CalendarDate, to: CalendarDate) {
    val days = julianDayNumber(to) - julianDayNumber(from) + 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "選択した期間",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${from.toDisplayString()}  〜  ${to.toDisplayString()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "合計 $days 日間",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}
