package com.qrzen.app.ui.block;

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
public final class AppPickerActivity_MembersInjector implements MembersInjector<AppPickerActivity> {
  private final Provider<AppBlockDao> daoProvider;

  public AppPickerActivity_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<AppPickerActivity> create(Provider<AppBlockDao> daoProvider) {
    return new AppPickerActivity_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(AppPickerActivity instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.block.AppPickerActivity.dao")
  public static void injectDao(AppPickerActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
