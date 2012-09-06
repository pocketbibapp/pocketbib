package edu.kit.tm.telematics.pocketbib.model.library;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import edu.kit.tm.telematics.pocketbib.R;
import edu.kit.tm.telematics.pocketbib.controller.PocketBibApp;
import edu.kit.tm.telematics.pocketbib.model.ArrayListAdapter;

public class ItemAdapter extends ArrayListAdapter<Item> {
	
	public ItemAdapter(Context context, List<Item> items) {
		super(context, items);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getItemId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_item_entry, parent, false);
			
			ImageView thumbnail = (ImageView) convertView.findViewById(R.id.search_result_thumbnail);
			TextView title = (TextView) convertView.findViewById(R.id.search_result_title);
			TextView secondLine = (TextView) convertView.findViewById(R.id.search_result_second_line);
			
			holder = new ViewHolder(thumbnail, title, secondLine);
			convertView.setTag(holder);			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Item item = getItem(position);
		holder.currentItemId = item.getItemId();
		
		if(item.getThumbnail() == null) {
			holder.thumbnail.setImageDrawable(PocketBibApp.getItemCoverManager().getFallbackThumbnail());
			
			if(holder.task != null) {
				holder.task.cancel(true);
			}
			
			holder.task = new LoadThumbnailTask(item, holder);
			holder.task.execute();
		} else {
			holder.thumbnail.setImageDrawable(item.getThumbnail());
		}
		
		holder.title.setText(item.getTitle());
		holder.secondLine.setText(item.getSecodaryInformation());
		holder.secondLine.setTextColor(0xAA000000);

		if (checkSelected(position)) {
			convertView.setBackgroundColor(0xcc33b5e5);
		} else {
			convertView.setBackgroundColor(0x00FFFFFF);
		}

		return convertView;
	}
	
	private static  class ViewHolder {
		
		public final ImageView thumbnail;
		
		public final TextView title;
		
		public final TextView secondLine;
		
		public int currentItemId = -1;
		
		public LoadThumbnailTask task = null;

		public ViewHolder(ImageView thumbnail, TextView title, TextView secondLine) {
			this.thumbnail = thumbnail;
			this.title = title;
			this.secondLine = secondLine;
		}
	}
	
	private class LoadThumbnailTask extends AsyncTask<Void, Void, Void> {

		private final Item item;
		
		private final ViewHolder holder;
		
		public LoadThumbnailTask(Item item, ViewHolder holder) {
			super();
			
			this.item = item;
			this.holder = holder;
		}

		@Override
		protected Void doInBackground(Void... params) {
			item.setThumbnail(PocketBibApp.getItemCoverManager().getThumbnail(item));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (!isCancelled() && item.getThumbnail() != null && holder.currentItemId == item.getItemId())
				holder.thumbnail.setImageDrawable(item.getThumbnail());
		}
	}
	
}
