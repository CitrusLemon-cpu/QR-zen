package com.qrzen.app.ui.main;

import android.content.Context;
import com.qrzen.app.data.db.AppBlockDao;
import com.qrzen.app.data.db.TimeBlockDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<AppBlockDao> daoProvider;

  private final Provider<TimeBlockDao> timeBlockDaoProvider;

  private final Provider<Context> ctxProvider;

  public HomeViewModel_Factory(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider, Provider<Context> ctxProvider) {
    this.daoProvider = daoProvider;
    this.timeBlockDaoProvider = timeBlockDaoProvider;
    this.ctxProvider = ctxProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(daoProvider.get(), timeBlockDaoProvider.get(), ctxProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider, Provider<Context> ctxProvider) {
    return new HomeViewModel_Factory(daoProvider, timeBlockDaoProvider, ctxProvider);
  }

  public static HomeViewModel newInstance(AppBlockDao dao, TimeBlockDao timeBlockDao, Context ctx) {
    return new HomeViewModel(dao, timeBlockDao, ctx);
  }
}
