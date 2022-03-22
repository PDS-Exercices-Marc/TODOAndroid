package org.udg.pds.todoandroid.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.databinding.FragmentGroupListBinding;
import org.udg.pds.todoandroid.entity.Group;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFragment extends Fragment {

    TodoApi mTodoService;
    private FragmentGroupListBinding binding;

    RecyclerView mRecyclerView;
    private GRAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentGroupListBinding.inflate(inflater);
        return binding.getRoot();

    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        mRecyclerView = binding.groupRecyclerview;
        mAdapter = new GRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        // NEW BUTTON LISTENERS GO BELOW HERE
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateGroupList();
    }

    private void showGroupList(List<Group> groups) {
        mAdapter.clear();
        Log.d("GroupFragment_ListUpdate",groups.toString());
        for (Group g : groups) {
            mAdapter.add(g);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Global.RQ_ADD_GROUP) {
            this.updateGroupList();
        }
    }

    private void updateGroupList() {
        Call<List<Group>> call = mTodoService.getGroups();

        call.enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful()) {
                    GroupFragment.this.showGroupList(response.body());
                } else {
                    Toast.makeText(GroupFragment.this.getContext(), "Error reading groups", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(GroupFragment.this.getContext(), "Error making call", Toast.LENGTH_LONG).show();
            }
        });
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        View view;

        GroupViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.groupName);
            description = itemView.findViewById(R.id.groupDesc);
        }
    }

    static class GRAdapter extends RecyclerView.Adapter<GroupFragment.GroupViewHolder> {

        List<Group> list = new ArrayList<>();
        Context context;

        public GRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public GroupFragment.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
            return new GroupFragment.GroupViewHolder(v);
        }

        @Override
        public void onBindViewHolder(GroupFragment.GroupViewHolder holder,
                                     @SuppressLint("RecyclerView") final int position) {
            Log.d("GroupFragment_OnBindViewHolder_Pos", String.valueOf(position));
            Log.d("GroupFragment_OnBindViewHolder_Name",list.get(position).name);
            holder.name.setText(list.get(position).name);
            holder.description.setText(list.get(position).description);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, String.format("Hey, I'm item %1d", position), duration);
                    toast.show();
                }
            });

            animate(holder);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, Group data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(Group data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipate_overshoot_interpolator);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(Group g) {
            list.add(g);
            this.notifyItemInserted(list.size() - 1);
        }

        public void clear() {
            int size = list.size();
            list.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }

}
