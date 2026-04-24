package com.qrzen.app.ui.main;

import com.qrzen.app.data.db.BlockEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class StatsViewModel_Factory implements Factory<StatsViewModel> {
  private final Provider<BlockEventDao> daoProvider;

  public StatsViewModel_Factory(Provider<BlockEventDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(daoProvider.get());
  }

  public static StatsViewModel_Factory create(Provider<BlockEventDao> daoProvider) {
    return new StatsViewModel_Factory(daoProvider);
  }

  public static StatsViewModel newInstance(BlockEventDao dao) {
    return new StatsViewModel(dao);
  }
}
