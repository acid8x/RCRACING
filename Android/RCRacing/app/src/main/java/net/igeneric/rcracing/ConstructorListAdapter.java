package net.igeneric.rcracing;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.viewanimator.ViewAnimator;

import java.util.List;

class ConstructorListAdapter extends ArrayAdapter<Players> {
	private int	resource;
	private LayoutInflater inflater;
	private Context context;
	private String[] positions = { "drawable/gold", "drawable/silver", "drawable/copper" };
	private boolean anim = false;

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
		Players p = getItem( position );
		TextView name = (TextView) convertView.findViewById(R.id.tvInfo1);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
		if (p != null) {
			String text = p.getName();
			name.setText(text);
			TextView[] tvs = new TextView[5];
			int[] ids = new int[]{R.id.ivKill, R.id.ivDeath, R.id.ivLive, R.id.ivGates, R.id.ivLaps};
			for (int i = 0; i < 5; i++) tvs[i] = (TextView) convertView.findViewById(ids[i]);
			if (MainActivity.raceType != p.getRaceType()) {
				p.setRaceType(MainActivity.raceType);
				switch (MainActivity.raceType) {
					case 1:
						tvs[0].setScaleX(0);
						tvs[1].setScaleX(0);
						tvs[2].setScaleX(0);
						tvs[3].setScaleX(1);
						tvs[4].setScaleX(1);
						break;
					case 2:
						tvs[0].setScaleX(1);
						tvs[1].setScaleX(1);
						if (MainActivity.raceLivesNumber > 0) tvs[2].setScaleX(1);
						else tvs[2].setScaleX(0);
						tvs[3].setScaleX(1);
						tvs[4].setScaleX(1);
						break;
					case 3:
						tvs[0].setScaleX(1);
						tvs[1].setScaleX(1);
						if (MainActivity.raceLivesNumber > 0) tvs[2].setScaleX(1);
						else tvs[2].setScaleX(0);
						tvs[3].setScaleX(0);
						tvs[4].setScaleX(0);
						break;
					case 4:
						tvs[0].setScaleX(1);
						tvs[1].setScaleX(0);
						tvs[2].setScaleX(0);
						tvs[3].setScaleX(0);
						tvs[4].setScaleX(0);
						break;
				}
			}
		}
		String uri = "drawable/truck";
		if (p != null) {
			int playerPosition = MainActivity.mPlayersList.indexOf(p);
			if (p.isFinish() && playerPosition < 3) {
				uri = positions[playerPosition];
				anim = true;
			}
		}
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
		Drawable image;
		if (Build.VERSION.SDK_INT < 22) image = context.getResources().getDrawable(imageResource);
		else image = context.getDrawable(imageResource);
		imageView.setImageDrawable(image);
		if (anim) ViewAnimator.animate(imageView).bounce().wobble().repeatCount(-1).start();
		return convertView;
	}
}

