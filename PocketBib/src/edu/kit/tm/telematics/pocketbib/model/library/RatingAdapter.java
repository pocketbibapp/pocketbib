package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.model.ArrayListAdapter;

public class RatingAdapter extends ArrayListAdapter<Rating<? extends Item>> implements Filterable {

	public RatingAdapter(Context context, List<Rating<? extends Item>> items) {
		super(context, items);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getRatingId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.adapter_rating_entry, parent, false);

		RatingBar ratingScore = (RatingBar) rowView.findViewById(R.id.rating_adapter_ratingbar);
		TextView name = (TextView) rowView.findViewById(R.id.rating_adapter_name);
		TextView comment = (TextView) rowView.findViewById(R.id.rating_adapter_comment);

		Rating<? extends Item> rating = getItem(position);
		ratingScore.setRating(rating.getRating());
		String fullName = rating.getUser().getFirstName() + " " + rating.getUser().getLastName();
		name.setText(fullName);
		comment.setText(rating.getComment());
		return rowView;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<Rating<? extends Item>>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				Assert.assertTrue(constraint != null);
				
				Integer userId = null;
				try {
					userId = Integer.valueOf(constraint.toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				List<Rating<? extends Item>> result = new ArrayList<Rating<? extends Item>>(items.size());

				for (Rating<? extends Item> rating : items) {
					if (userId == null || !userId.equals(rating.getUser().getUserId())) {
						result.add(rating);
					}
				}

				FilterResults newFilterResults = new FilterResults();
				newFilterResults.count = result.size();
				newFilterResults.values = result;
				return newFilterResults;
			}
		};
	}

}
