package net.igeneric.rcracing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class broadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BTService.ACTION_DATA_AVAILABLE)) {
            final char[] array = intent.getCharArrayExtra(BTService.EXTRA_DATA);
            String data = "";
            int index = 0;
            boolean read = false;
            if (array != null) {
                for (char c : array) {
                    if (!read && c > 48 && c < 58) read = true;
                    if (read && c > 47 && c < 91) {
                        data += c;
                        index++;
                        if (index == 3) {
                            final char[] charArray = data.toCharArray();
                            final int id = charArray[0] - 48;
                            final char command = charArray[1];
                            final int arg = charArray[2] - 48;
                            if (id < 10 && command > 64 && arg < 10) dataHandle(id, command, arg);
                            data = "";
                            index = 0;
                            read = false;
                        }
                    }
                }
            }
            if (MainActivity.hasFocus) context.sendBroadcast(new Intent("ACTION_UPDATE_UI"));
        }
    }

    private void dataHandle(int id, char command, int arg) {
        Players p = getPlayers(id);
        if (p == null) {
            p = new Players(id);
            MainActivity.mPlayersList.add(p);
        } else if (p.isFinish()) return;
        switch (command) {
            case 'D':
                if (MainActivity.raceType > 1) {
                    p.addTotalDeaths();
                    dataHandle(arg,'K',1);
                }
                break;
            case 'G':
                p.addTotalGates(arg);
                break;
            case 'K':
                p.addTotalKills();
                break;
            case 'Z':
                BTService.sendMessage("&" + id + "C" + MainActivity.raceType);
                break;
        }
        if (p.isFinish()) MainActivity.mWinnersList.add(id);
    }

    @Nullable
    private Players getPlayers(int id) {
        for (Players players : MainActivity.mPlayersList) {
            if (players.checkId(id)) return players;
        }
        return null;
    }
}