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

import java.util.List;

class ConstructorListAdapter extends ArrayAdapter<Players> {
	private int	resource;
	private LayoutInflater inflater;
	private Context context;

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
		if (p != null) name.setText(p.getName());
		TextView info = (TextView) convertView.findViewById(R.id.tvInfo2);
		if (p != null) info.setText("NextGate: " + p.getNextGate() + " Laps: " + p.getTotalLaps() + "\nKills: " + p.getTotalKills() + " Deaths: " + p.getTotalDeaths());
		ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
        String uri = "drawable/truck";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
		Drawable image;
		if (Build.VERSION.SDK_INT < 22) image = context.getResources().getDrawable(imageResource);
		else image = context.getDrawable(imageResource);
		imageView.setImageDrawable(image);
		return convertView;
	}
}

