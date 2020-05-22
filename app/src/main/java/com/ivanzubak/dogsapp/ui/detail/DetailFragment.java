package com.ivanzubak.dogsapp.ui.detail;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ivanzubak.dogsapp.R;
import com.ivanzubak.dogsapp.databinding.FragmentDetailBinding;
import com.ivanzubak.dogsapp.databinding.SendSmsDialogBinding;
import com.ivanzubak.dogsapp.data.DogBreed;
import com.ivanzubak.dogsapp.data.DogPallete;
import com.ivanzubak.dogsapp.data.SmsInfo;
import com.ivanzubak.dogsapp.ui.MainActivity;

public class DetailFragment extends Fragment {
    private int dogUuid;
    private DetailViewModel viewModel;
    private FragmentDetailBinding binding;
    private DogBreed currentDog;

    private Boolean sendSmsStarted = false;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null) {
            dogUuid = DetailFragmentArgs.fromBundle(getArguments()).getDogUuid();
        }

        viewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        viewModel.fetch(dogUuid);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getDog().observe(this, dogBreed -> {
            if(dogBreed != null && dogBreed instanceof DogBreed && getContext() != null){
                currentDog = dogBreed;
                binding.setDog(dogBreed);

                if(dogBreed.imageUrl != null) {
                    setupBackgroundColor(dogBreed.imageUrl);
                }
            }
        });

        viewModel.getPostResponse().observe(this, isSuccessful -> {
            if(isSuccessful != null) {
                if(isSuccessful) {
                    Toast.makeText(getContext(), "Post request sent!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupBackgroundColor(String url) {
        Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Palette.from(resource).generate(palette -> {
                    int intColor = palette.getLightMutedSwatch().getRgb();
                    DogPallete dogPallete = new DogPallete(intColor);
                    binding.setPallete(dogPallete);
                });
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_sms: {
                if(!sendSmsStarted) {
                    sendSmsStarted = true;
                    ((MainActivity) getActivity()).checkSmsPermission();
                }
                break;
            }
            case R.id.action_share: {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed");
                intent.putExtra(Intent.EXTRA_TEXT, currentDog.dogBreed);
                intent.putExtra(Intent.EXTRA_STREAM, currentDog.imageUrl);
                startActivity(Intent.createChooser(intent, "Share with"));
                break;
            }

            case R.id.action_send_post: {
                viewModel.sendPostRequest(currentDog);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPermissionResult(Boolean permissionGranted) {
        if(isAdded() && sendSmsStarted && permissionGranted) {
            SmsInfo smsInfo = new SmsInfo("", currentDog.dogBreed, currentDog.imageUrl);

            SendSmsDialogBinding dialogBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.send_sms_dialog,
                    null,
                    false
            );

            new AlertDialog.Builder(getContext())
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton("Send SMS", ((dialogInterface, i) -> {
                        if(!dialogBinding.smsDestination.getText().toString().isEmpty()) {
                            smsInfo.to = dialogBinding.smsDestination.getText().toString();
                            sendSms(smsInfo);
                        }
                    }))
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                    .show();

            sendSmsStarted = false;

            dialogBinding.setSmsInfo(smsInfo);
        }
    }

    private void sendSms(SmsInfo smsInfo) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(smsInfo.to, null, smsInfo.text, pendingIntent, null);
        Toast.makeText(getContext(), "SMS sent!", Toast.LENGTH_SHORT).show();
    }
}
