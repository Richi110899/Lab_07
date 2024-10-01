package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // Anotación para APIs experimentales
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = crearDatabase(context)
    val dao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser by remember { mutableStateOf("") } // Cambiado a var para poder actualizarlo

    // Función para cargar usuarios
    fun loadUsers() {
        coroutineScope.launch {
            dataUser = getUsers(dao)
        }
    }

    // Cargar usuarios al inicio
    LaunchedEffect(Unit) {
        loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                actions = {
                    IconButton(onClick = {
                        val user = User(0, firstName, lastName)
                        coroutineScope.launch {
                            AgregarUsuario(user = user, dao = dao)
                            firstName = ""
                            lastName = ""
                            loadUsers() // Recargar la lista de usuarios después de agregar
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val lastUser = dao.getLastUser()
                            lastUser?.let {
                                dao.deleteUser(it.uid)
                                loadUsers() // Recargar la lista después de eliminar
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Último Usuario")
                    }
                    IconButton(onClick = {
                        loadUsers() // Listar usuarios
                    }) {
                        Icon(Icons.Default.List, contentDescription = "Listar Usuarios")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            Spacer(Modifier.height(50.dp))
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name: ") },
                singleLine = true
            )
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name:") },
                singleLine = true
            )
            Text(
                text = dataUser, // Mostrar la lista de usuarios
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    val users = dao.getAll()
    return users.joinToString("\n") { "${it.firstName} - ${it.lastName}" }
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error: insert: ${e.message}")
    }
}
