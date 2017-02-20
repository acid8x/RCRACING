package net.igeneric.rcracing;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class ConstructorListAdapter extends ArrayAdapter<Players> {
	private int	resource, height;
	private LayoutInflater inflater;
	private Context context;
	private String[] positions = { "drawable/gold", "drawable/silver", "drawable/copper" };

	ConstructorListAdapter(Context ctx, int resourceId, List<Players> objects, int heightResize) {
		super( ctx, resourceId, objects );
		resource = resourceId;
		inflater = LayoutInflater.from( ctx );
		context = ctx;
		height = heightResize;
	}

	@NonNull
	@Override
	public View getView (int position, View convertView, @NonNull ViewGroup parent ) {
		if (convertView == null) convertView = inflater.inflate( resource, null );
		final Players p = getItem( position );
		if (p != null) {
			TextView ivKill = (TextView) convertView.findViewById(R.id.tvKill);
			TextView ivDeath = (TextView) convertView.findViewById(R.id.tvDeath);
			TextView ivLive = (TextView) convertView.findViewById(R.id.tvLive);
			TextView ivGates = (TextView) convertView.findViewById(R.id.tvGates);
			TextView ivLaps = (TextView) convertView.findViewById(R.id.tvLaps);
			TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
			String name = p.getName();
			tvName.setText(name);
			if (height != -1) {
				ivKill.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
				ivDeath.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
				ivLive.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
				ivGates.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
				ivLaps.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
				tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,height);
			}

			ImageView imageView = (ImageView) convertView.findViewById(R.id.ivLogo);
			String uri = "drawable/truck";
			int playerPosition = MainActivity.mPlayersList.indexOf(p);
			if (p.isFinish() && playerPosition < 3) uri = positions[playerPosition];
			int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
			if (Build.VERSION.SDK_INT < 22) imageView.setImageDrawable(context.getResources().getDrawable(imageResource));
			else imageView.setImageDrawable(context.getDrawable(imageResource));
			switch (MainActivity.raceType) { // 3 = LIVES
				case 1: // RACE NO GUNS - 45
					setString(ivGates,p.getNextGate());
					setString(ivLaps,p.getTotalLaps());
					break;
				case 2: // RACE WITH GUNS - 1245+3
					setString(ivKill,p.getTotalKills());
					setString(ivDeath,p.getTotalDeaths());
					if (MainActivity.raceLivesNumber > 0) setString(ivLive,p.getLives());
					setString(ivGates,p.getNextGate());
					setString(ivLaps,p.getTotalLaps());
					break;
				case 3: // BATTLE - 12+3
					setString(ivKill,p.getTotalKills());
					setString(ivDeath,p.getTotalDeaths());
					if (MainActivity.raceLivesNumber > 0) setString(ivLive,p.getLives());
					break;
				case 4: // HUNTING - 1
					setString(ivKill,p.getTotalKills());
					break;
			}
		}
		return convertView;
	}

	private void setString(TextView tv, int i) {
		String s = "";
		if (i < 10) {
			s += i;
			s += " ";
		} else s += i;
		tv.setText(s);
		if (tv.getVisibility() == View.INVISIBLE) tv.setVisibility(View.VISIBLE);
	}
}

