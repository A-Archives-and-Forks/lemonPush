package net.lemontree.push

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import net.lemontree.push.model.PCClient
import net.lemontree.push.model.PCClientViewModel
import net.lemontree.push.model.PCClientViewModelFactory
import net.lemontree.push.ui.theme.MsglistenerTheme

private lateinit var sharedPref: SharedPreferences
val DEFAULT_PORT = "14756"


class PCConfigActivity : ComponentActivity() {
    private lateinit var pcClientViewModel: PCClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("PCList", Context.MODE_PRIVATE)
        pcClientViewModel = ViewModelProvider(
            this,
            PCClientViewModelFactory(sharedPref)
        ).get(PCClientViewModel::class.java)

        setContent {
            MsglistenerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MyScreen(pcClientViewModel, this)
                }
            }
        }
    }
}

@Composable
fun MyScreen(pcClientViewModel: PCClientViewModel, activity: PCConfigActivity) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "电脑管理",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Medium
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 4.dp,
                navigationIcon = {
                    IconButton(
                        onClick = { activity.finish() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加电脑",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Greeting(pcClientViewModel)
        }
    }
    if (showDialog) {
        TextInputDialog(
            showDialog = showDialog,
            onConfirm = { it, _ ->
                showDialog = false
                if (it.port == "") {
                    it.port = DEFAULT_PORT
                }
                pcClientViewModel.addPCItem(it)
            },
            onDismiss = { showDialog = false },
            pcClient = null,
            idx = null
        )
    }

}

@Composable
fun TextInputDialog(
    showDialog: Boolean,
    onConfirm: (PCClient, Int?) -> Unit,
    onDismiss: () -> Unit,
    pcClient: PCClient? = null, // 添加可选的 PCClient 参数
    idx: Int?
) {
    if (showDialog) {
        var ipValue by remember { mutableStateOf(pcClient?.ip ?: "") }
        var portValue by remember { mutableStateOf(pcClient?.port ?: "") }
        var nameValue by remember { mutableStateOf(pcClient?.name ?: "") }
        
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { 
                Text(
                    text = if (pcClient == null) "添加电脑" else "编辑电脑",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 电脑名称输入
                    OutlinedTextField(
                        value = nameValue,
                        onValueChange = { nameValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("电脑名称") },
                        placeholder = { Text("例如：我的电脑") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small
                    )
                    
                    // IP地址输入
                    OutlinedTextField(
                        value = ipValue,
                        onValueChange = { ipValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("IP地址") },
                        placeholder = { Text("例如：192.168.1.100") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small
                    )
                    
                    // 端口输入
                    OutlinedTextField(
                        value = portValue,
                        onValueChange = { portValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("端口") },
                        placeholder = { Text("默认：14756") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(PCClient(nameValue, ipValue, portValue), idx)
                    },
                    enabled = ipValue.isNotBlank() && portValue.isNotBlank()
                ) {
                    Text(if (pcClient == null) "添加" else "保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("取消")
                }
            },
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface
        )
    }
}

@Composable
fun Greeting(pcClientViewModel: PCClientViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var pcClient by remember { mutableStateOf(PCClient("", "", "")) }
    var pcIndex by remember { mutableStateOf(0) }
    val pcList = pcClientViewModel.pcItems
    if (pcList.size > 0) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(pcList) { idx, pc ->
                PCCardItem(
                    pc = pc,
                    onEditClick = {
                        showDialog = true
                        pcClient = pc
                        pcIndex = idx
                    },
                    onDeleteClick = {
                        pcClientViewModel.delItem(idx)
                    }
                )
            }
        }
        if (showDialog) {
            TextInputDialog(
                showDialog = showDialog,
                onConfirm = { pcClient, i ->
                    if (i != null) {
                        pcClientViewModel.updatePCItem(i, pcClient)
                    } else {
                        pcClientViewModel.addPCItem(pcClient)
                    }
                    showDialog = false
                },
                onDismiss = { showDialog = false },
                pcClient,
                idx = pcIndex
            )
        }
    } else {
        // 空状态显示
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = "暂无电脑配置",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "点击右上角 + 按钮添加电脑",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun PCCardItem(
    pc: PCClient,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 电脑图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }
                
                // 电脑信息
                Column {
                    Text(
                        text = "电脑 " + (pc.name.takeIf { it.isNotEmpty() } ?: "未命名"),
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = pc.ip + ":" + pc.port,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}