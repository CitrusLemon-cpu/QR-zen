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
public final class EditBlockActivity_MembersInjector implements MembersInjector<EditBlockActivity> {
  private final Provider<AppBlockDao> daoProvider;

  public EditBlockActivity_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<EditBlockActivity> create(Provider<AppBlockDao> daoProvider) {
    return new EditBlockActivity_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(EditBlockActivity instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.block.EditBlockActivity.dao")
  public static void injectDao(EditBlockActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
