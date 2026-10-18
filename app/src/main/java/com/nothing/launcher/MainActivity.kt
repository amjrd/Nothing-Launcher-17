package com.nothing.launcher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LauncherApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: android.graphics.drawable.Drawable
)

class MainActivity : ComponentActivity() {

    private lateinit var widgetHost: AppWidgetHost
    private lateinit var widgetManager: AppWidgetManager

    private var pendingWidgetId =
        AppWidgetManager.INVALID_APPWIDGET_ID

    private var selectedWidgetId by mutableIntStateOf(
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private val widgetConfigure =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val configuredId = result.data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                pendingWidgetId
            ) ?: pendingWidgetId

            if (
                result.resultCode == RESULT_OK &&
                configuredId != AppWidgetManager.INVALID_APPWIDGET_ID &&
                widgetManager.getAppWidgetInfo(configuredId) != null
            ) {
                saveSelectedWidget(configuredId)
            } else {
                if (
                    configuredId !=
                    AppWidgetManager.INVALID_APPWIDGET_ID
                ) {
                    widgetHost.deleteAppWidgetId(configuredId)
                }

                pendingWidgetId =
                    AppWidgetManager.INVALID_APPWIDGET_ID
            }
        }

    private val widgetPicker =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val returnedId = result.data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                pendingWidgetId
            ) ?: pendingWidgetId

            if (
                result.resultCode == RESULT_OK &&
                returnedId != AppWidgetManager.INVALID_APPWIDGET_ID
            ) {
                val providerInfo =
                    widgetManager.getAppWidgetInfo(returnedId)

                if (providerInfo == null) {
                    widgetHost.deleteAppWidgetId(returnedId)

                    pendingWidgetId =
                        AppWidgetManager.INVALID_APPWIDGET_ID

                } else if (providerInfo.configure != null) {

                    val configureIntent = Intent(
                        AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                    ).apply {
                        component = providerInfo.configure

                        putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            returnedId
                        )
                    }

                    try {
                        widgetConfigure.launch(configureIntent)
                    } catch (_: Exception) {
                        widgetHost.deleteAppWidgetId(returnedId)

                        pendingWidgetId =
                            AppWidgetManager.INVALID_APPWIDGET_ID
                    }

                } else {
                    saveSelectedWidget(returnedId)
                }

            } else {
                if (
                    pendingWidgetId !=
                    AppWidgetManager.INVALID_APPWIDGET_ID
                ) {
                    widgetHost.deleteAppWidgetId(pendingWidgetId)
                }

                pendingWidgetId =
                    AppWidgetManager.INVALID_APPWIDGET_ID
            }
        }

    private fun saveSelectedWidget(appWidgetId: Int) {

        selectedWidgetId = appWidgetId

        getSharedPreferences(
            "nothing_launcher",
            MODE_PRIVATE
        ).edit()
            .putInt("selected_widget_id", appWidgetId)
            .apply()

        pendingWidgetId =
            AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetManager = AppWidgetManager.getInstance(this)
        widgetHost = AppWidgetHost(this, 1024)

        selectedWidgetId = getSharedPreferences(
            "nothing_launcher",
            MODE_PRIVATE
        ).getInt(
            "selected_widget_id",
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        window.statusBarColor =
            android.graphics.Color.TRANSPARENT

        window.navigationBarColor =
            android.graphics.Color.TRANSPARENT

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F4F4)
                ) {
                    NothingLauncherApp(
                        onLoadApps = { loadInstalledApps() },
                        onLaunchApp = { launchApp(it) },
                        widgetHost = widgetHost,
                        widgetManager = widgetManager,
                        selectedWidgetId = selectedWidgetId,
                        onAddWidget = { openWidgetPicker() },
                        onRemoveWidget = { removeSelectedWidget() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        try {
            widgetHost.startListening()
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        try {
            widgetHost.stopListening()
        } catch (_: Exception) {
        }

        super.onStop()
    }

    private fun openWidgetPicker() {

        try {
            widgetHost.startListening()
        } catch (_: Exception) {
        }

        pendingWidgetId = widgetHost.allocateAppWidgetId()

        val intent = Intent(
            AppWidgetManager.ACTION_APPWIDGET_PICK
        ).apply {
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                pendingWidgetId
            )
        }

        try {
            widgetPicker.launch(intent)
        } catch (_: Exception) {

            widgetHost.deleteAppWidgetId(pendingWidgetId)

            pendingWidgetId =
                AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }

    private fun removeSelectedWidget() {

        if (
            selectedWidgetId !=
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {
            widgetHost.deleteAppWidgetId(selectedWidgetId)

            selectedWidgetId =
                AppWidgetManager.INVALID_APPWIDGET_ID

            getSharedPreferences(
                "nothing_launcher",
                MODE_PRIVATE
            ).edit()
                .remove("selected_widget_id")
                .apply()
        }
    }

    private fun loadInstalledApps(): List<LauncherApp> {

        val intent = Intent(
            Intent.ACTION_MAIN
        ).addCategory(
            Intent.CATEGORY_LAUNCHER
        )

        return packageManager
            .queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )
            .mapNotNull { resolveInfo ->

                val info = resolveInfo.activityInfo
                    ?: return@mapNotNull null

                LauncherApp(
                    label = resolveInfo
                        .loadLabel(packageManager)
                        .toString(),
                    packageName = info.packageName,
                    activityName = info.name,
                    icon = resolveInfo.loadIcon(packageManager)
                )
            }
            .distinctBy {
                "${it.packageName}/${it.activityName}"
            }
            .sortedBy {
                it.label.lowercase(Locale.getDefault())
            }
    }

    private fun launchApp(app: LauncherApp) {

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)

            setClassName(
                app.packageName,
                app.activityName
            )

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
        }
    }
}

@Composable
private fun NothingLauncherApp(
    onLoadApps: () -> List<LauncherApp>,
    onLaunchApp: (LauncherApp) -> Unit,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    selectedWidgetId: Int,
    onAddWidget: () -> Unit,
    onRemoveWidget: () -> Unit
) {

    var apps by remember {
        mutableStateOf<List<LauncherApp>>(emptyList())
    }

    var showDrawer by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        apps = onLoadApps()
    }

    BackHandler(enabled = showDrawer) {
        showDrawer = false
    }

    if (showDrawer) {

        AppDrawer(
            apps = apps,
            onAppClick = onLaunchApp,
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClose = { showDrawer = false }
        )

    } else {

        HomeScreen(
            selectedWidgetId = selectedWidgetId,
            widgetHost = widgetHost,
            widgetManager = widgetManager,
            onOpenDrawer = { showDrawer = true },
            onAddWidget = onAddWidget,
            onRemoveWidget = onRemoveWidget
        )
    }
}

@Composable
private fun HomeScreen(
    selectedWidgetId: Int,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    onOpenDrawer: () -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: () -> Unit
) {

    val date = remember {
        SimpleDateFormat(
            "EEEE, d MMMM",
            Locale.getDefault()
        ).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .pointerInput(Unit) {

                var dragDistance = 0f

                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        dragDistance += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance < -80f) {
                            onOpenDrawer()
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
            .padding(horizontal = 24.dp)
    ) {

        Column(
            modifier = Modifier.padding(top = 54.dp)
        ) {

            Text(
                date,
                fontSize = 17.sp,
                color = Color(0xFF555555)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "NOTHING",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color(0xFF171717)
            )

            Text(
                "Simple. Clear. Yours.",
                fontSize = 15.sp,
                color = Color(0xFF777777)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            ActionChip(
                "＋ إضافة Widget",
                Modifier.weight(1f),
                onAddWidget
            )

            if (
                selectedWidgetId !=
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) {
                ActionChip(
                    "حذف Widget",
                    Modifier.weight(1f),
                    onRemoveWidget
                )
            }
        }

        if (
            selectedWidgetId !=
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {

            val info = remember(selectedWidgetId) {
                widgetManager.getAppWidgetInfo(selectedWidgetId)
            }

            if (info != null) {

                AndroidView(
                    factory = { context ->
                        widgetHost.createView(
                            context,
                            selectedWidgetId,
                            info
                        ).apply {
                            setAppWidget(selectedWidgetId, info)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

            } else {

                Text(
                    "تعذّر تحميل الـWidget. احذفها وأضفها من جديد.",
                    color = Color(0xFF9B1C1C),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "اضغط «إضافة Widget» لإضافة ساعة أو أي Widget من هاتفك.",
                    color = Color(0xFF777777),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(
            Modifier.weight(
                if (
                    selectedWidgetId ==
                    AppWidgetManager.INVALID_APPWIDGET_ID
                ) 0.2f else 1f
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(
                        Color(0xFFE4E4E4),
                        RoundedCornerShape(28.dp)
                    )
                    .clickable {
                        onOpenDrawer()
                    }
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                listOf("●", "◉", "▦", "＋").forEach { symbol ->

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color(0xFFFAFAFA),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            symbol,
                            fontSize = 23.sp,
                            color = Color(0xFF252525)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "اسحب للأعلى أو اضغط هنا لعرض التطبيقات",
                fontSize = 13.sp,
                color = Color(0xFF777777),
                modifier = Modifier.clickable {
                    onOpenDrawer()
                }
            )
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .background(
                Color(0xFFE4E4E4),
                RoundedCornerShape(18.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {

    val filteredApps = apps.filter {
        it.label.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 40.dp,
                bottom = 20.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                "Applications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717)
            )

            Text(
                "✕",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "${filteredApps.size} applications",
            fontSize = 14.sp,
            color = Color(0xFF777777)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text("Search apps / بحث التطبيقات")
            },
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (filteredApps.isEmpty()) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (apps.isEmpty()) {
                        "جارٍ تحميل التطبيقات..."
                    } else {
                        "لا توجد تطبيقات مطابقة"
                    },
                    color = Color(0xFF555555)
                )
            }

        } else {

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                items(
                    filteredApps,
                    key = {
                        "${it.packageName}/${it.activityName}"
                    }
                ) { app ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAppClick(app)
                            }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    scaleType =
                                        ImageView.ScaleType.FIT_CENTER

                                    setImageDrawable(app.icon)
                                    contentDescription = app.label
                                }
                            },
                            update = {
                                it.setImageDrawable(app.icon)
                                it.contentDescription = app.label
                            },
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            app.label,
                            fontSize = 11.sp,
                            color = Color(0xFF222222),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
