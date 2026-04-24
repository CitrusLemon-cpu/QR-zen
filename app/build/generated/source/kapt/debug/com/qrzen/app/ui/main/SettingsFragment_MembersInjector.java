package com.qrzen.app.ui.main;

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
public final class SettingsFragment_MembersInjector implements MembersInjector<SettingsFragment> {
  private final Provider<AppBlockDao> daoProvider;

  public SettingsFragment_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<SettingsFragment> create(Provider<AppBlockDao> daoProvider) {
    return new SettingsFragment_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(SettingsFragment instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.main.SettingsFragment.dao")
  public static void injectDao(SettingsFragment instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
