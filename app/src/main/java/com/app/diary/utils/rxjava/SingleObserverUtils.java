package com.app.diary.utils.rxjava;

import androidx.annotation.Nullable;

import com.app.diary.ui.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SingleObserverUtils {

    /**
     * UI线程调度器
     */
    public static <T> SingleTransformer<T, T> applyUIScheduler(@Nullable BaseViewModel viewModel) {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    if (viewModel != null) {
                        viewModel.addDisposable(disposable);
                    }
                });
    }

    /**
     * IO线程调度器
     */
    public static <T> SingleTransformer<T, T> applyIOScheduler(@Nullable BaseViewModel viewModel) {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    if (viewModel != null) {
                        viewModel.addDisposable(disposable);
                    }
                });
    }

}