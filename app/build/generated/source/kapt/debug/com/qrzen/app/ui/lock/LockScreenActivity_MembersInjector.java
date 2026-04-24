package com.qrzen.app.ui.lock;

import com.qrzen.app.data.db.AppBlockDao;
import com.qrzen.app.data.db.BlockEventDao;
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
public final class LockScreenActivity_MembersInjector implements MembersInjector<LockScreenActivity> {
  private final Provider<AppBlockDao> daoProvider;

  private final Provider<BlockEventDao> blockEventDaoProvider;

  public LockScreenActivity_MembersInjector(Provider<AppBlockDao> daoProvider,
      Provider<BlockEventDao> blockEventDaoProvider) {
    this.daoProvider = daoProvider;
    this.blockEventDaoProvider = blockEventDaoProvider;
  }

  public static MembersInjector<LockScreenActivity> create(Provider<AppBlockDao> daoProvider,
      Provider<BlockEventDao> blockEventDaoProvider) {
    return new LockScreenActivity_MembersInjector(daoProvider, blockEventDaoProvider);
  }

  @Override
  public void injectMembers(LockScreenActivity instance) {
    injectDao(instance, daoProvider.get());
    injectBlockEventDao(instance, blockEventDaoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.lock.LockScreenActivity.dao")
  public static void injectDao(LockScreenActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }

  @InjectedFieldSignature("com.qrzen.app.ui.lock.LockScreenActivity.blockEventDao")
  public static void injectBlockEventDao(LockScreenActivity instance, BlockEventDao blockEventDao) {
    instance.blockEventDao = blockEventDao;
  }
}
