package com.permissionmonitor.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.permissionmonitor.data.model.AppInfo
import com.permissionmonitor.data.model.PermissionDetail
import com.permissionmonitor.data.model.PermissionRisk
import com.permissionmonitor.data.repository.AppRepository
import com.permissionmonitor.data.source.PermissionClassifier
import com.permissionmonitor.ui.components.drawableToBitmap
import com.permissionmonitor.ui.theme.Green500
import com.permissionmonitor.ui.theme.Orange500
import com.permissionmonitor.ui.theme.Red500
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    
    var appInfo by remember { mutableStateOf<AppInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(0) } // 0=全部, 1=敏感, 2=已授权
    
    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            appInfo = repository.getAppInfo(packageName)
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        appInfo?.appName ?: "应用详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            appInfo == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("无法加载应用信息")
                }
            }
            else -> {
                val currentAppInfo = appInfo!!
                
                val filteredPermissions = when (selectedFilter) {
                    1 -> currentAppInfo.permissions.filter { 
                        it.riskLevel == PermissionRisk.DANGEROUS || it.riskLevel == PermissionRisk.HIGH 
                    }
                    2 -> currentAppInfo.permissions.filter { it.isGranted }
                    else -> currentAppInfo.permissions
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 应用信息卡片
                    item {
                        AppInfoCard(
                            appInfo = currentAppInfo,
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:$packageName")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                    
                    // 筛选器
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == 0,
                                onClick = { selectedFilter = 0 },
                                label = { Text("全部 (${currentAppInfo.permissions.size})") }
                            )
                            FilterChip(
                                selected = selectedFilter == 1,
                                onClick = { selectedFilter = 1 },
                                label = { 
                                    Text("敏感 (${currentAppInfo.permissions.count { 
                                        it.riskLevel == PermissionRisk.DANGEROUS || it.riskLevel == PermissionRisk.HIGH 
                                    }})") 
                                }
                            )
                            FilterChip(
                                selected = selectedFilter == 2,
                                onClick = { selectedFilter = 2 },
                                label = { Text("已授权 (${currentAppInfo.grantedPermissions})") }
                            )
                        }
                    }
                    
                    // 权限列表
                    items(filteredPermissions) { permission ->
                        PermissionItem(permission = permission)
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppInfoCard(
    appInfo: AppInfo,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 应用图标和名称
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    appInfo.icon?.let { drawable ->
                        Image(
                            bitmap = drawableToBitmap(drawable).asImageBitmap(),
                            contentDescription = appInfo.appName,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "版本 ${appInfo.versionName ?: "未知"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = appInfo.totalPermissions.toString(),
                    label = "总权限",
                    color = MaterialTheme.colorScheme.primary
                )
                StatBox(
                    value = appInfo.grantedPermissions.toString(),
                    label = "已授权",
                    color = Green500
                )
                StatBox(
                    value = appInfo.dangerousPermissions.toString(),
                    label = "敏感权限",
                    color = if (appInfo.dangerousPermissions > 0) Red500 else Green500
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 管理按钮
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("管理权限")
            }
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionItem(permission: PermissionDetail) {
    val riskColor = when (permission.riskLevel) {
        PermissionRisk.DANGEROUS -> Red500
        PermissionRisk.HIGH -> Orange500
        PermissionRisk.MEDIUM -> Color(0xFFFFC107)
        PermissionRisk.LOW -> Green500
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (permission.isGranted) Green500.copy(alpha = 0.1f) 
                        else Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (permission.isGranted) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RemoveCircle,
                    contentDescription = null,
                    tint = if (permission.isGranted) Green500 else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 权限信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = PermissionClassifier.getSimplePermissionName(permission.name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                permission.description?.let { desc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                permission.group?.let { group ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 风险标签
            Box(
                modifier = Modifier
                    .background(
                        color = riskColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = permission.riskLevel.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = riskColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
