package com.qrzen.app.ui.block;

import com.qrzen.app.data.db.AppBlockDao;
import com.qrzen.app.data.db.TimeBlockDao;
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

  private final Provider<TimeBlockDao> timeBlockDaoProvider;

  public EditBlockActivity_MembersInjector(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider) {
    this.daoProvider = daoProvider;
    this.timeBlockDaoProvider = timeBlockDaoProvider;
  }

  public static MembersInjector<EditBlockActivity> create(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider) {
    return new EditBlockActivity_MembersInjector(daoProvider, timeBlockDaoProvider);
  }

  @Override
  public void injectMembers(EditBlockActivity instance) {
    injectDao(instance, daoProvider.get());
    injectTimeBlockDao(instance, timeBlockDaoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.block.EditBlockActivity.dao")
  public static void injectDao(EditBlockActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }

  @InjectedFieldSignature("com.qrzen.app.ui.block.EditBlockActivity.timeBlockDao")
  public static void injectTimeBlockDao(EditBlockActivity instance, TimeBlockDao timeBlockDao) {
    instance.timeBlockDao = timeBlockDao;
  }
}
