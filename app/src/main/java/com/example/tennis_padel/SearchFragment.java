package com.example.tennis_padel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchFragment extends Fragment {

    private MainViewModel viewModel;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false); // Make sure it's fully expanded

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(getContext(), null, this::openOtherProfileFragment); // Initialize with an empty list
        recyclerView.setAdapter(userAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Observe all users data
        viewModel.getAllUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userAdapter.setUserList(users);
            }
        });

        // Filter the list as the user types in the search bar
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.filter(newText);
                return false;
            }
        });

        // Load all users data
        viewModel.loadAllUsers();

        return view;
    }

    private void openOtherProfileFragment(User user) {
        // Create a new instance of OtherProfileFragment
        OtherProfileFragment otherProfileFragment = OtherProfileFragment.newInstance(user);

        // Replace the current fragment with OtherProfileFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, otherProfileFragment)
                .addToBackStack(null)
                .commit();
    }
}
