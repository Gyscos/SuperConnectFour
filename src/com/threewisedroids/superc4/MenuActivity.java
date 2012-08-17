package com.threewisedroids.superc4;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ToggleButton;

@SuppressWarnings("deprecation")
public class MenuActivity extends Activity {

    Dialog dialog;

    public Dialog makeNewGameDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_game);
        dialog.setTitle(R.string.new_game);

        dialog.findViewById(R.id.button_confirm_new_game).setOnClickListener(
                new View.OnClickListener() {

                    public void onClick(View v) {
                        boolean useAi = ((ToggleButton) dialog
                                .findViewById(R.id.opponent_toggle))
                                .isChecked();

                        boolean useSize10 = ((RadioButton) dialog
                                .findViewById(R.id.size_10x10)).isChecked();

                        boolean fillCorner = ((CheckBox) dialog
                                .findViewById(R.id.check_fill_corner))
                                .isChecked();

                        newGame(useAi, useSize10, fillCorner);
                    }
                });

        return dialog;
    }

    public void newGame(boolean useAi, boolean useSize10, boolean fillCorner) {
        // Starts a new activity...
        Intent intent = new Intent(this, GameActivity.class);

        intent.putExtra("useAI", useAi);
        intent.putExtra("gridSize", useSize10 ? 10 : 8);
        intent.putExtra("fillCorners", fillCorner);

        startActivity(intent);
        dialog.dismiss();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.button_exit).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(final View v) {
                        // Quit
                        finish();
                    }
                });

        findViewById(R.id.button_new_game).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(final View v) {
                        // Show a popup
                        showDialog(0);
                    }
                });
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case 0:
                return makeNewGameDialog();
            case 1:
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_rules);
                dialog.setTitle(R.string.rules);
                return dialog;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_see_rules:
                // Show Rules Dialog
                showDialog(1);
                return true;
            case R.id.menu_settings:
                // Start Setting Activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }
}
