package com.qrzen.app.ui.allowlist;

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
public final class AllowlistOverlayActivity_MembersInjector implements MembersInjector<AllowlistOverlayActivity> {
  private final Provider<AppBlockDao> daoProvider;

  private final Provider<BlockEventDao> blockEventDaoProvider;

  public AllowlistOverlayActivity_MembersInjector(Provider<AppBlockDao> daoProvider,
      Provider<BlockEventDao> blockEventDaoProvider) {
    this.daoProvider = daoProvider;
    this.blockEventDaoProvider = blockEventDaoProvider;
  }

  public static MembersInjector<AllowlistOverlayActivity> create(Provider<AppBlockDao> daoProvider,
      Provider<BlockEventDao> blockEventDaoProvider) {
    return new AllowlistOverlayActivity_MembersInjector(daoProvider, blockEventDaoProvider);
  }

  @Override
  public void injectMembers(AllowlistOverlayActivity instance) {
    injectDao(instance, daoProvider.get());
    injectBlockEventDao(instance, blockEventDaoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.dao")
  public static void injectDao(AllowlistOverlayActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }

  @InjectedFieldSignature("com.qrzen.app.ui.allowlist.AllowlistOverlayActivity.blockEventDao")
  public static void injectBlockEventDao(AllowlistOverlayActivity instance,
      BlockEventDao blockEventDao) {
    instance.blockEventDao = blockEventDao;
  }
}
