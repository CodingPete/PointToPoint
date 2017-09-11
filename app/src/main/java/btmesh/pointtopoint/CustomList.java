package btmesh.pointtopoint;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by julia on 11.09.2017.
 */

public class CustomList extends ArrayAdapter<RowItem> {
    Context context;

    public CustomList(Context context, int resourceId,
                                 List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtUuid;
        TextView txtMac;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtUuid = (TextView) convertView.findViewById(R.id.uuid);
            holder.txtMac = (TextView) convertView.findViewById(R.id.mac);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtUuid.setText(rowItem.getUuid());
        holder.txtMac.setText(rowItem.getMac());

        return convertView;
    }
}
