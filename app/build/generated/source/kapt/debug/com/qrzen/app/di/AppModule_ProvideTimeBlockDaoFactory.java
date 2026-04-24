package com.qrzen.app.di;

import com.qrzen.app.data.db.AppDatabase;
import com.qrzen.app.data.db.TimeBlockDao;
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
public final class AppModule_ProvideTimeBlockDaoFactory implements Factory<TimeBlockDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideTimeBlockDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TimeBlockDao get() {
    return provideTimeBlockDao(dbProvider.get());
  }

  public static AppModule_ProvideTimeBlockDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideTimeBlockDaoFactory(dbProvider);
  }

  public static TimeBlockDao provideTimeBlockDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTimeBlockDao(db));
  }
}
