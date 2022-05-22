package com.curso.liga.ui.agregar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AgregarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AgregarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("FRAGMENTO AGREGAR");
    }

    public LiveData<String> getText() {
        return mText;
    }
}