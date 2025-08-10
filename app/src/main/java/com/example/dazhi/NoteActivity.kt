import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteActivity(navController: NavController) {
    val context = LocalContext.current
    // 状态：控制选择对话框显示
    var showSourceDialog by remember { mutableStateOf(false) }
    // 状态：接收相机/相册返回的图片
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }



    // 1. 相机启动器（返回拍摄的 Bitmap）
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        selectedImage = bitmap // 处理相机返回的图片
    }

    // 新增：控制权限拒绝对话框显示的状态（Composable 状态）
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // 新增：相机权限请求器
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，启动相机
            cameraLauncher.launch(null)
        } else {
            // 权限被拒绝，可显示提示
            showPermissionDeniedDialog = true
        }
    }

    // 2. 相册启动器（返回选择的图片 Uri）
    val albumLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri // 处理相册返回的图片 Uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打卡") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            Text("这是新页面内容", modifier = Modifier.padding(16.dp))

            // 触发选择的按钮
            Button(
                onClick = { showSourceDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("选择图片（相机/相册）")
            }

            // 3. 选择来源对话框
            if (showSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showSourceDialog = false },
                    title = { Text("选择图片来源") },
                    text = { Text("请选择拍摄照片或从相册选择") },
                    confirmButton = {
                        Button(onClick = {
                            // 启动相机

                            // 修改：复用提前获取的 context，无需在此处调用 LocalContext.current
                            if (ContextCompat.checkSelfPermission(
                                    context,  // 使用提前获取的上下文
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            showSourceDialog = false
                        }) {
                            Text("相机")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            // 启动相册（选择图片类型）
                            albumLauncher.launch("image/*")
                            showSourceDialog = false
                        }) {
                            Text("相册")
                        }
                    }
                )
            }

            // 4. 预览选择的图片（可选）
            selectedImage?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "拍摄的图片",
                    modifier = Modifier.size(200.dp)
                )
            }
            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberImagePainter(uri),
                    contentDescription = "选择的图片",
                    modifier = Modifier.size(200.dp)
                )
            }

            // 新增：在 Composable 作用域内显示权限拒绝对话框
            if (showPermissionDeniedDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDeniedDialog = false },
                    title = { Text("权限被拒绝") },
                    text = { Text("需要相机权限才能拍照，请在设置中开启。") },
                    confirmButton = {
                        Button(onClick = { showPermissionDeniedDialog = false }) {
                            Text("确定")
                        }
                    }
                )
            }

        }
    }
}