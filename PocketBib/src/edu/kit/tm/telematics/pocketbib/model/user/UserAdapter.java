package edu.kit.tm.telematics.pocketbib.model.user;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.model.ArrayListAdapter;

public class UserAdapter extends ArrayListAdapter<LoggedInUser> {

	public UserAdapter(Context context, List<LoggedInUser> items) {
		super(context, items);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getUserId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView nameTextView, emailTextView;
		
		// use the reusable convertView for better performance
		if(convertView == null) {
			LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.adapter_user_entry, parent, false);
			
			nameTextView = (TextView) convertView.findViewById(R.id.user_adapter_name);
			emailTextView = (TextView) convertView.findViewById(R.id.user_adapter_email);
			
			convertView.setTag(new Pair<TextView, TextView>(nameTextView, emailTextView));
		} else {
			@SuppressWarnings("unchecked")
			Pair<TextView, TextView> views = (Pair<TextView, TextView>) convertView.getTag();
			
			nameTextView = views.first;
			emailTextView = views.second;
		}
		
		LoggedInUser user = getItem(position);
		
		nameTextView.setText(user.getLastName() + ", " + user.getFirstName());
		emailTextView.setText(user.getEmail());
		
		return convertView;
	}
}
