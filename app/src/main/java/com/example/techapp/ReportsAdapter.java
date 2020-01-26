package com.example.techapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolderClass> {
    private onItemClickInterface itemClickInterface;
    private ArrayList<TechReportClass> reports;
    private Context context;


    public ReportsAdapter(Context context, ArrayList<TechReportClass> reports) {
        this.reports = reports;
        this.context = context;
          itemClickInterface = (onItemClickInterface) context;
    }

    public void addAll(ArrayList<TechReportClass> data) {
        if (reports != null)
            reports.addAll(data);
    }

    public void clear() {
        if (reports != null)
            reports.clear();
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public interface onItemClickInterface {
         void onItemClickListener(TechReportClass currReport);
    }

    @NonNull
    @Override
    public ViewHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list_item, parent, false);
        return new ViewHolderClass(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderClass holder, int position) {
        holder.bindViews(position);
    }

    public class ViewHolderClass extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView type;
        private TextView section;
        private TextView date;
        private ImageButton menu;
        private ImageView image;
        private TextView authorName;

        @Override
        public void onClick(View v) {
            itemClickInterface.onItemClickListener(reports.get(getAdapterPosition()));
        }

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            type = itemView.findViewById(R.id.type);
            section = itemView.findViewById(R.id.section);
            date = itemView.findViewById(R.id.date);
            menu = itemView.findViewById(R.id.item_menu);
            image = itemView.findViewById(R.id.image);
            authorName = itemView.findViewById(R.id.author_name);
            itemView.setOnClickListener(this);
        }

        public void bindViews(final int index) {
            final TechReportClass current = reports.get(index);
            title.setText(current.getmWebTitle());
            type.setText(context.getResources().getString(R.string.type, current.getmType()));
            if (current.getImage() == null)
                image.setVisibility(View.GONE);
            else
                image.setImageBitmap(current.getImage());
            section.setText(context.getResources().getString(R.string.section, current.getmSection()));
            String authorNames = current.getAuthorName();
            if (authorNames != null && !authorNames.matches("")) {
                authorName.setText(context.getString(R.string.author, authorNames));
                authorName.setVisibility(View.VISIBLE);
            } else {
                authorName.setVisibility(View.GONE);
            }
            String[] dateSplitter = current.getDate().split("T");
            date.setText(dateSplitter[0]);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.item_menu, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_url:
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                                    alertDialog.setMessage(current.getmWebUrl())
                                            .setPositiveButton(context.getString(R.string.open_page), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                       itemClickInterface.onItemClickListener(current);
                                                }
                                            }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();
                            }
                            return false;
                        }
                    });
                }
            });
        }
    }
}
