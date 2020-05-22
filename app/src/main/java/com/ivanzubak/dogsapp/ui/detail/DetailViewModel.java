package com.ivanzubak.dogsapp.ui.detail;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ivanzubak.dogsapp.data.DogBreed;
import com.ivanzubak.dogsapp.data.PostResponse;
import com.ivanzubak.dogsapp.data.db.DogDatabase;
import com.ivanzubak.dogsapp.data.remote.post.PostApiService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DetailViewModel extends AndroidViewModel {
    private MutableLiveData<DogBreed> dogLiveData = new MutableLiveData<DogBreed>();
    private MutableLiveData<Boolean> postResponseLiveData = new MutableLiveData<Boolean>();
    private PostApiService postApiService = new PostApiService();
    private CompositeDisposable disposable = new CompositeDisposable();
    private RetrieveDogTask task;

    public DetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<DogBreed> getDog() {
        return dogLiveData;
    }

    public LiveData<Boolean> getPostResponse() {
        return postResponseLiveData;
    }

    public void fetch(int uuid) {
        task = new RetrieveDogTask();
        task.execute(uuid);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(task != null) {
            task.cancel(true);
            task = null;
        }

        disposable.clear();
    }

    private class RetrieveDogTask extends AsyncTask<Integer, Void, DogBreed> {

        @Override
        protected DogBreed doInBackground(Integer... integers) {
            int uuid = integers[0];
            return DogDatabase.getInstance(getApplication()).dogDao().getDog(uuid);
        }

        @Override
        protected void onPostExecute(DogBreed dogBreed) {
            dogLiveData.setValue(dogBreed);
        }
    }

    public void sendPostRequest(DogBreed dogBreed) {
            disposable.add(
                    postApiService.sendPostRequest(dogBreed)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<PostResponse>() {
                                @Override
                                public void onSuccess(PostResponse postResponse) {
                                    postResponseLiveData.setValue(postResponse.success);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
            );
    }
}
