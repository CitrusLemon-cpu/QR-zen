package com.qrzen.app.ui.pomodoro;

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
public final class PomodoroActivity_MembersInjector implements MembersInjector<PomodoroActivity> {
  private final Provider<AppBlockDao> daoProvider;

  public PomodoroActivity_MembersInjector(Provider<AppBlockDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  public static MembersInjector<PomodoroActivity> create(Provider<AppBlockDao> daoProvider) {
    return new PomodoroActivity_MembersInjector(daoProvider);
  }

  @Override
  public void injectMembers(PomodoroActivity instance) {
    injectDao(instance, daoProvider.get());
  }

  @InjectedFieldSignature("com.qrzen.app.ui.pomodoro.PomodoroActivity.dao")
  public static void injectDao(PomodoroActivity instance, AppBlockDao dao) {
    instance.dao = dao;
  }
}
