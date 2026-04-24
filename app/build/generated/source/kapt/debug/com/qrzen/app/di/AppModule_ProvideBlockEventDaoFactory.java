package com.qrzen.app.di;

import com.qrzen.app.data.db.AppDatabase;
import com.qrzen.app.data.db.BlockEventDao;
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
public final class AppModule_ProvideBlockEventDaoFactory implements Factory<BlockEventDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideBlockEventDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BlockEventDao get() {
    return provideBlockEventDao(dbProvider.get());
  }

  public static AppModule_ProvideBlockEventDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideBlockEventDaoFactory(dbProvider);
  }

  public static BlockEventDao provideBlockEventDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBlockEventDao(db));
  }
}
