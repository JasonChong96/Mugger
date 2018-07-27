package com.bojio.mugger.lifecycle;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;

public class LifecycleUtils {
  private static ViewModelProvider.AndroidViewModelFactory factory = null;
  private static Application curApp = null;

  public static ViewModelProvider.AndroidViewModelFactory getAndroidViewModelFactory(Application
                                                                                         app) {
    if (factory == null || curApp == null || !app.equals(curApp)) {
      curApp = app;
      factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app);
    }
    return factory;
  }
}
