package com.elsharif.dailyseventy.presentaion.prayertimes

import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentaion.common.OutlinedRow
import com.elsharif.dailyseventy.presentaion.common.PrimaryColorDivider
import com.elsharif.dailyseventy.presentaion.common.maps.MapView
import com.elsharif.dailyseventy.presentaion.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentaion.prayertimes.model.UiPrayerTimesAuthority
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val TAG = "PrayerTimesPage"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrayerTimesPage(viewModel: PrayerTimeViewModel = hiltViewModel()) {

    val currentLocation by viewModel.currentLocationFlow.collectAsState(GeoPoint(30.0, 30.0))
    val selectedDate by viewModel.currentDateFlow.collectAsState(LocalDate.now())
    val prayerTimesAuthorities by viewModel.prayerTimesAuthoritiesFlow.collectAsState(listOf())
    val selectedAuthority by viewModel.currentPrayerAuthorityFlow.collectAsState(
        UiPrayerTimesAuthority(-1, "")
    )
    val prayerTimes by viewModel.prayerTimesFlow.collectAsState(listOf())

    PrayerTimesViews(
        currentLocation = currentLocation,
        pickedDate = selectedDate.toString(),
        selectedAuthority = selectedAuthority,
        prayerTimesAuthorities = prayerTimesAuthorities,
        prayerTimes = prayerTimes,
        onMapClick = { viewModel.updateLocation(it) },
        onDateChange = { viewModel.setDate(it) },
        onAuthorityChange = { viewModel.updateAuthority(it) }
    )
}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerTimesViews(
    currentLocation: GeoPoint,
    pickedDate: String,
    selectedAuthority: UiPrayerTimesAuthority,
    prayerTimesAuthorities: List<UiPrayerTimesAuthority>,
    prayerTimes: List<UiPrayerTime>,
    onMapClick: (GeoPoint) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onAuthorityChange: (UiPrayerTimesAuthority) -> Unit
) {

    val datePickerState = rememberUseCaseState()
    val authorityDialogListState = rememberUseCaseState()
    val authorityListOptions =
        prayerTimesAuthorities.map {
            ListOption(
                titleText = it.name, selected = it == selectedAuthority && it.name.isNotEmpty()
            )
        }


    CalendarDialog(
        state = datePickerState,
        selection = CalendarSelection.Date(onSelectDate = onDateChange)
    )

    ListDialog(state = authorityDialogListState,
        header = Header.Default(stringResource(id = R.string.prayers_time_authorization)),

        selection = ListSelection.Single(options = authorityListOptions) { index, _ ->
            onAuthorityChange(prayerTimesAuthorities[index])
            Log.d(TAG, "PrayerTimesViews: $prayerTimesAuthorities $index")
        })


    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {


             Column(
                 Modifier
                     .fillMaxWidth()
                     .height(280.dp)
                     .clipToBounds()
             ) {
                 MapView(
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(280.dp),
                     onMapClick = { onMapClick(it) },
                     currentLocation = currentLocation
                 )
             }

             Spacer(modifier = Modifier.height(20.dp))

             Text(
                 text = stringResource(R.string.pick_location_acurrately),
                 fontSize = 16.sp,
                 fontWeight = FontWeight.ExtraBold,
                 textAlign = TextAlign.Center,
                 letterSpacing = 0.1.sp,
                 modifier = Modifier
                     .padding(top = 3.dp)
                     .fillMaxWidth()
             )
             OutlinedRow(
                 Modifier
                     .padding(top = 19.dp)
                     .padding(horizontal = 12.dp)
                     .clickable { datePickerState.show() }
             ) {
                 Text(text = pickedDate, modifier = Modifier.padding(horizontal = 16.dp))
                 Spacer(modifier = Modifier.weight(1f))
                 Image(painter = painterResource(id = R.drawable.ic_calender), contentDescription = null)
             }


             OutlinedRow(
                 Modifier
                     .padding(top = 14.dp)
                     .padding(horizontal = 12.dp)
                     .clickable { authorityDialogListState.show() }

             ) {
                 Text(text = selectedAuthority.name, modifier = Modifier.padding(horizontal = 16.dp))
                 Spacer(modifier = Modifier.weight(1f))
                 Image(
                     imageVector = Icons.Default.KeyboardArrowDown,
                     contentDescription = null,
                     modifier = Modifier
                         .padding(8.dp)
                 )
             }

             Spacer(modifier = Modifier.height(10.dp))



        prayerTimes.forEach {
            PrayerTimeListItem(
                it
            )
        }


    }
}

// I need to find
@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun PrayerTimeListItem(uiPrayerTime: UiPrayerTime) {
    var remainingTime by remember { mutableStateOf("00:00:00") }
    val fontSize = 14.sp
    val currentTime = LocalTime.now()
    var remainingDuration =
        Duration.between(
            currentTime,
            LocalTime.parse(uiPrayerTime.time, DateTimeFormatter.ofPattern("hh:mm a"))
        )
    if (remainingDuration.isNegative) remainingDuration = Duration.ZERO


    var timer by remember { mutableStateOf(remainingDuration) }
    LaunchedEffect(key1 = timer) {

        if (timer > Duration.ZERO) {
            delay(1_000)
            timer = timer.minus(Duration.ofSeconds(1))
            val hours = remainingDuration.toHours()
            val minutes = remainingDuration.minusHours(hours).toMinutes()
            val seconds = remainingDuration.minusHours(hours).minusMinutes(minutes).seconds

            remainingTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        }
    }

    Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                val d: Drawable? =
                    AppCompatResources.getDrawable(LocalContext.current, uiPrayerTime.iconRes)

                Image(
                    bitmap = d!!.toBitmap().asImageBitmap(),
                    contentDescription = uiPrayerTime.name,
                    modifier
                    = Modifier
                        .scale(2f)
                        .padding(4.dp)
                        .weight(.1f)
                )
                Text(
                    text = uiPrayerTime.name,
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(.50f)
                )

                Text(
                    text = uiPrayerTime.time,
                    fontSize = fontSize,
                    modifier = Modifier.weight(.50f)
                )

                Text(
                    text = remainingTime,
                    fontSize = fontSize,
                    modifier = Modifier.weight(.50f),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.stopwatch), contentDescription = null,
                )
            }
            PrimaryColorDivider(horizontalPadding = 10.dp)

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, locale = "AR")
@Composable
private fun PrayerTimesPreview() {
    var currentLocation by remember { mutableStateOf(GeoPoint(0.0, 0.0)) }
    var currentDate by remember {
        mutableStateOf(LocalDate.now())
    }
    val auths by remember {
        mutableStateOf(
            listOf(
                UiPrayerTimesAuthority(1, "My Auth"),
                UiPrayerTimesAuthority(2, "afa")
            )
        )
    }
    var selectedAuthority by remember {
        mutableStateOf(auths.first())
    }
    PrayerTimesViews(
        currentLocation = currentLocation,
        pickedDate = currentDate.toString(),
        selectedAuthority = selectedAuthority,
        prayerTimesAuthorities = auths,
        prayerTimes = listOf(
            UiPrayerTime(R.drawable.ic_calender, "asr", "20:20", "20"),
            UiPrayerTime(R.drawable.ic_calender, "asr", "20:20", "20"),
            UiPrayerTime(R.drawable.ic_calender, "asr", "20:20", "20"),
            UiPrayerTime(R.drawable.ic_calender, "asr", "20:20", "20"),
            UiPrayerTime(R.drawable.ic_calender, "asr", "20:20", "20"),
        ),
        onMapClick = { currentLocation = it },
        onDateChange = { currentDate = it },
        onAuthorityChange = { selectedAuthority = it }
    )
}