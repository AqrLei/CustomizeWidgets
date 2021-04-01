package com.aqrlei.roomlib

import androidx.lifecycle.*
import com.aqrlei.roomlib.data.repository.UserRepository
import com.aqrlei.roomlib.data.source.local.entity.User
import kotlinx.coroutines.launch

/**
 * created by AqrLei on 4/1/21
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    val allUsers: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun categoryUsers(category: String) = repository.loadAllByCategory(category).asLiveData()

    fun insert(vararg users: User) = viewModelScope.launch { repository.insertAll(*users) }

    fun delete(vararg users: User) = viewModelScope.launch { repository.delete(*users) }

    fun update(vararg users: User) = viewModelScope.launch { repository.update(*users) }
}

class UserViewModelFactory(private val repository: UserRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) return UserViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

