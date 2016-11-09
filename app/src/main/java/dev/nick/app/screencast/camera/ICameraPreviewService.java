package dev.nick.app.screencast.camera;

public interface ICameraPreviewService {
    void show(int size);

    void hide();

    boolean isShowing();

    void setSize(int sizeIndex);
}
