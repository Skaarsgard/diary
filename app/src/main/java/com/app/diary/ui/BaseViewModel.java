package com.app.diary.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class BaseViewModel extends AndroidViewModel {

    protected boolean loaded;//是否已加载过数据
    private CompositeDisposable compositeDisposable;

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

}