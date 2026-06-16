package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Long,
    val iconName: String,
    val createdAt: Long,
    val isArchived: Boolean = false
)
