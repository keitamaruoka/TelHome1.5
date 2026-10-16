package com.example.telhome

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** コンセプト画像から拾った配色 */
object TelColors {
    val Background = Color(0xFF1B1F24)
    val PhoneTop = Color(0xFF3A8AA6)
    val PhoneBottom = Color(0xFF1F4C60)
    val MailTop = Color(0xFF7E2433)
    val MailBottom = Color(0xFF4A0C16)
    val ContactsTop = Color(0xFF6B7F70)
    val ContactsBottom = Color(0xFF45554B)
    val SmallTileTop = Color(0xFF666B76)
    val SmallTile = Color(0xFF41454E)
    val SmallTileEmptyTop = Color(0xFF50545D)
    val SmallTileEmpty = Color(0xFF33363D)
    val HomeBgTop = Color(0xFF262B31)
    val SearchBar = Color(0xFFA7ABB4)
    val MenuStrip = Color(0xFFC9CDD2)
    val MenuButton = Color(0xFF48433D)
    val LogHeader = Color(0xFF215D75)
    val LogPageTop = Color(0xFF34444B)
    val LogPage = Color(0xFF8E949B)
    val LogItem = Color(0x8C000000)   // 半透明の黒
    val LogDivider = Color(0x33FFFFFF)
    val CallAccent = Color(0xFF35A7CC)
    val Missed = Color(0xFFE0455B)
    val BottomBar = Color(0xFFE6E7E9)
    val MenuGap = Color(0xFF6E6F6A)
    val MenuTile = Color(0xFF34342F)
}

/** 端末の標準アプリを呼び出す機能。インテントは上から順に試す。 */
enum class Feature(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val intents: () -> List<Intent>,
) {
    DIALER("電話", Icons.Filled.Dialpad, Color(0xFF2F7E99), {
        listOf(Intent(Intent.ACTION_DIAL))
    }),
    MAIL("メール", Icons.Filled.Email, Color(0xFF8A2A3A), {
        listOf(category(Intent.CATEGORY_APP_EMAIL), Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")))
    }),
    CONTACTS("電話帳", Icons.Filled.Contacts, Color(0xFF5C6D65), {
        listOf(category(Intent.CATEGORY_APP_CONTACTS), Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI))
    }),
    MESSAGES("メッセージ", Icons.Filled.Sms, Color(0xFF6DA83B), {
        listOf(category(Intent.CATEGORY_APP_MESSAGING), Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
    }),
    BROWSER("インターネット", Icons.Filled.Language, Color(0xFF3B78C2), {
        listOf(category(Intent.CATEGORY_APP_BROWSER), Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/")))
    }),
    CAMERA("カメラ", Icons.Filled.PhotoCamera, Color(0xFF555B66), {
        listOf(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
    }),
    GALLERY("ギャラリー", Icons.Filled.PhotoLibrary, Color(0xFF3E8E9C), {
        listOf(category(Intent.CATEGORY_APP_GALLERY), Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }),
    CALENDAR("スケジュール", Icons.Filled.CalendarMonth, Color(0xFFC9483E), {
        listOf(
            category(Intent.CATEGORY_APP_CALENDAR),
            Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()),
        )
    }),
    MAPS("地図", Icons.Filled.Map, Color(0xFF4F9A5A), {
        listOf(category(Intent.CATEGORY_APP_MAPS), Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")))
    }),
    MUSIC("ミュージック", Icons.Filled.MusicNote, Color(0xFFB0558F), {
        listOf(category(Intent.CATEGORY_APP_MUSIC))
    }),
    CALCULATOR("電卓", Icons.Filled.Calculate, Color(0xFF6A6F7A), {
        listOf(category(Intent.CATEGORY_APP_CALCULATOR))
    }),
    ALARM("アラーム", Icons.Filled.Alarm, Color(0xFFCB7A2E), {
        listOf(Intent(AlarmClock.ACTION_SHOW_ALARMS))
    }),
    SETTINGS("本体設定", Icons.Filled.Settings, Color(0xFF6A6F7A), {
        listOf(Intent(Settings.ACTION_SETTINGS))
    }),
    CALL_SETTINGS("電話機能", Icons.Filled.PhoneInTalk, Color(0xFF2F7E99), {
        listOf(Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS), Intent(Intent.ACTION_DIAL))
    }),
    PROFILE("プロフィール", Icons.Filled.AccountBox, Color(0xFF5C6D65), {
        listOf(
            Intent(Intent.ACTION_VIEW, ContactsContract.Profile.CONTENT_URI),
            category(Intent.CATEGORY_APP_CONTACTS),
        )
    });

    fun launch(context: Context): Boolean {
        // メールは作成画面ではなく、メールアプリの受信トレイ（トップ画面）を開く
        if (this == MAIL && MailApp.openInbox(context)) return true
        return Launch.startAny(context, intents())
    }

    /** 担当アプリを探すためのインテント（選択画面用インテントは使わず、カテゴリを直接指定） */
    fun resolveIntents(): List<Intent> {
        val cat = when (this) {
            MAIL -> Intent.CATEGORY_APP_EMAIL
            CONTACTS -> Intent.CATEGORY_APP_CONTACTS
            MESSAGES -> Intent.CATEGORY_APP_MESSAGING
            BROWSER -> Intent.CATEGORY_APP_BROWSER
            GALLERY -> Intent.CATEGORY_APP_GALLERY
            CALENDAR -> Intent.CATEGORY_APP_CALENDAR
            MAPS -> Intent.CATEGORY_APP_MAPS
            MUSIC -> Intent.CATEGORY_APP_MUSIC
            CALCULATOR -> Intent.CATEGORY_APP_CALCULATOR
            else -> null
        }
        val byCategory = cat?.let { listOf(Intent(Intent.ACTION_MAIN).addCategory(it)) } ?: emptyList()
        val plain = intents().filter { it.selector == null }
        return byCategory + plain
    }
}

private fun category(cat: String): Intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, cat)

object Launch {
    fun start(context: Context, intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    fun startAny(context: Context, intents: List<Intent>): Boolean {
        for (intent in intents) {
            if (start(context, intent)) return true
        }
        Toast.makeText(context, "対応するアプリが見つかりません", Toast.LENGTH_SHORT).show()
        return false
    }

    fun app(context: Context, pkg: String, cls: String) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(pkg, cls)
            .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startAny(context, listOf(intent))
    }
}

/** 検索バーから Firefox で検索する */
object FirefoxSearch {
    /** 通常版・ベータ版・Nightly の順に探す */
    val PACKAGES = listOf("org.mozilla.firefox", "org.mozilla.firefox_beta", "org.mozilla.fenix")

    fun search(context: Context, query: String) {
        val pkg = PACKAGES.firstOrNull { isInstalled(context, it) }
        if (pkg == null) {
            Toast.makeText(context, "Firefoxがインストールされていません", Toast.LENGTH_SHORT).show()
            Launch.startAny(context, listOf(webSearch(query), Intent(Intent.ACTION_VIEW, googleUrl(query))))
            return
        }
        if (query.isEmpty()) {
            // 何も入力していなければ Firefox を開くだけ
            context.packageManager.getLaunchIntentForPackage(pkg)?.let { Launch.start(context, it) }
            return
        }
        Launch.startAny(
            context,
            listOf(
                // Firefox側の「既定の検索エンジン」で検索
                webSearch(query).setPackage(pkg),
                // 受け付けない場合は検索URLを Firefox で開く
                Intent(Intent.ACTION_VIEW, googleUrl(query)).setPackage(pkg),
            ),
        )
    }

    private fun webSearch(query: String) =
        Intent(Intent.ACTION_WEB_SEARCH).putExtra(android.app.SearchManager.QUERY, query)

    private fun googleUrl(query: String): Uri =
        Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))

    private fun isInstalled(context: Context, pkg: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(pkg) != null
}

/** 端末で使っているメールアプリを探して、その最初の画面（受信トレイ）を開く */
object MailApp {
    fun openInbox(context: Context): Boolean {
        val pkg = findMailPackage(context) ?: return false
        val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        return Launch.start(context, launch)
    }

    private fun findMailPackage(context: Context): String? {
        val pm = context.packageManager
        val mailto = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        // 既定のメールアプリが決まっていればそれを使う
        @Suppress("DEPRECATION")
        val def = pm.resolveActivity(mailto, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
        if (def != null && def != "android" && pm.getLaunchIntentForPackage(def) != null) return def
        // 決まっていなければ、メールを扱えるアプリの最初の1つ
        @Suppress("DEPRECATION")
        return pm.queryIntentActivities(mailto, 0)
            .map { it.activityInfo.packageName }
            .firstOrNull { it != context.packageName && pm.getLaunchIntentForPackage(it) != null }
    }
}
