package com.nothing.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F4F4)
                ) {
                    NothingLauncherApp(
                        onLoadApps = { loadInstalledApps() },
                        onLaunchApp = { app -> launchApp(app) }
                    )
                }
            }
        }
    }

    private fun loadInstalledApps(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager
            .queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                    ?: return@mapNotNull null

                LauncherApp(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    icon = resolveInfo.loadIcon(packageManager)
                )
            }
            .distinctBy { "${it.packageName}/${it.activityName}" }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun launchApp(app: LauncherApp) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            // The activity may no longer be available.
        }
    }
}

@Composable
private fun NothingLauncherApp(
    onLoadApps: () -> List<LauncherApp>,
    onLaunchApp: (LauncherApp) -> Unit
) {
    var apps by remember {
        mutableStateOf<List<LauncherApp>>(emptyList())
    }

    var showDrawer by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        apps = onLoadApps()
    }

    BackHandler(enabled = showDrawer) {
        showDrawer = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
    ) {
        if (showDrawer) {
            AppDrawer(
                apps = apps,
                onAppClick = onLaunchApp,
                onClose = { showDrawer = false }
            )
        } else {
            HomeScreen(
                onOpenDrawer = { showDrawer = true }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenDrawer: () -> Unit
) {
    val date = remember {
        SimpleDateFormat(
            "EEEE, d MMMM",
            Locale.getDefault()
        ).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 76.dp)
        ) {
            Text(
                text = date,
                fontSize = 17.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "NOTHING",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color(0xFF171717)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Simple. Clear. Yours.",
                fontSize = 15.sp,
                color = Color(0xFF777777)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .background(
                        color = Color(0xFFE4E4E4),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable {
                        onOpenDrawer()
                    }
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = Color(0xFFFAFAFA),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (index) {
                                0 -> "●"
                                1 -> "◉"
                                2 -> "▦"
                                else -> "＋"
                            },
                            fontSize = 23.sp,
                            color = Color(0xFF252525)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "اسحب للأعلى لعرض التطبيقات",
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
private fun AppDrawer(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    onClose: () -> Unit
) {
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
                text = "Applications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717)
            )

            Text(
                text = "✕",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${apps.size} applications",
            fontSize = 14.sp,
            color = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "جارٍ تحميل التطبيقات...",
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
                    items = apps,
                    key = { "${it.packageName}/${it.activityName}" }
                ) { app ->
                    AppIcon(
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIcon(
    app: LauncherApp,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageDrawable(app.icon)
                    contentDescription = app.label
                }
            },
            update = { imageView ->
                imageView.setImageDrawable(app.icon)
                imageView.contentDescription = app.label
            },
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = app.label,
            fontSize = 11.sp,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )
    }
}
