package com.qrzen.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.qrzen.app.data.db.AppBlockDao;
import com.qrzen.app.data.db.AppDatabase;
import com.qrzen.app.data.db.BlockEventDao;
import com.qrzen.app.data.db.TimeBlockDao;
import com.qrzen.app.di.AppModule_ProvideAppBlockDaoFactory;
import com.qrzen.app.di.AppModule_ProvideBlockEventDaoFactory;
import com.qrzen.app.di.AppModule_ProvideDatabaseFactory;
import com.qrzen.app.di.AppModule_ProvideTimeBlockDaoFactory;
import com.qrzen.app.service.BackgroundService;
import com.qrzen.app.service.BackgroundService_MembersInjector;
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity;
import com.qrzen.app.ui.allowlist.AllowlistOverlayActivity_MembersInjector;
import com.qrzen.app.ui.block.AppPickerActivity;
import com.qrzen.app.ui.block.AppPickerActivity_MembersInjector;
import com.qrzen.app.ui.block.EditBlockActivity;
import com.qrzen.app.ui.block.EditBlockActivity_MembersInjector;
import com.qrzen.app.ui.lock.LockScreenActivity;
import com.qrzen.app.ui.lock.LockScreenActivity_MembersInjector;
import com.qrzen.app.ui.main.HomeFragment;
import com.qrzen.app.ui.main.HomeViewModel;
import com.qrzen.app.ui.main.HomeViewModel_HiltModules;
import com.qrzen.app.ui.main.MainActivity;
import com.qrzen.app.ui.main.SettingsFragment;
import com.qrzen.app.ui.main.SettingsFragment_MembersInjector;
import com.qrzen.app.ui.main.StatsFragment;
import com.qrzen.app.ui.main.StatsViewModel;
import com.qrzen.app.ui.main.StatsViewModel_HiltModules;
import com.qrzen.app.ui.pomodoro.PomodoroActivity;
import com.qrzen.app.ui.pomodoro.PomodoroActivity_MembersInjector;
import com.qrzen.app.ui.unlock.UnlockChallengeActivity;
import com.qrzen.app.ui.unlock.UnlockChallengeActivity_MembersInjector;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerQrZenApp_HiltComponents_SingletonC {
  private DaggerQrZenApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public QrZenApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements QrZenApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements QrZenApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements QrZenApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements QrZenApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements QrZenApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements QrZenApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements QrZenApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public QrZenApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends QrZenApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends QrZenApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public void injectHomeFragment(HomeFragment homeFragment) {
    }

    @Override
    public void injectSettingsFragment(SettingsFragment settingsFragment) {
      injectSettingsFragment2(settingsFragment);
    }

    @Override
    public void injectStatsFragment(StatsFragment statsFragment) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }

    @CanIgnoreReturnValue
    private SettingsFragment injectSettingsFragment2(SettingsFragment instance) {
      SettingsFragment_MembersInjector.injectDao(instance, singletonCImpl.provideAppBlockDaoProvider.get());
      return instance;
    }
  }

  private static final class ViewCImpl extends QrZenApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends QrZenApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectAllowlistOverlayActivity(AllowlistOverlayActivity allowlistOverlayActivity) {
      injectAllowlistOverlayActivity2(allowlistOverlayActivity);
    }

    @Override
    public void injectAppPickerActivity(AppPickerActivity appPickerActivity) {
      injectAppPickerActivity2(appPickerActivity);
    }

    @Override
    public void injectEditBlockActivity(EditBlockActivity editBlockActivity) {
      injectEditBlockActivity2(editBlockActivity);
    }

    @Override
    public void injectLockScreenActivity(LockScreenActivity lockScreenActivity) {
      injectLockScreenActivity2(lockScreenActivity);
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectPomodoroActivity(PomodoroActivity pomodoroActivity) {
      injectPomodoroActivity2(pomodoroActivity);
    }

    @Override
    public void injectUnlockChallengeActivity(UnlockChallengeActivity unlockChallengeActivity) {
      injectUnlockChallengeActivity2(unlockChallengeActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(2).put(LazyClassKeyProvider.com_qrzen_app_ui_main_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_qrzen_app_ui_main_StatsViewModel, StatsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private AllowlistOverlayActivity injectAllowlistOverlayActivity2(
        AllowlistOverlayActivity instance) {
      AllowlistOverlayActivity_MembersInjector.injectDao(instance, singletonCImpl.provideAppBlockDaoProvider.get());
      AllowlistOverlayActivity_MembersInjector.injectTimeBlockDao(instance, singletonCImpl.provideTimeBlockDaoProvider.get());
      AllowlistOverlayActivity_MembersInjector.injectBlockEventDao(instance, singletonCImpl.provideBlockEventDaoProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private AppPickerActivity injectAppPickerActivity2(AppPickerActivity instance2) {
      AppPickerActivity_MembersInjector.injectDao(instance2, singletonCImpl.provideAppBlockDaoProvider.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private EditBlockActivity injectEditBlockActivity2(EditBlockActivity instance3) {
      EditBlockActivity_MembersInjector.injectDao(instance3, singletonCImpl.provideAppBlockDaoProvider.get());
      EditBlockActivity_MembersInjector.injectTimeBlockDao(instance3, singletonCImpl.provideTimeBlockDaoProvider.get());
      return instance3;
    }

    @CanIgnoreReturnValue
    private LockScreenActivity injectLockScreenActivity2(LockScreenActivity instance4) {
      LockScreenActivity_MembersInjector.injectDao(instance4, singletonCImpl.provideAppBlockDaoProvider.get());
      LockScreenActivity_MembersInjector.injectBlockEventDao(instance4, singletonCImpl.provideBlockEventDaoProvider.get());
      LockScreenActivity_MembersInjector.injectTimeBlockDao(instance4, singletonCImpl.provideTimeBlockDaoProvider.get());
      return instance4;
    }

    @CanIgnoreReturnValue
    private PomodoroActivity injectPomodoroActivity2(PomodoroActivity instance5) {
      PomodoroActivity_MembersInjector.injectDao(instance5, singletonCImpl.provideAppBlockDaoProvider.get());
      return instance5;
    }

    @CanIgnoreReturnValue
    private UnlockChallengeActivity injectUnlockChallengeActivity2(
        UnlockChallengeActivity instance6) {
      UnlockChallengeActivity_MembersInjector.injectDao(instance6, singletonCImpl.provideAppBlockDaoProvider.get());
      UnlockChallengeActivity_MembersInjector.injectTimeBlockDao(instance6, singletonCImpl.provideTimeBlockDaoProvider.get());
      return instance6;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_qrzen_app_ui_main_StatsViewModel = "com.qrzen.app.ui.main.StatsViewModel";

      static String com_qrzen_app_ui_main_HomeViewModel = "com.qrzen.app.ui.main.HomeViewModel";

      @KeepFieldType
      StatsViewModel com_qrzen_app_ui_main_StatsViewModel2;

      @KeepFieldType
      HomeViewModel com_qrzen_app_ui_main_HomeViewModel2;
    }
  }

  private static final class ViewModelCImpl extends QrZenApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<StatsViewModel> statsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.statsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(2).put(LazyClassKeyProvider.com_qrzen_app_ui_main_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_qrzen_app_ui_main_StatsViewModel, ((Provider) statsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_qrzen_app_ui_main_StatsViewModel = "com.qrzen.app.ui.main.StatsViewModel";

      static String com_qrzen_app_ui_main_HomeViewModel = "com.qrzen.app.ui.main.HomeViewModel";

      @KeepFieldType
      StatsViewModel com_qrzen_app_ui_main_StatsViewModel2;

      @KeepFieldType
      HomeViewModel com_qrzen_app_ui_main_HomeViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.qrzen.app.ui.main.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.provideAppBlockDaoProvider.get(), singletonCImpl.provideTimeBlockDaoProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.qrzen.app.ui.main.StatsViewModel 
          return (T) new StatsViewModel(singletonCImpl.provideBlockEventDaoProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends QrZenApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends QrZenApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectBackgroundService(BackgroundService backgroundService) {
      injectBackgroundService2(backgroundService);
    }

    @CanIgnoreReturnValue
    private BackgroundService injectBackgroundService2(BackgroundService instance) {
      BackgroundService_MembersInjector.injectDao(instance, singletonCImpl.provideAppBlockDaoProvider.get());
      BackgroundService_MembersInjector.injectTimeBlockDao(instance, singletonCImpl.provideTimeBlockDaoProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends QrZenApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<AppBlockDao> provideAppBlockDaoProvider;

    private Provider<BlockEventDao> provideBlockEventDaoProvider;

    private Provider<TimeBlockDao> provideTimeBlockDaoProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.provideAppBlockDaoProvider = DoubleCheck.provider(new SwitchingProvider<AppBlockDao>(singletonCImpl, 0));
      this.provideBlockEventDaoProvider = DoubleCheck.provider(new SwitchingProvider<BlockEventDao>(singletonCImpl, 2));
      this.provideTimeBlockDaoProvider = DoubleCheck.provider(new SwitchingProvider<TimeBlockDao>(singletonCImpl, 3));
    }

    @Override
    public void injectQrZenApp(QrZenApp qrZenApp) {
    }

    @Override
    public AppBlockDao appBlockDao() {
      return provideAppBlockDaoProvider.get();
    }

    @Override
    public BlockEventDao blockEventDao() {
      return provideBlockEventDaoProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.qrzen.app.data.db.AppBlockDao 
          return (T) AppModule_ProvideAppBlockDaoFactory.provideAppBlockDao(singletonCImpl.provideDatabaseProvider.get());

          case 1: // com.qrzen.app.data.db.AppDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.qrzen.app.data.db.BlockEventDao 
          return (T) AppModule_ProvideBlockEventDaoFactory.provideBlockEventDao(singletonCImpl.provideDatabaseProvider.get());

          case 3: // com.qrzen.app.data.db.TimeBlockDao 
          return (T) AppModule_ProvideTimeBlockDaoFactory.provideTimeBlockDao(singletonCImpl.provideDatabaseProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
