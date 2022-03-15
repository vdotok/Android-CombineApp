package com.vdotok.app.di

import android.app.Application
import com.vdotok.app.manager.AppManager
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.repository.AccountRepository
import com.vdotok.network.repository.GroupRepository
import com.vdotok.network.repository.ProfileRepository
import com.vdotok.network.repository.UserListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:00 PM in 2021
 */

@InstallIn(SingletonComponent::class)
@Module
object ApplicationModule {
    @Singleton
    @Provides
    fun getAppManager(application: Application): AppManager {
        return AppManager(
            application
        )
    }

    @Singleton
    @Provides
    fun provideAccountRepository() : AccountRepository {
        val service = RetrofitModule.provideRetrofitService()
        return AccountRepository(service)
    }

    @Singleton
    @Provides
    fun provideUserListRepository() : UserListRepository {
        val service = RetrofitModule.provideRetrofitService()
        return UserListRepository(service)
    }

    @Singleton
    @Provides
    fun provideGroupRepository() : GroupRepository {
        val service = RetrofitModule.provideRetrofitService()
        return GroupRepository(service)
    }

    @Singleton
    @Provides
    fun provideProfileRepository() : ProfileRepository {
        val service = RetrofitModule.provideRetrofitService()
        return ProfileRepository(service)
    }

}