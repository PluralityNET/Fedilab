package app.fedilab.android.fragments;
/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;

import static android.app.Activity.RESULT_OK;


/**
 * Created by Thomas on 29/04/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsNotificationsFragment extends Fragment {


    private Context context;
    private int style;
    private static final int ACTIVITY_CHOOSE_SOUND = 412;
    int count = 0;
    int count1 = 0;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings_notifications, container, false);
        context = getContext();
        assert context != null;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            style = R.style.DialogDark;
        }else {
            style = R.style.Dialog;
        }

        boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
        final SwitchCompat switchCompatNotify = rootView.findViewById(R.id.set_notify);
        switchCompatNotify.setChecked(notify);
        final LinearLayout notification_settings = rootView.findViewById(R.id.notification_settings);
        if( notify)
            notification_settings.setVisibility(View.VISIBLE);
        else
            notification_settings.setVisibility(View.GONE);
        switchCompatNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIFY, isChecked);
                editor.apply();
                if( isChecked)
                    notification_settings.setVisibility(View.VISIBLE);
                else
                    notification_settings.setVisibility(View.GONE);
            }
        });



        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        boolean notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL, true);

        boolean notif_wifi = sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false);
        boolean notif_silent = sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT, false);

        boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, false);

        final String time_from = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        final String time_to = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");


        final CheckBox set_notif_follow = rootView.findViewById(R.id.set_notif_follow);
        final CheckBox set_notif_follow_add = rootView.findViewById(R.id.set_notif_follow_add);
        final CheckBox set_notif_follow_ask = rootView.findViewById(R.id.set_notif_follow_ask);
        final CheckBox set_notif_follow_mention = rootView.findViewById(R.id.set_notif_follow_mention);
        final CheckBox set_notif_follow_share = rootView.findViewById(R.id.set_notif_follow_share);
        final CheckBox set_notif_follow_poll = rootView.findViewById(R.id.set_notif_follow_poll);

        final CheckBox set_notif_hometimeline = rootView.findViewById(R.id.set_notif_hometimeline);

        final SwitchCompat switchCompatWIFI = rootView.findViewById(R.id.set_wifi_only);
        final SwitchCompat switchCompatSilent = rootView.findViewById(R.id.set_silence);

        final Button settings_time_from = rootView.findViewById(R.id.settings_time_from);
        final Button settings_time_to = rootView.findViewById(R.id.settings_time_to);

        final LinearLayout channels_container = rootView.findViewById(R.id.channels_container);
        final Button sound_boost = rootView.findViewById(R.id.sound_boost);
        final Button sound_fav = rootView.findViewById(R.id.sound_fav);
        final Button sound_follow = rootView.findViewById(R.id.sound_follow);
        final Button sound_mention = rootView.findViewById(R.id.sound_mention);
        final Button sound_poll = rootView.findViewById(R.id.sound_poll);
        final Button sound_backup = rootView.findViewById(R.id.sound_backup);
        final Button sound_media = rootView.findViewById(R.id.sound_media);
        Button set_notif_sound = rootView.findViewById(R.id.set_notif_sound);
        settings_time_from.setText(time_from);
        settings_time_to.setText(time_to);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            set_notif_sound.setVisibility(View.GONE);
            channels_container.setVisibility(View.VISIBLE);

            sound_boost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_boost");
                    startActivity(intent);
                }
            });

            sound_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_fav");
                    startActivity(intent);
                }
            });

            sound_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_follow");
                    startActivity(intent);
                }
            });

            sound_mention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_mention");
                    startActivity(intent);
                }
            });

            sound_poll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_poll");
                    startActivity(intent);
                }
            });

            sound_backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_backup");
                    startActivity(intent);
                }
            });

            sound_media.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, "channel_store");
                    startActivity(intent);
                }
            });
        }else{
            set_notif_sound.setVisibility(View.VISIBLE);
            channels_container.setVisibility(View.GONE);
            set_notif_sound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.select_sound));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    startActivityForResult(intent, ACTIVITY_CHOOSE_SOUND);
                }
            });
        }


        boolean enable_time_slot = sharedpreferences.getBoolean(Helper.SET_ENABLE_TIME_SLOT, true);
        final CheckBox set_enable_time_slot = rootView.findViewById(R.id.set_enable_time_slot);
        set_enable_time_slot.setChecked(enable_time_slot);

        set_enable_time_slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_ENABLE_TIME_SLOT, set_enable_time_slot.isChecked());
                editor.apply();
            }
        });


        settings_time_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_from.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0"+String.valueOf(hourOfDay):String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0"+String.valueOf(minute):String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if( Helper.compareDate(context, newDate, false) ) {
                            editor.putString(Helper.SET_TIME_FROM, newDate);
                            editor.apply();
                            settings_time_from.setText(newDate);
                        }else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
                            Toasty.error(context, context.getString(R.string.settings_time_lower, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_init));
                timePickerDialog.show();
            }
        });

        settings_time_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_to.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0" + String.valueOf(minute) : String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if (Helper.compareDate(context, newDate, true)) {
                            editor.putString(Helper.SET_TIME_TO, newDate);
                            editor.apply();
                            settings_time_to.setText(newDate);
                        } else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
                            Toasty.error(context, context.getString(R.string.settings_time_greater, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_end));
                timePickerDialog.show();
            }
        });


        final Spinner action_notification = rootView.findViewById(R.id.action_notification);
        ArrayAdapter<CharSequence> adapterAction = ArrayAdapter.createFromResource(getContext(),
                R.array.action_notification, android.R.layout.simple_spinner_item);
        action_notification.setAdapter(adapterAction);
        int positionNotificationAntion;
        switch (sharedpreferences.getInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_ACTIVE)){
            case Helper.ACTION_ACTIVE:
                positionNotificationAntion = 0;
                break;
            case Helper.ACTION_SILENT:
                positionNotificationAntion = 1;
                break;
            default:
                positionNotificationAntion = 0;
        }
        action_notification.setSelection(positionNotificationAntion);
        action_notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    switch (position) {
                        case 0:
                            editor.putInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_ACTIVE);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_SILENT);
                            editor.apply();
                            break;
                    }
                }
                count1++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_notif_follow.setChecked(notif_follow);
        set_notif_follow_add.setChecked(notif_add);
        set_notif_follow_ask.setChecked(notif_ask);
        set_notif_follow_mention.setChecked(notif_mention);
        set_notif_follow_share.setChecked(notif_share);
        set_notif_follow_poll.setChecked(notif_poll);
        set_notif_hometimeline.setChecked(notif_hometimeline);

        switchCompatWIFI.setChecked(notif_wifi);
        switchCompatSilent.setChecked(notif_silent);

        set_notif_hometimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_HOMETIMELINE, set_notif_hometimeline.isChecked());
                editor.apply();
            }
        });
        set_notif_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_notif_follow.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_ADD, set_notif_follow_add.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_ASK, set_notif_follow_ask.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_mention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_MENTION, set_notif_follow_mention.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SHARE, set_notif_follow_share.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_poll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_POLL, set_notif_follow_poll.isChecked());
                editor.apply();
            }
        });


        switchCompatWIFI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_WIFI_ONLY, isChecked);
                editor.apply();

            }
        });

        final Spinner led_colour_spinner = rootView.findViewById(R.id.led_colour_spinner);
        final TextView ledLabel = rootView.findViewById(R.id.set_led_colour_label);

        switchCompatSilent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SILENT, isChecked);
                editor.apply();

                if (isChecked) {
                    ledLabel.setEnabled(true);
                    led_colour_spinner.setEnabled(true);
                } else {
                    ledLabel.setEnabled(false);
                    for (View lol : led_colour_spinner.getTouchables()) {
                        lol.setEnabled(false);
                    }
                }
            }
        });

        if (sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT, false)) {

            ledLabel.setEnabled(true);
            led_colour_spinner.setEnabled(true);

            ArrayAdapter<CharSequence> adapterLEDColour = ArrayAdapter.createFromResource(getContext(), R.array.led_colours, android.R.layout.simple_spinner_item);
            led_colour_spinner.setAdapter(adapterLEDColour);
            int positionSpinnerLEDColour = (sharedpreferences.getInt(Helper.SET_LED_COLOUR, Helper.LED_COLOUR));
            led_colour_spinner.setSelection(positionSpinnerLEDColour);

            led_colour_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (count > 0) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_LED_COLOUR, position);
                        editor.apply();
                    } else {
                        count++;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        else {
            ledLabel.setEnabled(false);
            for (View lol : led_colour_spinner.getTouchables()) {
                lol.setEnabled(false);
            }
        }

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == ACTIVITY_CHOOSE_SOUND){
            try{
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_NOTIF_SOUND,  uri.toString());
                editor.apply();

            }catch (Exception e){
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }




}
