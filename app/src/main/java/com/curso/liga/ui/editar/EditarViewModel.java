package com.curso.liga.ui.editar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public EditarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("FRAGMENTO EDITAR");
    }

    public LiveData<String> getText() {
        return mText;
    }
}