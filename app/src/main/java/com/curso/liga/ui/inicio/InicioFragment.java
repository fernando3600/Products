package com.curso.liga.ui.inicio;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.curso.liga.R;
import com.curso.liga.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {

    private InicioViewModel inicioViewModel;
    private FragmentInicioBinding binding;
    VideoView video;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        inicioViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        View root = inflater.inflate(R.layout.fragment_inicio, container, false);

        video = root.findViewById(R.id.vvvVideo);

        String path = "android.resource://com.curso.liga/" + R.raw.video1;

        video.setVideoURI(Uri.parse(path));
        video.setMediaController(new MediaController(getContext()));
        video.start();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}