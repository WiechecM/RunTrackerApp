package maciek.w.runtracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by Maciek on 17.11.2020
 */
class RVAdapter extends RecyclerView.Adapter<RVAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> date;
    ArrayList<String> description;
    ArrayList<Integer> trainingID;

    public RVAdapter(Context _context, ArrayList<String> _date, ArrayList<String> _desc,
                     ArrayList<Integer> _trainingID){
        this.context=_context;
        this.date=_date;
        this.description=_desc;
        this.trainingID=_trainingID;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_row,parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        holder.rv_date.setText(date.get(position));
        holder.rv_description.setText(description.get(position));
        holder.rv_image_view.setImageResource(R.drawable.running_icon);

        holder.rv_row_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,TrainingViewer.class);
                intent.putExtra("date",date.get(position));
                intent.putExtra("desc",description.get(position));
                intent.putExtra("id",trainingID.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return date.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView rv_date;
        TextView rv_description;
        ImageView rv_image_view;
        ConstraintLayout rv_row_layout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            rv_date = itemView.findViewById(R.id.rv_date);
            rv_description = itemView.findViewById(R.id.rv_description);
            rv_image_view  = itemView.findViewById(R.id.rv_image_view);
            rv_row_layout = itemView.findViewById(R.id.rv_row_layout);

        }
    }
}
