/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.app.screencast.content;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.camera.CameraPreviewServiceProxy;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.modle.Video;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.provider.VideoProvider;
import dev.nick.app.screencast.tools.MediaTools;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCastActivity extends TransactionSafeActivity {

    private static final int PERMISSION_CODE = 1;
    protected boolean mReadyToRun;
    protected Logger mLogger;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private boolean mIsCasting;
    private ProgressDialog mProgressDialog;
    private int mRemainingSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(getContentViewId());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            new AlertDialog.Builder(ScreenCastActivity.this)
                    .setTitle(R.string.warn_sdk_title)
                    .setMessage(R.string.warn_sdk_low)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
            return;
        }

        mReadyToRun = true;

        mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        initUI();
    }

    protected void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFab.show();
                } else {
                    mFab.hide();
                }
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCasting) {
                    stopRecording();
                    CameraPreviewServiceProxy.hide(getApplicationContext());
                } else {
                    ScreenCastActivityPermissionsDispatcher.startRecordingWithCheck(ScreenCastActivity.this);
                    if (SettingsProvider.get().withCamera()) {
                        ScreenCastActivityPermissionsDispatcher.showCameraPreviewWithCheck(ScreenCastActivity.this);
                    }
                }
            }
        });
        showVideoList();
        if (SettingsProvider.get().firstStart()
                || SettingsProvider.get().getAppVersionNum() < SettingsProvider.APP_VERSION_INT) {
            showRetation();
        }
    }

    protected void showCountdown(String content) {
        boolean showCD = SettingsProvider.get().showCD();
        if (showCD) {
            SuperToast.cancelAllSuperToasts();
            SuperToast.create(this, content, Style.DURATION_LONG, Style.red())
                    .setText(content)
                    .setFrame(Style.FRAME_KITKAT)
                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                    .setAnimations(Style.ANIMATIONS_POP).show();
        }
    }

    protected int getContentViewId() {
        return R.layout.navigator_content;
    }

    public void showRetation() {
        new AlertDialog.Builder(ScreenCastActivity.this)
                .setTitle(R.string.title_perm_require)
                .setMessage(R.string.summary_perm_require)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
                        SettingsProvider.get().setFirstStart(false);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ScreencastServiceProxy.watch(getApplicationContext(), new IScreencaster.ICastWatcher() {
            @Override
            public void onStartCasting() {
                LoggerManager.getLogger(ScreenCastActivity.class).debug("onStartCasting");
                refreshState(true);
            }

            @Override
            public void onStopCasting() {
                if (!mReadyToRun || SettingsProvider.get().firstStart()) return;
                refreshState(false);
                ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
                    }
                }, 1000);// Waiting for the Scanner.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mReadyToRun || SettingsProvider.get().firstStart()) return;
        ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void readVideos() {
        ThreadUtil.newThread(new Runnable() {
            @Override
            public void run() {
                final List<Video> videos = new VideoProvider(getApplicationContext()).getList();

                ThreadUtil.getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.update(videos);
                        updateHint();
                    }
                });
            }
        }).run();
    }

    List<Video> getAdded(List<Video> nows) {
        List<Video> added = new ArrayList<>();
        List<Video> olds = new ArrayList<>(mAdapter.data);
        for (Video v : nows) {
            if (!olds.contains(v)) {
                added.add(v);
            }
        }
        return added;
    }

    List<Video> getRemoved(List<Video> nows) {
        List<Video> rm = new ArrayList<>();
        List<Video> olds = new ArrayList<>(mAdapter.data);
        for (Video v : olds) {
            if (!nows.contains(v)) {
                rm.add(v);
            }
        }
        return rm;
    }

    void updateHint() {
        boolean hasVideo = mAdapter != null && mAdapter.getItemCount() > 0;
        if (!hasVideo) {
            TextView textView = (TextView) findViewById(R.id.hint_text);
            if (!mIsCasting) {
                textView.setText(R.string.start_description);
            } else {
                textView.setText(R.string.stop_description);
            }
        }
        findViewById(R.id.hint_area).setVisibility(hasVideo ? View.GONE : View.VISIBLE);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCameraPreview() {
        CameraPreviewServiceProxy.show(getApplicationContext(), SettingsProvider.get().previewSize());
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void startRecording() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                    PERMISSION_CODE);
            return;
        }

        ScreencastServiceProxy.start(getApplicationContext(), mMediaProjection, SettingsProvider.get().withAudio());

        if (SettingsProvider.get().startDelay() > 0) {
            mFab.hide();
            long delay = SettingsProvider.get().startDelay();
            mRemainingSeconds = (int) (delay / 1000);
            showCountdown(String.valueOf(mRemainingSeconds));
            mRemainingSeconds--;
            mLogger.debug(mRemainingSeconds);
            new CountDownTimer(delay, 1000) {

                @Override
                public void onTick(long l) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLogger.debug("Tick");
                            showCountdown(String.valueOf(mRemainingSeconds));
                            mRemainingSeconds--;
                        }
                    });
                }

                @Override
                public void onFinish() {
                    mRemainingSeconds = 0;
                    SuperToast.cancelAllSuperToasts();
                    mFab.show();
                }
            }.start();
        }

        if (SettingsProvider.get().hideAppWhenStart()) finish();
    }

    private void stopRecording() {
        ScreencastServiceProxy.stop(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ScreenCastActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            LoggerManager.getLogger(getClass()).error("Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                onProjectionStop();
            }
        }, null);
        startRecording();
    }

    private void onProjectionStop() {
        mMediaProjection = null;
        ScreencastServiceProxy.stop(getApplicationContext());
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNoStoragePermission() {
        finish();
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    void onNoAudioPermission() {
        SettingsProvider.get().setWithAudio(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshState(final boolean isCasting) {
        mIsCasting = isCasting;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCasting) {
                    mFab.setImageResource(R.drawable.stop);
                } else {
                    mFab.setImageResource(R.drawable.record);
                }
                updateHint();
            }
        });
    }

    protected void showVideoList() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        ImageView thumbnail;
        View actionBtn;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            actionBtn = itemView.findViewById(R.id.hint);
            thumbnail = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final File RECORDINGS_GIF_DIR = new File(Environment.getExternalStorageDirectory().getPath(), SettingsProvider.STORAGE_GIF_FOLDER_NAME);

        private final List<Video> data;

        public Adapter(List<Video> data) {
            this.data = data;
        }

        public Adapter() {
            this(new ArrayList<Video>());
        }

        public void update(List<Video> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Video video, int position) {
            this.data.add(position, video);
            notifyItemInserted(position);
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_card_item, parent, false);
            return new TwoLinesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, int position) {
            final Video item = data.get(position);
            holder.title.setText(item.getTitle());
            String descriptionText = item.getDuration();
            holder.description.setText(descriptionText);
            holder.actionBtn.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(ScreenCastActivity.this, holder.actionBtn);
                    popupMenu.inflate(R.menu.actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_play:
                                    startActivity(MediaTools.buildOpenIntent(ScreenCastActivity.this,
                                            new File(item.getPath())));
                                    break;
                                case R.id.action_remove:
                                    ThreadUtil.getWorkThreadHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new File(item.getPath()).delete();
                                            remove(holder.getAdapterPosition());
                                        }
                                    });
                                    break;
                                case R.id.action_rename:
                                    ThreadUtil.getMainThreadHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showRenameDialog(item.getPath());
                                        }
                                    });
                                    break;
                                case R.id.action_share:
                                    startActivity(MediaTools.buildSharedIntent(ScreenCastActivity.this,
                                            new File(item.getPath())));
                                    break;
//                                case R.id.action_togif:
//                                    new AsyncTask<Void, Void, Void>() {
//                                        @Override
//                                        protected void onPreExecute() {
//                                            super.onPreExecute();
//                                            if (mProgressDialog == null) {
//                                                mProgressDialog = new ProgressDialog(ScreenCastActivity.this);
//                                                mProgressDialog.setIndeterminate(true);
//                                                mProgressDialog.setCancelable(false);
//                                            }
//                                            mProgressDialog.show();
//                                        }
//
//                                        @Override
//                                        protected void onPostExecute(Void aVoid) {
//                                            super.onPostExecute(aVoid);
//                                            mProgressDialog.dismiss();
//                                        }
//
//                                        @Override
//                                        protected Void doInBackground(Void... voids) {
//                                            toGif(item.getPath(), item.getTitle(), RECORDINGS_GIF_DIR.getAbsolutePath());
//                                            return null;
//                                        }
//                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
            Glide.with(getApplicationContext()).load(item.getPath()).into(holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        void showRenameDialog(final String fromPath) {
            View editTextContainer = LayoutInflater.from(ScreenCastActivity.this).inflate(dev.nick.tiles.R.layout.dialog_edit_text, null, false);
            final EditText editText = (EditText) editTextContainer.findViewById(dev.nick.tiles.R.id.edit_text);
            AlertDialog alertDialog = new AlertDialog.Builder(ScreenCastActivity.this)
                    .setView(editTextContainer)
                    .setTitle(R.string.action_rename)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ThreadUtil.getWorkThreadHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    File parent = new File(fromPath).getParentFile();
                                    File to = new File(parent, editText.getText().toString() + ".mp4");
                                    new File(fromPath).renameTo(to);
                                    MediaScannerConnection.scanFile(getApplicationContext(),
                                            new String[]{to.getAbsolutePath()}, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    LoggerManager.getLogger(getClass()).info("MediaScanner scanned recording " + path);
                                                    readVideos();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alertDialog.show();
        }

        void toGif(String fromFilePath, String fileName, String toDir) {

        }
    }
}
