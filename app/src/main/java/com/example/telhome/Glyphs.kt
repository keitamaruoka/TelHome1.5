package com.example.telhome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/*
 * F-04F のテレフォンホーム風の大タイル用アイコン（白い塗りのシンプルな形）。
 * 座標は 100×100 の枠で考えて、実際の大きさに合わせて拡大する。
 * 封筒のフタの線や電話帳の人型は「くり抜き」なので、タイルの質感が透けて見える。
 */

private const val White = 0xFFFFFFFF

/** 受話器＋ダイヤルキー（F-04F実機の表示に合わせた形） */
@Composable
fun PhoneGlyph(size: Dp, modifier: Modifier = Modifier) {
    GlyphCanvas(size, modifier) { u ->
        val white = Color(White)
        // 受話器のアーチ：細めの弧
        val handset = Path().apply {
            moveTo(u(19f), u(31f))
            cubicTo(u(21f), u(10f), u(79f), u(10f), u(81f), u(31f))
        }
        drawPath(handset, white, style = Stroke(width = u(8f), cap = StrokeCap.Butt))
        // 受話器の両端：外側・下向きに傾いた角ばった塊
        rotate(-38f, pivot = Offset(u(14.5f), u(32f))) {
            drawRoundRect(white, Offset(u(3f), u(25f)), Size(u(23f), u(14f)), CornerRadius(u(1.5f)))
        }
        rotate(38f, pivot = Offset(u(85.5f), u(32f))) {
            drawRoundRect(white, Offset(u(74f), u(25f)), Size(u(23f), u(14f)), CornerRadius(u(1.5f)))
        }
        // ダイヤルキー 3×3
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                drawRoundRect(
                    white,
                    Offset(u(26f + c * 18f), u(45f + r * 17.5f)),
                    Size(u(12f), u(11.5f)),
                    CornerRadius(u(1f)),
                )
            }
        }
    }
}

/** 封筒（フタの V 字はくり抜き） */
@Composable
fun MailGlyph(size: Dp, modifier: Modifier = Modifier) {
    GlyphCanvas(size, modifier) { u ->
        drawRoundRect(Color(White), Offset(u(6f), u(20f)), Size(u(88f), u(62f)), CornerRadius(u(7f)))
        val flap = Path().apply {
            moveTo(u(13f), u(27f))
            lineTo(u(50f), u(57f))
            lineTo(u(87f), u(27f))
        }
        drawPath(
            flap,
            Color.Black,
            style = Stroke(width = u(6.5f), cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = BlendMode.Clear,
        )
    }
}

/** 電話帳（人型はくり抜き、右側にインデックスのツメ） */
@Composable
fun ContactsGlyph(size: Dp, modifier: Modifier = Modifier) {
    GlyphCanvas(size, modifier) { u ->
        val white = Color(White)
        // 表紙
        drawRoundRect(white, Offset(u(14f), u(11f)), Size(u(58f), u(78f)), CornerRadius(u(6f)))
        // 人型（頭と肩）をくり抜く
        drawCircle(Color.Black, radius = u(10f), center = Offset(u(43f), u(39f)), blendMode = BlendMode.Clear)
        drawRoundRect(
            Color.Black,
            Offset(u(27f), u(53f)),
            Size(u(32f), u(16f)),
            CornerRadius(u(12f)),
            blendMode = BlendMode.Clear,
        )
        // インデックスのツメ 5つ
        for (i in 0 until 5) {
            drawRoundRect(white, Offset(u(77f), u(14f + i * 15f)), Size(u(13f), u(9.5f)), CornerRadius(u(2.5f)))
        }
    }
}

/** 100×100 の座標で描くためのキャンバス。くり抜き（Clear）が効くよう別レイヤーで描く */
@Composable
private fun GlyphCanvas(size: Dp, modifier: Modifier, draw: DrawScope.(u: (Float) -> Float) -> Unit) {
    Canvas(
        modifier
            .size(size)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        val scale = this.size.minDimension / 100f
        draw { it * scale }
    }
}
