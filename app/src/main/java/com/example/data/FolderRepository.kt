package com.example.data

import kotlinx.coroutines.flow.Flow

class FolderRepository(private val folderDao: FolderDao) {
    val allActiveFolders: Flow<List<Folder>> = folderDao.getAllActiveFolders()
    val allFolders: Flow<List<Folder>> = folderDao.getAllFolders()

    suspend fun getFolderById(id: Int): Folder? {
        return folderDao.getFolderById(id)
    }

    fun getFolderByIdFlow(id: Int): Flow<Folder?> {
        return folderDao.getFolderByIdFlow(id)
    }

    suspend fun insertFolder(folder: Folder): Long {
        return folderDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder)
    }

    suspend fun deleteFolderById(id: Int) {
        folderDao.deleteFolderById(id)
    }
}
