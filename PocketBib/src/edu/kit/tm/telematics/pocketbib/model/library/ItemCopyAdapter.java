package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.ArrayListAdapter;
import edu.kit.tm.telematics.pocketbib.model.user.LoggedInUser;

public class ItemCopyAdapter extends ArrayListAdapter<ItemCopy> {

	@SuppressWarnings("unused")
	/** Tag for debugging purposes. */
	private final static String TAG = "ItemCopyAdapter";
	
	public ItemCopyAdapter(Context context, List<ItemCopy> items) {
		super(context, items);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getItemCopyId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_copy_entry, parent, false);	
		}
			
		TextView copyNumber = (TextView) convertView.findViewById(R.id.search_result_entry);
		TextView secondLine = (TextView) convertView.findViewById(R.id.search_result_second_row);		

		ItemCopy copy = getItem(position);
		
		LoggedInUser user = copy.getUser();
		
		// or you can change it to show copy.getItemCopyId()
		copyNumber.setText(PocketBibApp.getAppContext().getString(R.string.label_copy_id, (position + 1)));
		
		if (user != null) {
			secondLine.setText(PocketBibApp.getAppContext().getString(R.string.label_copy_lent_by,
					user.getFirstName(), user.getLastName()));
		} else {
			secondLine.setText(R.string.label_copy_available);
		}
		

		if (checkSelected(position)) {
			convertView.setBackgroundColor(0xcc33b5e5);
		} else {
			convertView.setBackgroundColor(0x00FFFFFF);
		}

		return convertView;
	}

}
