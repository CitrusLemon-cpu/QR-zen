package com.qrzen.app.service;

import com.qrzen.app.data.db.AppBlockDao;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class BackgroundService_MembersInjector implements MembersInjector<BackgroundService> {
  private final Provider<AppBlockDao> daoProvider;

  public BackgroundService_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<BackgroundService> create(Provider<AppBlockDao> daoProvider) {
    return new BackgroundService_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(BackgroundService instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.service.BackgroundService.dao")
  public static void injectDao(BackgroundService instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
