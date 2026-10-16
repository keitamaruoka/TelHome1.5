@file:OptIn(ExperimentalFoundationApi::class)

package com.example.telhome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gap = 5.dp

/** 中央のホーム画面 */
@Composable
fun HomeScreen(
    slots: List<Slot>,
    apps: List<AppInfo>,
    onSlotLongPress: (Int) -> Unit,
    onMenu: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TelColors.HomeBgTop, TelColors.Background))),
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 8.dp),
        ) {
            SearchBar(apps)
            Spacer(Modifier.height(Gap + 3.dp))

            // 大きいタイル：電話（左）、メール・電話帳（右）
            Row(Modifier.weight(1f).fillMaxWidth()) {
                BigTile(
                    feature = Feature.DIALER,
                    top = TelColors.PhoneTop,
                    bottom = TelColors.PhoneBottom,
                    iconSize = 104,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                Spacer(Modifier.width(Gap))
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    BigTile(
                        feature = Feature.MAIL,
                        top = TelColors.MailTop,
                        bottom = TelColors.MailBottom,
                        iconSize = 70,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Gap))
                    BigTile(
                        feature = Feature.CONTACTS,
                        top = TelColors.ContactsTop,
                        bottom = TelColors.ContactsBottom,
                        iconSize = 70,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(Gap))

            // 小さいタイル 4列×3段（長押しで割り当て変更）
            Column(Modifier.weight(1.2f).fillMaxWidth()) {
                for (row in 0 until SlotStore.ROWS) {
                    if (row > 0) Spacer(Modifier.height(Gap))
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        for (col in 0 until SlotStore.COLUMNS) {
                            if (col > 0) Spacer(Modifier.width(Gap))
                            val index = row * SlotStore.COLUMNS + col
                            SmallSlotTile(
                                slot = slots.getOrElse(index) { Slot.Empty },
                                apps = apps,
                                onClick = { slot ->
                                    when (slot) {
                                        is Slot.Builtin -> slot.feature.launch(context)
                                        is Slot.App -> Launch.app(context, slot.pkg, slot.cls)
                                        Slot.Empty -> onSlotLongPress(index)
                                    }
                                },
                                onLongClick = { onSlotLongPress(index) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
        MenuStrip(onMenu)
    }
}

/** 検索バーの白文字（明るい背景でも読めるよう薄い影付き） */
private val SearchTextStyle = TextStyle(
    color = Color.White,
    fontSize = 21.sp,
    fontWeight = FontWeight.Bold,
    shadow = Shadow(color = Color(0x99000000), offset = Offset(0f, 1.5f), blurRadius = 3f),
)

/** 上部の検索バー：入力して確定すると Firefox で検索する */
@Composable
private fun SearchBar(apps: List<AppInfo>) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var query by remember { mutableStateOf("") }

    fun submit() {
        FirefoxSearch.search(context, query.trim())
        query = ""
        keyboard?.hide()
        focus.clearFocus()
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFC2C5CC), TelColors.SearchBar)))
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text("Firefox", style = SearchTextStyle)
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = SearchTextStyle,
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { submit() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Search, contentDescription = "検索", tint = Color(0xFF555A63), modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BigTile(
    feature: Feature,
    top: Color,
    bottom: Color,
    iconSize: Int,
    modifier: Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier
            .tileSurface(top, bottom, texture = 0.16f, sheen = 0.08f)
            .clickable { feature.launch(context) },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (feature) {
                Feature.DIALER -> PhoneGlyph(iconSize.dp)
                Feature.MAIL -> MailGlyph(iconSize.dp)
                Feature.CONTACTS -> ContactsGlyph(iconSize.dp)
                else -> Icon(feature.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(feature.label, color = Color.White, fontSize = 22.sp)
        }
    }
}

@Composable
private fun SmallSlotTile(
    slot: Slot,
    apps: List<AppInfo>,
    onClick: (Slot) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier,
) {
    // アンインストール済みのアプリは空き枠として表示
    val app = (slot as? Slot.App)?.let { s -> apps.firstOrNull { it.pkg == s.pkg && it.cls == s.cls } }
    val effective = if (slot is Slot.App && app == null && apps.isNotEmpty()) Slot.Empty else slot
    val empty = effective == Slot.Empty

    Box(
        modifier
            .then(
                if (empty) {
                    Modifier.tileSurface(TelColors.SmallTileEmptyTop, TelColors.SmallTileEmpty, texture = 0.06f, sheen = 0.05f)
                } else {
                    Modifier.tileSurface(TelColors.SmallTileTop, TelColors.SmallTile, texture = 0.06f, sheen = 0.07f)
                },
            )
            .combinedClickable(onClick = { onClick(effective) }, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            app != null ->
                TileContent(label = app.label) { BitmapIcon(app.icon, 54) }
            effective is Slot.App -> Unit // アプリ一覧の読み込み待ち
            else ->
                Icon(Icons.Filled.Add, contentDescription = "追加", tint = Color.White.copy(alpha = 0.18f), modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun TileContent(label: String, icon: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun VectorBadge(icon: ImageVector, color: Color, size: Int = 34) {
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 6).dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.85f), color))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size((size * 0.62f).dp))
    }
}

@Composable
fun BitmapIcon(bitmap: ImageBitmap, size: Int = 36) {
    Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(size.dp))
}

@Composable
private fun MenuStrip(onMenu: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(TelColors.MenuStrip),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(180.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TelColors.MenuButton)
                .clickable(onClick = onMenu),
            contentAlignment = Alignment.Center,
        ) {
            Text("MENU", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}
