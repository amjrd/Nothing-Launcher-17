package com.nothing.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NothingBlack = Color(0xFF101010)
private val NothingWhite = Color(0xFFF5F5F5)
private val NothingRed = Color(0xFFD71920)
private val NothingGray = Color(0xFF929292)

data class LauncherApp(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            MaterialTheme {
                NothingLauncherApp()
            }
        }
    }
}

@Composable
private fun NothingLauncherApp() {
    val context = LocalContext.current

    var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("--:--") }
    var currentDate by remember { mutableStateOf("") }

    fun loadApps() {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        apps = pm.queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName
                    ?: return@mapNotNull null

                if (packageName == context.packageName) {
                    return@mapNotNull null
                }

                LauncherApp(
                    label = info.loadLabel(pm).toString(),
                    packageName = packageName,
                    icon = info.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    LaunchedEffect(Unit) {
        loadApps()

        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(now)

            currentDate = SimpleDateFormat(
                "EEEE, d MMMM",
                Locale.getDefault()
            ).format(now)

            delay(1000L)
        }
    }

    BackHandler(enabled = showDrawer) {
        showDrawer = false
        searchQuery = ""
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NothingBlack
    ) {
        if (showDrawer) {
            AppDrawer(
                apps = apps,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onAppClick = { app ->
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(app.packageName)

                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                    }

                    showDrawer = false
                    searchQuery = ""
                },
                onBack = {
                    showDrawer = false
                    searchQuery = ""
                }
            )
        } else {
            HomeScreen(
                time = currentTime,
                date = currentDate,
                onOpenDrawer = { showDrawer = true }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    time: String,
    date: String,
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "NOTHING",
                color = NothingWhite,
                fontSize = 13.sp,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = time,
                color = NothingWhite,
                fontSize = 72.sp,
                lineHeight = 76.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = date.uppercase(Locale.getDefault()),
                color = NothingGray,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR APPS",
                    color = NothingGray,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "ALL  ↗",
                    color = NothingWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenDrawer)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF202020))
                    .clickable(onClick = onOpenDrawer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OPEN APP DRAWER   +",
                    color = NothingWhite,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NOTHING OS  •  ANDROID 17",
                color = NothingGray,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onBack: () -> Unit
) {
    val filteredApps = remember(apps, query) {
        apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(horizontal = 18.dp)
            .padding(top = 32.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "APPLICATIONS",
                color = NothingWhite,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "CLOSE  ✕",
                color = NothingGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF202020))
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⌕",
                color = NothingGray,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.size(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = NothingWhite,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search applications",
                                color = NothingGray,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${filteredApps.size} APPLICATIONS",
            color = NothingGray,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(
                items = filteredApps,
                key = { it.packageName }
            ) { app ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAppClick(app) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.painter
                            .rememberDrawablePainter(app.icon),
                        contentDescription = app.label,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = app.label,
                        color = NothingWhite,
                        fontSize = 10.sp,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
