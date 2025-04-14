    package com.example.agendaescolar

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.material3.DatePicker
    import androidx.compose.material3.rememberDatePickerState
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.compose.material.icons.filled.ArrowDropDown
    import com.example.agendaescolar.ui.theme.AgendaEscolarTheme
    import kotlinx.coroutines.launch
    import java.time.LocalDate
    import java.time.format.DateTimeFormatter
    import androidx.compose.foundation.BorderStroke
    import androidx.compose.material.icons.filled.Menu
    import androidx.compose.ui.text.style.TextAlign

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                AgendaEscolarTheme {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") { LoginScreen(navController) }
                        composable("agenda/{user}") { backStackEntry ->
                            val user = backStackEntry.arguments?.getString("user") ?: "Usuário"
                            AgendaScreen(navController, user)
                        }
                    }
                }
            }
        }

        data class Atividade(
            var descricao: String,
            var data: String,
            var urgencia: String,
            var notas: String = ""
        )

        @Composable
        fun LoginScreen(navController: NavHostController) {
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var showError by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }

            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(12.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo do Estado de Santa Catarina",
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Agenda Escolar",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Usuário") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()

                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Senha") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            )

                            Button(
                                onClick = {
                                    if ((username == "aluno" && password == "1234") || (username == "willen" && password == "1234")) {
                                        navController.navigate("agenda/$username")
                                    } else {
                                        showError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF1976D2

                                    )
                                )
                            ) {
                                Text("Entrar")
                            }
                        }
                    }

                    Text(
                        text = "Desenvolvido por Lucas de Matos e Cleyton Winicius Vieira\n2025",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
            if (showError) {
                LaunchedEffect(showError) {
                    snackbarHostState.showSnackbar("Login ou senha incorreto")
                    showError = false
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun AgendaScreen(navController: NavHostController, userName: String) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val atividades = remember { mutableStateListOf<Atividade>() }
            var atividadeParaEditar by remember { mutableStateOf<Atividade?>(null) }
            var atividadeSelecionada by remember { mutableStateOf<Atividade?>(null) }

            var expandedMenu by remember { mutableStateOf(false) }
            var showAddDialog by remember { mutableStateOf(false) }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text(
                            "MENU",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                        NavigationDrawerItem(
                            label = { Text("Agenda") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() } }
                        )
                        NavigationDrawerItem(
                            label = { Text("Configurações (em breve)") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() } }
                        )
                        NavigationDrawerItem(
                            label = { Text("Ajuda (em breve)") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                if (showAddDialog || atividadeParaEditar != null) {
                    AddActivityDialog(
                        atividadeExistente = atividadeParaEditar,
                        onAdd = { descricao, data, urgencia, notas ->
                            if (atividadeParaEditar != null) {
                                atividadeParaEditar?.descricao = descricao
                                atividadeParaEditar?.data = data
                                atividadeParaEditar?.urgencia = urgencia
                                atividadeParaEditar?.notas = notas
                                atividadeParaEditar = null
                            } else {
                                atividades.add(Atividade(descricao, data, urgencia, notas))
                            }
                            showAddDialog = false
                        },
                        onDismiss = {
                            showAddDialog = false
                            atividadeParaEditar = null
                        }
                    )
                }

                atividadeSelecionada?.let { atividade ->
                    AlertDialog(
                        onDismissRequest = { atividadeSelecionada = null },
                        title = { Text(atividade.descricao) },
                        text = { Text("Notas: ${atividade.notas}") },
                        confirmButton = {
                            TextButton(onClick = { atividadeSelecionada = null }) {
                                Text("Fechar")
                            }
                        }
                    )
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Agenda Escolar") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
                                }
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { expandedMenu = true }
                                ) {
                                    Text(
                                        userName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Abrir menu do usuário",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Meu Perfil") },
                                        onClick = { expandedMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sair") },
                                        onClick = {
                                            expandedMenu = false
                                            navController.navigate("login") {
                                                popUpTo("agenda") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = Color(0xFF1976D2),
                                titleContentColor = Color.White
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = Color(0xFF1976D2)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Atividade")
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        LazyColumn {
                            items(atividades) { atividade ->
                            Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { atividadeSelecionada = atividade },
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (atividade.urgencia) {
                                            "Alta" -> Color(0xFFFFCDD2)
                                            "Média" -> Color(0xFFFFF9C4)
                                            else -> Color(0xFFC8E6C9)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = atividade.descricao,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Data: ${atividade.data} | Urgência: ${atividade.urgencia}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                atividadeParaEditar = atividade
                                                showAddDialog = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar"
                                                )
                                            }
                                            IconButton(onClick = { atividades.remove(atividade) }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Excluir"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun AddActivityDialog(
            atividadeExistente: Atividade? = null,
            onAdd: (String, String, String, String) -> Unit,
            onDismiss: () -> Unit
        ) {
            var descricao by remember { mutableStateOf(atividadeExistente?.descricao ?: "") }
            var datePickerState =
                rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
            var urgencia by remember { mutableStateOf(atividadeExistente?.urgencia ?: "Baixa") }
            var notas by remember { mutableStateOf(atividadeExistente?.notas ?: "") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (atividadeExistente != null) "Editar atividade" else "Adicionar nova atividade") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            label = { Text("Descrição da atividade") }
                        )

                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas adicionais") },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text("Urgência:", modifier = Modifier.padding(top = 16.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { urgencia = "Alta" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (urgencia == "Alta") Color.Red else Color.LightGray
                                ),
                                border = if (urgencia == "Alta") BorderStroke(
                                    2.dp,
                                    Color.Black
                                ) else null
                            ) {
                                Text(
                                    "Alta",
                                    fontWeight = if (urgencia == "Alta") FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Button(
                                onClick = { urgencia = "Média" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (urgencia == "Média") Color.Yellow else Color.LightGray
                                ),
                                border = if (urgencia == "Média") BorderStroke(
                                    2.dp,
                                    Color.Black
                                ) else null
                            ) {
                                Text(
                                    "Média",
                                    fontWeight = if (urgencia == "Média") FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Button(
                                onClick = { urgencia = "Baixa" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (urgencia == "Baixa") Color.Green else Color.LightGray
                                ),
                                border = if (urgencia == "Baixa") BorderStroke(
                                    2.dp,
                                    Color.Black
                                ) else null
                            ) {
                                Text(
                                    "Baixa",
                                    fontWeight = if (urgencia == "Baixa") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val millis =
                            datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onAdd(
                            descricao,
                            selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            urgencia,
                            notas
                        )
                    }) {
                        Text(if (atividadeExistente != null) "Salvar" else "Adicionar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
