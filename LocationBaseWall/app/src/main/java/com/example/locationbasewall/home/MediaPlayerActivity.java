package com.example.locationbasewall.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.locationbasewall.R;
import com.example.locationbasewall.utils.Media;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class MediaPlayerActivity extends AppCompatActivity implements Player.EventListener {
    private String mediaUrl;
    private ImageView mediaPlayerImageView;
    private PlayerView mPlayerView;
    private SimpleExoPlayer player;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private SeekBar seekBar;
    private TextView durationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        mediaPlayerImageView = findViewById(R.id.mediaPlayerImageView);
        mPlayerView = findViewById(R.id.playerView);

        mPlayerView.setUseController(false); // 禁用默认的控制器视图
        View exo_playback_control_view = LayoutInflater.from(this).inflate(R.layout.exo_playback_control_view, mPlayerView, false);
        mPlayerView.addView(exo_playback_control_view);

        // 初始化ExoPlayer
        player = new SimpleExoPlayer.Builder(this)
                .setLoadControl(new DefaultLoadControl())
                .build();

        playButton = exo_playback_control_view.findViewById(R.id.btn_play);
        pauseButton = exo_playback_control_view.findViewById(R.id.btn_pause);
        seekBar = exo_playback_control_view.findViewById(R.id.seek_bar);
        durationTextView = exo_playback_control_view.findViewById(R.id.tv_duration);

        mediaUrl = getIntent().getStringExtra("mediaUrl");

        if (Media.isImageFile(mediaUrl)) {
            mediaPlayerImageView.setVisibility(View.VISIBLE);
            mPlayerView.setVisibility(View.GONE);
            Glide.with(this).load(mediaUrl).into(mediaPlayerImageView);

        } else {
            mediaPlayerImageView.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);

            // 准备视频源
            Uri videoUri = Uri.parse(mediaUrl);
            MediaItem mediaItem = MediaItem.fromUri(videoUri);

            // 准备播放器
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);

            // 将ExoPlayer绑定到PlayerView
            mPlayerView.setPlayer(player);
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                // 播放操作
                player.setPlayWhenReady(true);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                // 暂停操作
                player.setPlayWhenReady(false);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 进度条改变操作
                if (fromUser) {
                    long newPosition = (player.getDuration() * progress) / 1000L;
                    player.seekTo(newPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动进度条
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖动进度条
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放ExoPlayer资源
        player.release();
    }

    // Player.EventListener接口的方法
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                // 视频正在缓冲，显示播放按钮和进度条
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                seekBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                // 视频准备好并可以播放，隐藏播放按钮和进度条
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                break;
            case Player.STATE_ENDED:
                // 视频播放结束，显示播放按钮和进度条
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                seekBar.setVisibility(View.VISIBLE);
                break;
            default:
                // 其他状态，隐藏播放按钮和进度条
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                break;
        }
    }
}
