package ca.scooter.androidpractice.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

class Repository {
    private final String name;
    private final String desciption;
    private final String url;

    public Repository(String name, String desciption, String url){
        this.name = name;
        this.desciption = desciption;
        this.url = url;
    }
    public String getName(){
        return name;
    }
    public String getDesciption(){
        return desciption;
    }
    public String getUrl(){
        return url;
    }
}

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<Repository>> _repositories = new MutableLiveData<>();
    public LiveData<List<Repository>> repositories = _repositories;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error_message = new MutableLiveData<>();
    public LiveData<String> error_message = _error_message;

    public HomeViewModel(MutableLiveData<String> mText, MutableLiveData<String> mText1){
        this.mText = mText;
        mText = new MutableLiveData<>();
        mText.setValue("Testing 👀");
    }
    public LiveData<String> getText(){
        return mText;
    }

}