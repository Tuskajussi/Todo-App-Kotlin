package com.example.firebasetesting

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.firebasetesting.ui.theme.FirebasetestingTheme
import com.google.firebase.Firebase

import com.google.firebase.firestore.firestore


data class CustomTask(
    val taskId: String,
    val taskText: String
)

class MainActivity : ComponentActivity() {
    private val db = Firebase.firestore
    private val userId = "userID-testing" // Replace with the actual user's ID
    private val tasks = mutableStateOf(emptyList<CustomTask>()) // State for tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebasetestingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskList(tasks = tasks.value) { taskId ->
                        deleteTask(userId, taskId)
                    }
                    AddTask()
                }
            }
        }
    }
    init {
        val taskCollectionRef = db.collection("users").document(userId).collection("tasks")

        taskCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val taskList = snapshot?.documents?.map { document ->
                val taskId = document.id
                val taskText = document.getString("todo") ?: ""
                CustomTask(taskId, taskText)
            } ?: emptyList()

            tasks.value = taskList
        }
    }
}

@Composable
fun TaskList(tasks: List<CustomTask>, onDeleteTask: (String) -> Unit) {
    LazyColumn {
        items(tasks) { task ->
            TaskListItem(task = task, onDeleteTask = onDeleteTask)
        }
    }
}

@Composable
fun TaskListItem(task: CustomTask, onDeleteTask: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = task.taskText, modifier = Modifier.padding(16.dp))

        IconButton(
            onClick = { onDeleteTask(task.taskId) },
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = Color.Red
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTask(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(TextFieldValue()) } 
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = { newText -> text = newText },
            label = { Text("Enter a task") }
        )

        Button(onClick = {
            val taskDescription = text.text
            if (taskDescription.isNotEmpty()) {
                writeToDatabase(taskDescription)
                text = TextFieldValue("")
            }
        }) {
            Text("Add Task")
        }
    }
}

fun writeToDatabase(value: String) {
    val db = Firebase.firestore
    val taskData = hashMapOf("todo" to value)

    val newDocumentRef = db.collection("users").document("userID-testing").collection("tasks")
    newDocumentRef
        .add(taskData)
        .addOnSuccessListener { Log.d(TAG, "Document successfully written with ID: ${newDocumentRef.id}") }
        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
}

fun deleteTask(userId: String, taskId: String) {
    val db = Firebase.firestore
    val userCollection = db.collection("users").document(userId).collection("tasks")

    userCollection.document(taskId)
        .delete()
        .addOnSuccessListener {
            Log.d(TAG, "Document successfully deleted with ID: $taskId")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error deleting document", e)
        }
}

@Preview(showBackground = true)
@Composable
fun AddTaskPreview() {
    FirebasetestingTheme {
        AddTask()
    }
}