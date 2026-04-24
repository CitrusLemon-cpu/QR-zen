package com.qrzen.app.ui.unlock;

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
public final class UnlockChallengeActivity_MembersInjector implements MembersInjector<UnlockChallengeActivity> {
  private final Provider<AppBlockDao> daoProvider;

  private final Provider<TimeBlockDao> timeBlockDaoProvider;

  public UnlockChallengeActivity_MembersInjector(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider) {
    this.daoProvider = daoProvider;
    this.timeBlockDaoProvider = timeBlockDaoProvider;
  }

  public static MembersInjector<UnlockChallengeActivity> create(Provider<AppBlockDao> daoProvider,
      Provider<TimeBlockDao> timeBlockDaoProvider) {
    return new UnlockChallengeActivity_MembersInjector(daoProvider, timeBlockDaoProvider);
  }

  @Override
  public void injectMembers(UnlockChallengeActivity instance) {
    injectDao(instance, daoProvider.get());
    injectTimeBlockDao(instance, timeBlockDaoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.unlock.UnlockChallengeActivity.dao")
  public static void injectDao(UnlockChallengeActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }

  @InjectedFieldSignature("com.qrzen.app.ui.unlock.UnlockChallengeActivity.timeBlockDao")
  public static void injectTimeBlockDao(UnlockChallengeActivity instance,
      TimeBlockDao timeBlockDao) {
    instance.timeBlockDao = timeBlockDao;
  }
}
