package com.example.telhome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** MENU画面の12項目（フィーチャーフォンのメニュー風 3列×4段） */
enum class MenuItem(val label: String, val icon: ImageVector, val feature: Feature? = null) {
    MAIL("メール", Feature.MAIL.icon, Feature.MAIL),
    BROWSER("インターネット", Feature.BROWSER.icon, Feature.BROWSER),
    APPS("アプリ", Icons.Filled.Apps),
    CAMERA("カメラ", Feature.CAMERA.icon, Feature.CAMERA),
    GALLERY("ギャラリー", Feature.GALLERY.icon, Feature.GALLERY),
    TOOLS("便利ツール", Icons.Filled.Handyman),
    CALL_SETTINGS("電話機能", Feature.CALL_SETTINGS.icon, Feature.CALL_SETTINGS),
    SETTINGS("本体設定", Feature.SETTINGS.icon, Feature.SETTINGS),
    MAPS("地図", Feature.MAPS.icon, Feature.MAPS),
    CALENDAR("スケジュール", Feature.CALENDAR.icon, Feature.CALENDAR),
    PROFILE("プロフィール", Feature.PROFILE.icon, Feature.PROFILE),
    DOCUMENTS("ドキュメント\nビューワー", Icons.Filled.Description),
}

@Composable
fun MenuScreen(onSelect: (MenuItem) -> Unit, onClose: () -> Unit) {
    val items = MenuItem.entries
    Column(
        Modifier
            .fillMaxSize()
            .background(TelColors.MenuGap)
            .blockTouches() // 下のホームにタッチが抜けないように
            .padding(6.dp),
    ) {
        for (row in 0 until 4) {
            if (row > 0) Spacer(Modifier.height(4.dp))
            Row(Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0 until 3) {
                    if (col > 0) Spacer(Modifier.width(4.dp))
                    val item = items[row * 3 + col]
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(TelColors.MenuTile)
                            .clickable { onSelect(item) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(item.icon, contentDescription = null, tint = Color(0xFFF4F2EC), modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                item.label,
                                color = Color(0xFFE6E4DE),
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(TelColors.MenuTile)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Text("閉じる", color = Color.White, fontSize = 20.sp)
        }
    }
}

/** 便利ツール（電卓・アラーム・ライト） */
@Composable
fun ToolsDialog(
    torchOn: Boolean,
    onFeature: (Feature) -> Unit,
    onTorch: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("閉じる") } },
        title = { Text("便利ツール") },
        text = {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                ToolButton("電卓", { VectorBadge(Feature.CALCULATOR.icon, Feature.CALCULATOR.color, 52) }) {
                    onFeature(Feature.CALCULATOR)
                }
                ToolButton("アラーム", { VectorBadge(Feature.ALARM.icon, Feature.ALARM.color, 52) }) {
                    onFeature(Feature.ALARM)
                }
                ToolButton(if (torchOn) "ライト消灯" else "ライト", {
                    VectorBadge(
                        if (torchOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                        if (torchOn) Color(0xFFE0A800) else Color(0xFF6A6F7A),
                        52,
                    )
                }, onTorch)
            }
        },
    )
}

@Composable
private fun ToolButton(label: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

/** ホームの小タイルに割り当てるものを選ぶ */
@Composable
fun SlotPickerDialog(
    apps: List<AppInfo>,
    onPick: (Slot) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("タイルに割り当てる") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
        dismissButton = {
            Row {
                TextButton(onClick = onResetDefault) { Text("標準に戻す") }
                TextButton(onClick = { onPick(Slot.Empty) }) { Text("空にする") }
            }
        },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(apps, key = { it.pkg + "/" + it.cls }) { app ->
                    PickerRow(app.label, { BitmapIcon(app.icon, 40) }) { onPick(Slot.App(app.pkg, app.cls)) }
                }
            }
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun PickerRow(label: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** 重ねた画面の下にタッチが伝わらないようにする */
private fun Modifier.blockTouches(): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
    )
}
