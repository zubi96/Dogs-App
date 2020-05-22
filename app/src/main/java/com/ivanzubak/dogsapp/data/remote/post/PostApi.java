package com.ivanzubak.dogsapp.data.remote.post;

import com.ivanzubak.dogsapp.data.DogBreed;
import com.ivanzubak.dogsapp.data.PostResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostApi {
    @POST("./")
    Single<PostResponse> sendPostRequest(@Body DogBreed dogBreed);
}
