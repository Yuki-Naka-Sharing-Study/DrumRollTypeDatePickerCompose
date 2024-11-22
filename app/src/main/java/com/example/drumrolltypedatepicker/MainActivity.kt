package com.example.drumrolltypedatepicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drumrolltypedatepicker.ui.theme.DrumRollTypeDatePickerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrumRollTypeDatePickerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrumRollTypeDatePickerScreen()
                }
            }
        }
    }
}

@Composable
fun DrumRollTypeDatePickerScreen() {
    var selectedDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        DrumRollTypeDatePicker { date ->
            selectedDate = date
        }
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("日付 (年-月-日)") },
            readOnly = true
        )
    }
}

@Composable
fun DrumRollTypeDatePicker(onDateSelected: (String) -> Unit) {
    var isPickerVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("日付を選択") }

    Box(contentAlignment = Alignment.Center) {
        Column() {
            Button(onClick = { isPickerVisible = true }) {
                Text(text = selectedDate)
            }
        }

        if (isPickerVisible) {
            DatePickerDialog(
                onDismiss = { isPickerVisible = false },
                onDateSelected = { year, month, day ->
                    selectedDate = "$year 年 $month 月 $day 日"
                    onDateSelected(selectedDate)
                    isPickerVisible = false
                }
            )
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2016..2100).toList()
    val months = (1..12).toList()
    val days = (1..31).toList()

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { onDateSelected(selectedYear, selectedMonth, selectedDay) }) {
                    Text("確定")
                }
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("日付を選択")
            }
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScrollPicker(
                    items = years,
                    selectedItem = selectedYear,
                    onItemSelected = { selectedYear = it },
                    highlightColor = Color.Red
                )
                ScrollPicker(
                    items = months,
                    selectedItem = selectedMonth,
                    onItemSelected = { selectedMonth = it },
                    highlightColor = Color.Green
                )
                ScrollPicker(
                    items = days,
                    selectedItem = selectedDay,
                    onItemSelected = { selectedDay = it },
                    highlightColor = Color.Blue
                )
            }
        }
    )
}

@Composable
fun <T> ScrollPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    highlightColor: Color
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 初期位置にスクロール
    LaunchedEffect(items, selectedItem) {
        val initialIndex = items.indexOf(selectedItem).coerceAtLeast(0)
        listState.scrollToItem(initialIndex)
    }

    // スクロール停止時の選択更新
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    val firstVisibleIndex = listState.firstVisibleItemIndex
                    val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset
                    val visibleItems = listState.layoutInfo.visibleItemsInfo

                    if (visibleItems.isNotEmpty()) {
                        val centerIndex = if (firstVisibleItemOffset > (visibleItems.first().size / 2)) {
                            firstVisibleIndex + 5
                        } else {
                            firstVisibleIndex
                        }.coerceIn(0, items.lastIndex)

                        onItemSelected(items[centerIndex])
                    }
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .height(150.dp)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val item = items[index]
            Text(
                text = item.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                            onItemSelected(item)
                        }
                    },
                style = if (item == selectedItem) {
                    MaterialTheme.typography.bodyLarge.copy(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                } else {
                    MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                }
            )
        }
    }
}