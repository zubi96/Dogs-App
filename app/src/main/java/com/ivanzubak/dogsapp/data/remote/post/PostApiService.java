package com.ivanzubak.dogsapp.data.remote.post;

import com.ivanzubak.dogsapp.data.DogBreed;
import com.ivanzubak.dogsapp.data.PostResponse;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostApiService {
    private static final String BASE_URL = "https://ena115io9cypi.x.pipedream.net/";

    private PostApi api;

    public PostApiService() {
        api = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(PostApi.class);
    }

    public Single<PostResponse> sendPostRequest(DogBreed dogBreed) {
        return api.sendPostRequest(dogBreed);
    }
}
