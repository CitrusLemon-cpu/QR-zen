package com.qrzen.app.ui.unlock;

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
public final class UnlockChallengeActivity_MembersInjector implements MembersInjector<UnlockChallengeActivity> {
  private final Provider<AppBlockDao> daoProvider;

  public UnlockChallengeActivity_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<UnlockChallengeActivity> create(Provider<AppBlockDao> daoProvider) {
    return new UnlockChallengeActivity_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(UnlockChallengeActivity instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.unlock.UnlockChallengeActivity.dao")
  public static void injectDao(UnlockChallengeActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
