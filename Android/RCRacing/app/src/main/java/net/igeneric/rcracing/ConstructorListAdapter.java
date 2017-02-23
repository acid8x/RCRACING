package net.igeneric.rcracing;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class ConstructorListAdapter extends ArrayAdapter<Players> {
	private int	resource;
	private LayoutInflater inflater;
	private Context context;
	private String[] positions = { "drawable/gold", "drawable/silver", "drawable/copper" };

	ConstructorListAdapter(Context ctx, int resourceId, List<Players> objects) {
		super( ctx, resourceId, objects );
		resource = resourceId;
		inflater = LayoutInflater.from( ctx );
		context = ctx;
	}

	@NonNull
	@Override
	public View getView (int position, View convertView, @NonNull ViewGroup parent ) {
		if (convertView == null) convertView = inflater.inflate( resource, null );
        int height = parent.getMeasuredHeight();
        if (MainActivity.landscape) height/=3;
        else height/=5;
		final Players p = getItem( position );
		if (p != null) {
			switch (MainActivity.raceType) { // 3 = LIVES
				case 1: // RACE NO GUNS - 45
					setView(new int[]{R.id.tvGates,R.id.tvLaps},convertView,new int[]{p.getNextGate(),p.getTotalLaps()});
					break;
				case 2: // RACE WITH GUNS - 1245+3
					setView(new int[]{R.id.tvGates,R.id.tvLaps,R.id.tvLive,R.id.tvKill,R.id.tvDeath},convertView,
							new int[]{p.getNextGate(),p.getTotalLaps(),p.getLives(),p.getTotalKills(),p.getTotalDeaths()});
					break;
				case 3: // BATTLE - 12+3
					setView(new int[]{R.id.tvLive,R.id.tvKill,R.id.tvDeath},convertView,new int[]{p.getLives(),p.getTotalKills(),p.getTotalDeaths()});
					break;
				case 4: // HUNTING - 1
					setView(new int[]{R.id.tvKill},convertView,new int[]{p.getNextGate(),p.getTotalLaps(),p.getLives(),p.getTotalKills(),p.getTotalDeaths()});
					break;
			}
			TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
			String name = p.getName();
			tvName.setText(name);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.ivTruck);
			String uri = "drawable/truck";
			int playerPosition = MainActivity.mPlayersList.indexOf(p);
			if (p.isFinish() && playerPosition < 3) uri = positions[playerPosition];
			int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
			if (Build.VERSION.SDK_INT < 22) imageView.setImageDrawable(context.getResources().getDrawable(imageResource));
			else imageView.setImageDrawable(context.getDrawable(imageResource));
		}
		convertView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
		return convertView;
	}

	private void setView(int[] views, View convertView, int[] values) {
		for (int i=0;i<views.length;i++) {
			TextView tv = (TextView) convertView.findViewById(views[i]);
			String s = "";
			if (values[i] == -1) s += "-";
			else if (values[i] < 10) {
				s += values[i];
				s += " ";
			} else s += values[i];
			tv.setText(s);
		}
	}
}

