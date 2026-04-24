package com.qrzen.app.di;

import com.qrzen.app.data.db.AppBlockDao;
import com.qrzen.app.data.db.AppDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideAppBlockDaoFactory implements Factory<AppBlockDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAppBlockDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AppBlockDao get() {
    return provideAppBlockDao(dbProvider.get());
  }

  public static AppModule_ProvideAppBlockDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAppBlockDaoFactory(dbProvider);
  }

  public static AppBlockDao provideAppBlockDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAppBlockDao(db));
  }
}
