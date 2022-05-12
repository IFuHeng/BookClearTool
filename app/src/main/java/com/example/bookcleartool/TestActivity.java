package com.example.bookcleartool;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileDescriptor;

public class TestActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_main);
        showPopupWindow(findViewById(R.id.btn_exit));
    }

    private void showPopupWindow(View view) {
        PopupWindow popupWindow = new PopupWindow();
    }

    class TestService2 extends Binder{

    }
    class TestService implements IBinder {

        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return null;
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return false;
        }
    }
}
